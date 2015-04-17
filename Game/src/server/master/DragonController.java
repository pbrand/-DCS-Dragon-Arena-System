package server.master;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import common.IBattleField;
import common.IDragonController;
import common.IUnit;
import common.Log;
import common.Message;
import common.MessageRequest;

public class DragonController implements IDragonController, Runnable {

	private int timeBetweenTurns;
	private boolean running;
	private ArrayList<IUnit> players;
	private boolean targets;
	private Dragon dragon;

	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;

	public DragonController(String id, String serverAddress) {
		this.dragon = new Dragon(id, serverAddress);

		/* Create a random delay */
		timeBetweenTurns = (int) (Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS))
				+ MIN_TIME_BETWEEN_TURNS;
		bindController(serverAddress);
	}

	public DragonController(Dragon dragon) {
		this.dragon = dragon;
		timeBetweenTurns = (int) (Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS))
				+ MIN_TIME_BETWEEN_TURNS;
		bindController(dragon.serverAddress);
	}

	private void bindController(String serverAddress) {
		IDragonController stub = null;

		try {
			stub = (IDragonController) UnicastRemoteObject
					.exportObject(this, 0);
			String address = serverAddress.split("/")[0];
			Registry reg = LocateRegistry.getRegistry(address.split(":")[0],
					Integer.parseInt(address.split(":")[1]));

			reg.rebind(dragon.unitID, stub);

			System.out.println("Dragon running, Dragon ID: " + dragon.unitID
					+ ", reg: " + reg.toString());

			Thread runnerThread = new Thread(this);
			runnerThread.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Roleplay the dragon. Make the dragon act once a while, only stopping when
	 * the dragon is actually dead or the program has halted.
	 * 
	 * It checks if an enemy is near and, if so, it attacks that specific enemy.
	 */
	@SuppressWarnings("static-access")
	public void run() {
		this.running = true;

		while (/* GameState.getRunningState() && */this.running) {
			try {
				/* Sleep while the dragon is considering its next move */
				Thread.currentThread().sleep(
						(int) (timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the dragon runs out of hitpoints */
				// if (getHitPoints() <= 0) {
				// this.disconnect();
				// this.running = false;
				// break;
				// }

				// Decide what players are near
				this.requestTargets();
				// Wait a while if the request from the server is not yet
				// answered.
				int attempts = 0;
				while (!this.targets) {
					if (attempts < 10) {
						Thread.sleep(5);
					} else {
						break;
					}
					attempts++;
				}

				// Pick a random player to attack
				if (players == null || players.size() == 0) {
					continue; // There are no players to attack
				}
				IUnit playerToAttack = players
						.get((int) (Math.random() * players.size()));

				// Attack the player
				this.dealDamage(playerToAttack.getX(), playerToAttack.getY());

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(Message msg) {
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + dragon.serverAddress);

		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			RMIServer.receiveMessage(msg);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void receiveMessage(Message msg) {
		Log.log(dragon.unitID, "Dragon: received message");
		Log.log(dragon.unitID, "Dragon: msg: " + msg.toString());
		// Update somethings
		switch (msg.getRequest()) {
		case MessageRequest.returnTargets:
			this.players = (ArrayList<IUnit>) msg.get("players");
			this.targets = true;
			break;
		case MessageRequest.gameOver:
			this.running = false;
			this.disconnect();
			break;
		default:
			break;
		}
	}

	private Message createMessage() {
		Message msg = new Message(dragon.serverAddress.split("/")[1]);
		msg.setSender(dragon.unitID);

		return msg;
	}

	public void requestTargets() {
		Message targets = createRequestTargetsMessage();
		sendMessage(targets);
	}

	private Message createRequestTargetsMessage() {
		Message msg = createMessage();
		msg.setRequest(MessageRequest.getTargets);
		msg.put("unitID", dragon.unitID);
		return msg;
	}

	private void dealDamage(int x, int y) {
		Message attack = createDealDamageMessage(x, y);
		Log.log(dragon.unitID, attack.toString());
		sendMessage(attack);
	}

	private Message createDealDamageMessage(int x, int y) {
		Message msg = createMessage();
		msg.setRequest(MessageRequest.dealDamage);
		msg.put("unitID", dragon.unitID);
		msg.put("x", x);
		msg.put("y", y);
		return msg;
	}

	public void disconnect() {
		Message disconnect = createDisconnectMessage();
		Log.log(dragon.unitID, disconnect.toString());
		sendMessage(disconnect);
	}

	private Message createDisconnectMessage() {
		Message msg = createMessage();
		msg.setRequest(MessageRequest.disconnectUnit);
		msg.put("unitID", dragon.unitID);
		return msg;
	}

	public Dragon getDragon() {
		return dragon;
	}

	@Override
	public String getMetrics() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
