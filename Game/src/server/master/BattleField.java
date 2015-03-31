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
import java.util.Random;

import server.helper.IBattleField;

import common.Enums.UnitType;
import common.IRunner;
import common.Log;
import common.Message;
import common.MessageRequest;

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
	
	//private static final Logger logger = LogManager.getLogger(BattleField.class);

	// private String serverLocation;

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
			

			/*for(int i = 0; i < NR_OF_DRAGONS; i++) {
				Unit dragon = new Dragon();
				String id = "Dragon"+i;
				int[] pos = getAvailablePosition();
				this.spawnUnit(id, dragon, pos[0], pos[1]);
			}*/
			dragonRunner();
		}
		
		setMyAddress(address);
		log("Battlefield created");
		if (!isBackup) {
			initHelperChecker();
			initBackupService();
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
		// TODO Auto-generated method stub
		IRunner RMIServer = null;
		String clienthost = null;
		try {
			clienthost = RemoteServer.getClientHost();
		} catch (ServerNotActiveException e1) {
			// TODO Auto-generated catch block
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

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void receiveMessage(Message msg) {
		// TODO Auto-generated method stub
		String from = msg.getSender();
		String request = msg.getRequest();

		log("Message received from: " + msg.getSender()
				+ ", request: " + request);

		Message reply = null;
		Unit unit;
		switch (request) {
		case MessageRequest.spawnUnit: {
			log("Spawning: " + msg.getSender());
			Unit player = new Player();
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
			this.putUnit((Unit) msg.get("unit"), (Integer) msg.get("x"),
					(Integer) msg.get("y"));
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
			int x = (Integer) msg.get("x");
			int y = (Integer) msg.get("y");
			unit = this.getUnit(x, y);
			if (unit != null) {
				//unit.adjustHitPoints(-(Integer) msg.get("damage"));
				unit.adjustHitPoints(-units.get((String) msg.get("playerID")).getAttackPoints());
				unitsChanged = true;
			}
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			break;
		}
		case MessageRequest.healDamage: {
			int x = (Integer) msg.get("x");
			int y = (Integer) msg.get("y");
			unit = this.getUnit(x, y);
			if (unit != null) {	
				//unit.adjustHitPoints((Integer) msg.get("healed"));
				unit.adjustHitPoints(units.get((String) msg.get("playerID")).getAttackPoints());
				unitsChanged = true;
			}
			/*
			 * Copy the id of the message so that the unit knows what message
			 * the battlefield responded to.
			 */
			break;
		}
		case MessageRequest.moveUnit: {

			this.moveUnit(units.get((String) msg.get("playerID")),
					(int) msg.get("x"), (int) msg.get("y"));

			break;
		}
		case MessageRequest.removeUnit: {
			this.removeUnit((Integer) msg.get("x"), (Integer) msg.get("y"));
			break;
		}
		case MessageRequest.disconnectUnit: {
			this.disconnectUnit((String) msg.get("playerID"));
			break;
		}
		case MessageRequest.getTargets: {
			String id = (String) msg.get("playerID");
			int x = units.get(id).getX();
			int y = units.get(id).getY();
			
			reply = new Message(from);		
			if(getUnit(x,y) instanceof Player) {
				reply.setMiddleman((String) msg.getMiddleman());
				reply.setMiddlemanPort((int) msg.getMiddlemanPort()); 
				reply.setRequest(MessageRequest.returnTargets);
				reply = getPlayerTargetsMessage(x,y, reply);
			}
			else if(getUnit(x,y) instanceof Dragon) {
				reply = getDragonTargetsMessage(x,y, reply);
			}
			break;
			
			
		}
		
		}

		if (reply != null) {
			sendMessage(reply);
		}

	}

	private Message getPlayerTargetsMessage(int x, int y, Message reply) {
		ArrayList<Unit> players = new ArrayList<Unit>();
		ArrayList<Unit> dragons = new ArrayList<Unit>();

		int closestEnemyDistance = Integer.MAX_VALUE;
		Unit closestDragon = null;
		// We assume that HEAL_RANGE >= ATTACK_RANGE will always apply.
		int minX = x - Player.HEAL_RANGE >= 0 ? x - Player.HEAL_RANGE : 0;
		int maxX = x + Player.HEAL_RANGE <= BattleField.MAP_WIDTH ? x + Player.HEAL_RANGE : BattleField.MAP_WIDTH;
		int minY = y - Player.HEAL_RANGE >= 0 ? y - Player.HEAL_RANGE : 0;
		int maxY = y + Player.HEAL_RANGE <= BattleField.MAP_HEIGHT ? y + Player.HEAL_RANGE : BattleField.MAP_HEIGHT;
		for(int i = minX; i < maxX; i++) {
			for(int j = minY; j < maxY; j++) {
				if(i != x || j != y) {
					int distance = Math.abs(x-i) + Math.abs(y-j);
					
					if(this.getUnit(i, j) instanceof Player && (distance <= Player.HEAL_RANGE)) {
						players.add(getUnit(i,j));
					}
					else if(this.getUnit(i, j) instanceof Dragon) {
						if(distance <= Player.ATTACK_RANGE) {
							dragons.add(getUnit(i,j));
						}
						if(distance < closestEnemyDistance) {
							closestEnemyDistance = distance;
							closestDragon = getUnit(i,j);
						}
					}												
				}
			}
		}
		
		reply.put("players", players);
		reply.put("dragons", dragons);
		reply.put("playerX", x);
		reply.put("playerY", y);
		reply.put("enemyX", closestDragon.getX());
		reply.put("enemyY", closestDragon.getY());
		return reply;
	}

	private Message getDragonTargetsMessage(int x, int y, Message reply) {
		ArrayList<Unit> players = new ArrayList<Unit>();
		int minX = x - Dragon.ATTACK_RANGE >= 0 ? x - Dragon.ATTACK_RANGE : 0;
		int maxX = x + Dragon.ATTACK_RANGE <= BattleField.MAP_WIDTH ? x + Dragon.ATTACK_RANGE : BattleField.MAP_WIDTH;
		int minY = y - Dragon.ATTACK_RANGE >= 0 ? y - Dragon.ATTACK_RANGE : 0;
		int maxY = y + Dragon.ATTACK_RANGE <= BattleField.MAP_HEIGHT ? y + Dragon.ATTACK_RANGE : BattleField.MAP_HEIGHT;
		for(int i = minX; i < maxX; i++) {
			for(int j = minY; j < maxY; j++) {
				if(i != x || j != y) {
					int distance = Math.abs(x-i) + Math.abs(y-j);
					if(this.getUnit(i, j) instanceof Player) {
						if(distance <= Dragon.ATTACK_RANGE) {
							players.add(getUnit(i,j));
						}
					}												
				}
			}
		}
		
		reply.put("players", players);
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
	private synchronized void removeUnit(int x, int y) {
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null)
			return; // There was no unit here to remove
		map[x][y] = null;
		unitToRemove.disconnect();
		units.remove(unitToRemove);
		unitsChanged = true;
		mapChanged = true;
	}
	
	private synchronized void removeDragon(int x, int y) {
		Unit unitToRemove = this.getUnit(x, y);
		if (unitToRemove == null || !(unitToRemove instanceof Dragon))
			return; 
		map[x][y] = null;
		units.remove(unitToRemove);
		unitsChanged = true;
		mapChanged = true;
	}
	
	
	private synchronized void disconnectUnit(String id) {
		Unit unitToDisconnect = units.get(id);
		if (unitToDisconnect == null) {
			log("Player with id: "+id+" could not disconnect, no unit found");
			return; // There was no unit here to remove
		}
		int x = unitToDisconnect.getX();
		int y = unitToDisconnect.getY();
		map[x][y] = null;
		units.remove(id);
		log("Player with id: "+id+" has disconnected.");

		unitsChanged = true;
		mapChanged = true;
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
	public Unit getUnit(int x, int y) {
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
		backupAddress = address;
		updateBackupServerForHelpers();
		log("Backup address set to: " + address);
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

		IBattleField RMIServer = null;
		try {
			RMIServer = (IBattleField) Naming.lookup("rmi://" + backupAddress);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			log("Backup server is not online");
			backupAddress = null;
			return;
		}
		
		try {
			boolean updated = RMIServer.updateBackup(snap);
			if (updated) {
				unitsChanged = false;
				mapChanged = false;
				helpersChanged = false;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	private void dragonRunner() {
		Runnable myRunnable = new Runnable() {

			public void run() {
				log("DragonRunner running");
				int dragons = numberOfDragons();
				int players = numberOfPlayers();
				while (true) {
					try {
						Thread.sleep(500);	
						dragons = numberOfDragons();
						players = numberOfPlayers();
						if (dragons < NR_OF_DRAGONS) {							
							spawnDragon();							
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
	
	private int numberOfPlayers() {		
		return units.size();
	}
	
	private int numberOfDragons() {
		return dragons.size();
	}
	
	private void spawnDragon() {
		Unit dragon = new Dragon();
		String id = "Dragon" + lastUnitID;
		lastUnitID += 1;
		int[] pos = getAvailablePosition();
		this.spawnUnit(id, dragon, pos[0], pos[1]);
		this.dragons.put(id, dragon);
	}
	
	private void stopRandomDragon() {
		if (numberOfDragons() > 0) {
			Random rnd = new Random();
			int i = (int) rnd.nextInt(dragons.size());
			List<String> list = new ArrayList<String>(dragons.keySet());
			Unit dragon = dragons.get(list.get(i));
			this.removeDragon(dragon.getX(), dragon.getY());
		}
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
		isBackup = false;
		log("Backup server promoted to main server");
		initHelperChecker();
		initBackupService();
		return true;
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
}
