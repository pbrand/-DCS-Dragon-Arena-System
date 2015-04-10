package server.master;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import common.Enums.UnitType;
import common.IBattleField;
import common.IDragonController;
import common.IRunner;
import common.Log;
import common.Message;
import common.MessageRequest;
import common.Snapshot;

public class BattleField implements IBattleField {

	/* The array of units */
	private Unit[][] map;

	/* The static singleton */
	private static BattleField battlefield;

	/* Primary socket of the battlefield */
	// private Socket serverSocket;

	/*
	 * The last id that was assigned to an unit. This variable is used to
	 * enforce that each unit has its own unique id.
	 */
	private int lastUnitID = 0;

	// public final static String serverID = "server";
	public final static int MAP_WIDTH = 25;
	public final static int MAP_HEIGHT = 25;
	public final static int NR_OF_DRAGONS = 20;
	private HashMap<String, Unit> units;
	private HashMap<String, Unit> dragons;

	private static String myAddress;
	private static HashMap<String, String> helpers;

	private boolean isBackup;

	private String backupAddress;
	private boolean unitsChanged;
	private boolean mapChanged;
	private boolean helpersChanged;
	private boolean dragonsChanged;

	/**
	 * Metrics
	 */
	private boolean firstWrite = true;
	private int totalMessagesSend;
	private int totalMessagesReceived;
	private int totalMessagesFailedToSend;
	private int totalMessagesFailedToReceive;
	private int totalNumberOfAttemptedBackupUpdates;
	private int totalNumberOfSuccessfullBackupUpdates;
	private int totalNumberOfFailedBackupUpdates;
	private int totalNumberOfDisconnectedHelpers;

	private long startTime;
	private long endTime;

	// private static final Logger logger =
	// LogManager.getLogger(BattleField.class);

	/**
	 * Initialize the battlefield to the specified size
	 * 
	 * @param width
	 *            of the battlefield
	 * @param height
	 *            of the battlefield
	 * @throws RemoteException
	 */
	private BattleField(boolean isBackup) throws RemoteException {
		this.startTime = System.currentTimeMillis();
		this.isBackup = isBackup;
	}

	private BattleField() throws RemoteException {
		this(false);
	}

	public void initiateBattleService(String address) {

		synchronized (this) {
			map = new Unit[MAP_WIDTH][MAP_HEIGHT];
			units = new HashMap<String, Unit>();
			helpers = new HashMap<String, String>();
			dragons = new HashMap<String, Unit>();
		}

		setMyAddress(address);
		log("Battlefield created");
		if (!isBackup) {
			initHelperChecker();
			initBackupService();
			dragonRunner();
			metricsRunner();
		}
	}

	/**
	 * Singleton method which returns the sole instance of the battlefield.
	 * 
	 * @return the battlefield.
	 * @throws RemoteException
	 */
	public static BattleField getBattleField() throws RemoteException {
		if (battlefield == null)
			battlefield = new BattleField();
		return battlefield;
	}

	public static BattleField createBackupBattleField() throws RemoteException {
		battlefield = new BattleField(true);
		battlefield.log("Backup created");
		return battlefield;
	}

	@Override
	public void sendMessage(Message msg) {
		IRunner RMIServer = null;
		String clienthost = null;
		try {
			clienthost = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e1) {
			e1.printStackTrace();
		}
		String urlServer = new String("rmi://" + clienthost + ":"
				+ msg.getMiddlemanPort() + "/" + msg.getMiddleman());

		// Bind to RMIServer
		try {
			log(urlServer);
			RMIServer = (IRunner) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			RMIServer.receiveMessage(msg);

			totalMessagesSend += 1;

		} catch (Exception e) {
			e.printStackTrace();
			totalMessagesFailedToSend += 1;
		}

	}

	private void sendMessageToDragon(Message msg) {
		IDragonController RMIServer = null;
		String urlServer = new String("rmi://" + myAddress.split("/")[0] + "/"
				+ msg.getRecipient());

		try {
			log(urlServer);
			RMIServer = (IDragonController) Naming.lookup(urlServer);
			RMIServer.receiveMessage(msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		String from = msg.getSender();
		String request = msg.getRequest();

		totalMessagesReceived += 1;

		log("Message received from: " + msg.getSender() + ", request: "
				+ request);

		Message reply = null;
		Unit unit;
		boolean replyToDragon = false;
		switch (request) {
		case MessageRequest.spawnUnit: {
			log("Spawning: " + msg.getSender());
			Unit player = new Player(from);
			int[] pos = getAvailablePosition();
			boolean spawned = this.spawnUnit(from, player, pos[0], pos[1]);
			reply = new Message(from);
			reply.setRequest(MessageRequest.spawnAck);
			reply.put("spawned", spawned);
			reply.setMiddleman(msg.getMiddleman());
			reply.setMiddlemanPort(msg.getMiddlemanPort());
			break;
		}
		case MessageRequest.putUnit: {
			Unit initiator = units.get((String) msg.get("unitID"));
			if (initiator.getHitPoints() > 0) {
				this.putUnit((Unit) msg.get("unit"), (Integer) msg.get("x"),
						(Integer) msg.get("y"));
			} else {
				this.sendGameOverMessage(msg);
			}
			break;
		}
		case MessageRequest.getUnit: {
			reply = new Message(from);
			int x = (Integer) msg.get("x");
			int y = (Integer) msg.get("y");
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			reply.put("id", msg.get("id"));
			// Get the unit at the specific location
			reply.put("unit", getUnit(x, y));
			break;
		}
		case MessageRequest.getType: {
			reply = new Message(from);
			int x = (int) msg.get("x");
			int y = (int) msg.get("y");
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			reply.put("id", msg.get("id"));
			if (getUnit(x, y) instanceof Player)
				reply.put("type", UnitType.player);
			else if (getUnit(x, y) instanceof Dragon)
				reply.put("type", UnitType.dragon);
			else
				reply.put("type", UnitType.undefined);
			break;
		}
		case MessageRequest.dealDamage: {
			Unit initiator = units.get((String) msg.get("unitID"));
			if (initiator.getHitPoints() > 0) {
				int x = (Integer) msg.get("x");
				int y = (Integer) msg.get("y");
				this.dealdamage(x, y, msg);
			} else {
				this.sendGameOverMessage(msg);
			}
			break;
		}
		case MessageRequest.healDamage: {
			Unit initiator = units.get((String) msg.get("unitID"));
			if (initiator.getHitPoints() > 0) {
				int x = (Integer) msg.get("x");
				int y = (Integer) msg.get("y");
				unit = this.getUnit(x, y);
				if (unit != null) {
					unit.adjustHitPoints(units.get((String) msg.get("unitID"))
							.getAttackPoints());
					unitsChanged = true;
				}
			} else {
				this.sendGameOverMessage(msg);
			}
			break;
		}
		case MessageRequest.moveUnit: {

			this.moveUnit(units.get((String) msg.get("unitID")),
					(int) msg.get("x"), (int) msg.get("y"));

			break;
		}
		case MessageRequest.removeUnit: {
			this.removeUnit((String) msg.get("unitID"));
			break;
		}
		case MessageRequest.disconnectUnit: {
			Unit unitToBeRemoved = units.get((String) msg.get("unitID"));
			boolean disconnect = this
					.disconnectUnit((String) msg.get("unitID"));
			if (unitToBeRemoved instanceof Player) {
				reply = new Message(from);
				reply.setRequest(MessageRequest.disconnectAck);
				reply.put("disconnected", disconnect);
				reply.setMiddleman(msg.getMiddleman());
				reply.setMiddlemanPort(msg.getMiddlemanPort());
			}
			break;
		}
		case MessageRequest.getTargets: {
			Unit initiator = units.get((String) msg.get("unitID"));
			if (initiator.getHitPoints() > 0) {
				String id = (String) msg.get("unitID");
				if (units.get(id) == null) {
					break;
				}
				int x = units.get(id).getX();
				int y = units.get(id).getY();

				reply = new Message(from);
				reply.setRequest(MessageRequest.returnTargets);
				System.out.println("From dragon: " + from);
				if (getUnit(x, y) instanceof Player) {
					reply.setMiddleman((String) msg.getMiddleman());
					reply.setMiddlemanPort((int) msg.getMiddlemanPort());
					reply = getPlayerTargetsMessage(x, y, reply);
				} else {
					reply = getDragonTargetsMessage(x, y, reply);
					replyToDragon = true;
				}
			} else {
				this.sendGameOverMessage(msg);
			}
			break;
		}

		}

		if (reply != null) {
			if (!replyToDragon) {
				sendMessage(reply);
			} else {
				replyToDragon = false;
				sendMessageToDragon(reply);
			}
		}

	}

	private void sendGameOverMessage(Message msg) {
		String id = (String) msg.get("unitID");
		log("Game over request to: " + id);
		int x = units.get(id).getX();
		int y = units.get(id).getY();

		Message reply = new Message(id);
		reply.setRequest(MessageRequest.gameOver);
		if (getUnit(x, y) instanceof Player) {
			reply.setMiddleman((String) msg.getMiddleman());
			reply.setMiddlemanPort((int) msg.getMiddlemanPort());
			sendMessage(reply);
		} else {
			String address = myAddress.split("/")[0];
			reply.setMiddleman(reply.getRecipient());
			reply.setMiddlemanPort(Integer.parseInt(address.split(":")[1]));
			sendMessageToDragon(reply);
		}
	}

	private void dealdamage(int x, int y, Message msg) {
		Unit unit = this.getUnit(x, y);
		if (unit != null) {
			unit.adjustHitPoints(-units.get((String) msg.get("unitID"))
					.getAttackPoints());
			unitsChanged = true;

			if (unit.getHitPoints() <= 0) {
				// removeUnit(unit.getUnitID());

				sendGameOverMessage(msg);
			}
		}
	}

	private synchronized Message getPlayerTargetsMessage(int x, int y,
			Message reply) {
		ArrayList<Unit> players = new ArrayList<Unit>();
		ArrayList<Unit> dragons = new ArrayList<Unit>();

		int closestEnemyDistance = Integer.MAX_VALUE;
		Unit closestDragon = null;
		// We assume that HEAL_RANGE >= ATTACK_RANGE will always apply.
		for (int i = 0; i < BattleField.MAP_WIDTH; i++) {
			for (int j = 0; j < BattleField.MAP_HEIGHT; j++) {
				if (i != x || j != y) {
					int distance = Math.abs(x - i) + Math.abs(y - j);

					if (this.getUnit(i, j) instanceof Player
							&& (distance <= Player.HEAL_RANGE)) {
						players.add(getUnit(i, j));
					} else if (this.getUnit(i, j) instanceof Dragon) {
						if (distance <= Player.ATTACK_RANGE) {
							dragons.add(getUnit(i, j));
						}
						if (distance < closestEnemyDistance) {
							closestEnemyDistance = distance;
							closestDragon = getUnit(i, j);
						}
					}
				}
			}
		}

		reply.put("players", players);
		reply.put("dragons", dragons);
		reply.put("playerX", x);
		reply.put("playerY", y);
		if (closestDragon != null) {
			reply.put("enemyX", closestDragon.getX());
			reply.put("enemyY", closestDragon.getY());
		}
		return reply;
	}

	private synchronized Message getDragonTargetsMessage(int x, int y,
			Message reply) {
		ArrayList<Unit> players = new ArrayList<Unit>();
		int minX = x - Dragon.ATTACK_RANGE >= 0 ? x - Dragon.ATTACK_RANGE : 0;
		int maxX = x + Dragon.ATTACK_RANGE <= BattleField.MAP_WIDTH ? x
				+ Dragon.ATTACK_RANGE : BattleField.MAP_WIDTH;
		int minY = y - Dragon.ATTACK_RANGE >= 0 ? y - Dragon.ATTACK_RANGE : 0;
		int maxY = y + Dragon.ATTACK_RANGE <= BattleField.MAP_HEIGHT ? y
				+ Dragon.ATTACK_RANGE : BattleField.MAP_HEIGHT;
		for (int i = minX; i < maxX; i++) {
			for (int j = minY; j < maxY; j++) {
				if (i != x || j != y) {
					int distance = Math.abs(x - i) + Math.abs(y - j);
					if (this.getUnit(i, j) instanceof Player) {
						if (distance <= Dragon.ATTACK_RANGE) {
							players.add(getUnit(i, j));
						}
					}
				}
			}
		}

		reply.put("players", players);
		String address = myAddress.split("/")[0];
		reply.setMiddleman(reply.getRecipient());
		reply.setMiddlemanPort(Integer.parseInt(address.split(":")[1]));
		return reply;
	}

	public int[] getAvailablePosition() {
		int[] pos = null;

		Random random = new Random();
		boolean running = true;
		int x = 0;
		int y = 0;

		while (running) {
			x = (int) random.nextInt(MAP_WIDTH - 1);
			y = (int) random.nextInt(MAP_HEIGHT - 1);
			log("x: " + x + " ,y: " + y);
			if (getUnit(x, y) == null) {
				running = false;
			}
		}
		pos = new int[2];
		pos[0] = x;
		pos[1] = y;
		log("Position: " + pos);
		return pos;
	}

	/**
	 * Put a unit at the specified position. First, it checks whether the
	 * position is empty, if not, it does nothing.
	 * 
	 * @param unit
	 *            is the actual unit being put on the specified position.
	 * @param x
	 *            is the x position.
	 * @param y
	 *            is the y position.
	 * @return true when the unit has been put on the specified position.
	 */
	private synchronized boolean putUnit(Unit unit, int x, int y) {
		if (map[x][y] != null)
			return false;

		map[x][y] = unit;
		unit.setPosition(x, y);
		mapChanged = true;

		return true;
	}

	/**
	 * Puts a new unit at the specified position. First, it checks whether the
	 * position is empty, if not, it does nothing. In addition, the unit is also
	 * put in the list of known units.
	 * 
	 * @param unit
	 *            is the actual unit being spawned on the specified position.
	 * @param x
	 *            is the x position.
	 * @param y
	 *            is the y position.
	 * @return true when the unit has been put on the specified position.
	 */
	private boolean spawnUnit(String id, Unit unit, int x, int y) {
		synchronized (this) {
			if (map[x][y] != null)
				return false;

			map[x][y] = unit;
			unit.setPosition(x, y);
		}
		units.put(id, unit);
		unitsChanged = true;
		mapChanged = true;
		log("Unit spwaned");

		return true;
	}

	/**
	 * Move the specified unit a certain number of steps.
	 * 
	 * @param unit
	 *            is the unit being moved.
	 * @param deltax
	 *            is the delta in the x position.
	 * @param deltay
	 *            is the delta in the y position.
	 * 
	 * @return true on success.
	 */
	private synchronized boolean moveUnit(Unit unit, int newX, int newY) {
		int originalX = unit.getX();
		int originalY = unit.getY();

		if (newX >= 0 && newX < BattleField.MAP_WIDTH)
			if (newY >= 0 && newY < BattleField.MAP_HEIGHT)
				if (map[newX][newY] == null) {
					if (putUnit(unit, newX, newY)) {
						map[originalX][originalY] = null;
						return true;
					}
				}

		return false;
	}

	/**
	 * Remove a unit from a specific position and makes the unit disconnect from
	 * the server.
	 * 
	 * @param x
	 *            position.
	 * @param y
	 *            position.
	 */
	private synchronized boolean removeUnit(String id) {
		Unit unitToRemove = this.units.get(id);
		if (unitToRemove == null) {
			log("Unit with id: " + id + " could not be removed, no unit found");
			return false; // There was no unit here to remove
		}
		int x = unitToRemove.getX();
		int y = unitToRemove.getY();
		map[x][y] = null;
		Unit removed = units.remove(unitToRemove);

		if (removed == null && map[x][y] == null) {
			unitsChanged = true;
			mapChanged = true;
			return true;
		} else {
			return false;
		}
	}

	private boolean disconnectUnit(String id) {
		if (units.get(id) == null) {
			return true;
		}
		boolean removed = removeUnit(id);
		if (removed) {
			log("Unit with id: " + id + " has disconnected.");
		} else {
			log("Unit with id:" + id + " could not be disconnected.");
		}
		return removed;
	}

	/**
	 * Get a unit from a position.
	 * 
	 * @param x
	 *            position.
	 * @param y
	 *            position.
	 * @return the unit at the specified position, or return null if there is no
	 *         unit at that specific position.
	 */
	public synchronized Unit getUnit(int x, int y) {
		assert x >= 0 && x < map.length;
		assert y >= 0 && x < map[0].length;

		return map[x][y];
	}

	/**
	 * Returns a new unique unit ID.
	 * 
	 * @return int: a new unique unit ID.
	 */
	public synchronized int getNewUnitID() {
		return ++lastUnitID;
	}

	public void setMyAddress(String myAddress) {
		BattleField.myAddress = myAddress;
	}

	public HashMap<String, String> getHelpers() {
		return BattleField.helpers;
	}

	@Override
	public void putHelper(String key, String value) {
		BattleField.helpers.put(key, value);
		helpersChanged = true;
		printHelpers();
		if (this.backupAddress != null) {
			updateBackupServerForHelper(key, this.backupAddress.split("/")[0]);
		}
	}

	public String getRandomHelper() {
		if (helpers.size() == 0) {
			return "noServers";
		}
		Random rnd = new Random();
		int i = (int) rnd.nextInt(helpers.size());
		List<String> list = new ArrayList<String>(helpers.keySet());
		if (!isHelperAlive(list.get(i))) {
			return null;
		} else
			return list.get(i) + ":" + helpers.get(list.get(i));
	}

	private void checkHelpers() {
		Iterator<String> iterator = helpers.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			isHelperAlive(key);
		}
	}

	private boolean isHelperAlive(String helper) {

		IRunner RMIServer = null;
		String urlServer = new String("rmi://" + helpers.get(helper) + "/"
				+ helper);

		// Bind to RMIServer
		try {
			RMIServer = (IRunner) Naming.lookup(urlServer);
			RMIServer.ping();
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			helpers.remove(helper);
			helpersChanged = true;
			totalNumberOfDisconnectedHelpers += 1;
			printHelpers();

			if (helpers.size() == 0) {
				log("All helpers are disconnected");
			}

			return false;
		}
	}

	private void printHelpers() {
		Iterator<String> iterator = helpers.keySet().iterator();
		System.out.println();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = helpers.get(key).toString();
			log(key + ": " + value);
		}
		System.out.println();
	}

	@Override
	public int[] getPosition(String id) throws RemoteException {
		int[] pos = new int[2];
		pos[0] = units.get(id).getX();
		pos[1] = units.get(id).getY();

		return pos;
	}

	@Override
	public int getMapHeight() throws RemoteException {
		return BattleField.MAP_HEIGHT;
	}

	@Override
	public int getMapWidth() throws RemoteException {
		return BattleField.MAP_WIDTH;
	}

	@Override
	public UnitType getType(int x, int y) throws RemoteException {
		if (getUnit(x, y) instanceof Player)
			return UnitType.player;
		else if (getUnit(x, y) instanceof Dragon)
			return UnitType.dragon;
		else
			return UnitType.undefined;

	}

	@Override
	public void ping() throws RemoteException {

	}

	@Override
	public void setBackupAddress(String address) throws RemoteException {
		if (isBackupServerAlive(address)) {
			backupAddress = address;
			updateBackupServerForHelpers();
			log("Backup address set to: " + address);
		} else {
			log("Backup address not set, because server is not online");
		}
	}

	private boolean isBackupServerAlive(String address) {
		try {
			IBattleField RMIServer = (IBattleField) Naming.lookup("rmi://"
					+ address);
			RMIServer.ping();
			return true;
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			log("Backup server not available");
			return false;
		}
	}

	private void updateBackupServerForHelpers() {
		String backupAddress = this.backupAddress.split("/")[0];

		Iterator<String> iterator = helpers.keySet().iterator();

		while (iterator.hasNext()) {
			updateBackupServerForHelper(iterator.next(), backupAddress);
		}
	}

	private void updateBackupServerForHelper(String helperServer,
			String backupAddress) {
		try {
			IRunner RMIServer = (IRunner) Naming.lookup("rmi://"
					+ helpers.get(helperServer) + "/" + helperServer);
			RMIServer.setBackupServerLocation(backupAddress);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	private void sendUpdate() {
		if (backupAddress == null) {
			return;
		}

		Snapshot snap = new Snapshot();
		snap.setLastUnitID(lastUnitID);

		if (mapChanged)
			snap.setMap(map);

		if (unitsChanged)
			snap.setUnits(units);

		if (helpersChanged) {
			snap.setHelpers(helpers);
		}

		if (dragonsChanged) {
			snap.setDragons(dragons);
		}

		IBattleField RMIServer = null;
		try {
			RMIServer = (IBattleField) Naming.lookup("rmi://" + backupAddress);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			log("Backup server is not online");
			backupAddress = null;
			return;
		}

		totalNumberOfAttemptedBackupUpdates += 1;

		try {
			boolean updated = RMIServer.updateBackup(snap);
			if (updated) {
				unitsChanged = false;
				mapChanged = false;
				helpersChanged = false;
				dragonsChanged = false;
				totalNumberOfSuccessfullBackupUpdates += 1;
			}
		} catch (RemoteException e) {
			totalNumberOfFailedBackupUpdates += 1;
			e.printStackTrace();
		}

	}

	private void metricsRunner() {
		Runnable myRunnable = new Runnable() {

			public void run() {
				log("Metrics Runner running");

				while (true) {
					try {
						Thread.sleep(5000);

						if (units.keySet().size() == 0) {
							saveMetrics();
							break;
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}

	private void dragonRunner() {
		Runnable myRunnable = new Runnable() {

			public void run() {
				log("DragonRunner running");
				int dragons = numberOfDragons();
				while (true) {
					try {
						Thread.sleep(500);
						dragons = numberOfDragons();
						if (dragons < NR_OF_DRAGONS) {
							spawnDragon();
						} else {
							break;
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}

	private int numberOfDragons() {
		return dragons.size();
	}

	private void spawnDragon() {
		String id = "d_" + lastUnitID;
		DragonController dragonController = new DragonController(id, myAddress);
		lastUnitID += 1;
		int[] pos = getAvailablePosition();
		this.spawnUnit(id, dragonController.getDragon(), pos[0], pos[1]);
		this.dragons.put(id, dragonController.getDragon());
		dragonsChanged = true;
	}

	private void initBackupService() {
		Runnable myRunnable = new Runnable() {

			public void run() {
				log("Backup service running");
				while (!isBackup) {
					try {
						Thread.sleep(5000);
						if (backupAddress != null) {
							sendUpdate();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				log("Backup service stopped");
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}

	@Override
	public boolean updateBackup(Snapshot snapshot) throws RemoteException {
		this.lastUnitID = snapshot.getLastUnitID();

		if (snapshot.getMap() != null) {
			this.map = snapshot.getMap();
		}

		if (snapshot.getUnits() != null) {
			this.units = snapshot.getUnits();
			log("units: " + units.size());
		}

		if (snapshot.getHelpers() != null) {
			helpers = snapshot.getHelpers();
			log("Helpers updated: " + helpers.size());
		}

		if (snapshot.getDragons() != null) {
			dragons = snapshot.getDragons();
			log("Dragons updated: " + dragons.size());
		}

		return true;
	}

	private void initHelperChecker() {
		Runnable myRunnable = new Runnable() {

			public void run() {
				log("Helper checker running");
				while (!isBackup) {
					try {
						Thread.sleep(5000);
						checkHelpers();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				log("Helper checker stopped");
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}

	@Override
	public boolean promoteBackupToMain() throws RemoteException {
		if (!isBackup) {
			isBackup = false;
			initHelperChecker();
			initBackupService();
			reInitDragons();
			log("Backup server promoted to main server");
			return true;
		} else {
			return false;
		}
	}

	private void reInitDragons() {
		log("DragonRunner running again");
		Iterator<Entry<String, Unit>> dragons = this.dragons.entrySet()
				.iterator();

		while (dragons.hasNext()) {
			Dragon current = (Dragon) dragons.next();
			current.serverAddress = myAddress;
			new DragonController(current);
		}
		while (numberOfDragons() < NR_OF_DRAGONS) {
			spawnDragon();
		}
	}

	public boolean isBackup() {
		return isBackup;
	}

	public void setBackup(boolean isBackup) {
		this.isBackup = isBackup;
	}

	private void log(String text) {
		Log.log(myAddress, text);
	}

	public void saveMetrics() {
		this.endTime = System.currentTimeMillis();
		logMetric("Total Messages Send: " + totalMessagesSend);
		logMetric("Total Messages Received: " + totalMessagesReceived);
		logMetric("Total Messages Failed To Send: " + totalMessagesFailedToSend);
		logMetric("Total Messages Failed To Receive: "
				+ totalMessagesFailedToReceive);
		logMetric("Total Number of disconnected helpers: "
				+ totalNumberOfDisconnectedHelpers);

		logMetric("Total Attempted backup updates: "
				+ totalNumberOfAttemptedBackupUpdates);
		logMetric("Total Successfull backup updates: "
				+ totalNumberOfSuccessfullBackupUpdates);
		logMetric("Total Failed backup updates: "
				+ totalNumberOfFailedBackupUpdates);

		logMetric("Runtime: "
				+ common.Common.getFormatedTime(endTime - startTime));

		saveHelperMetrics();
	}

	private void saveHelperMetrics() {
		Iterator<String> iterator = helpers.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			logMetric("H: " + helpers.get(key) + "/" + key);
			logMetric(this.getHelperMetrics(key));
		}
	}

	private String getHelperMetrics(String helper) {
		String res = "";

		IRunner RMIServer = null;
		String urlServer = new String("rmi://" + helpers.get(helper) + "/"
				+ helper);

		try {
			RMIServer = (IRunner) Naming.lookup(urlServer);
			res = RMIServer.getMetrics();
			return res;
		} catch (Exception e) {
			// e.printStackTrace();
		}

		return res;
	}

	private void logMetric(String text) {
		if (firstWrite) {
			Log.logMetric(myAddress, "\n ################ \n");
			firstWrite = false;
		}
		Log.logMetric(myAddress, text);
	}
}
