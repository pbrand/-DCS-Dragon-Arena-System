package server.helper;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;

import common.Enums.Direction;
import common.Enums.UnitType;
import common.IPlayerController;
import common.IRunner;
import common.Log;
import common.Message;
import common.MessageRequest;

public class PlayerRunner implements IRunner {

	private String myAddress;
	private String battleServerLocation;
	private String battleServer;
	private String backupBattleServerLocation;
	private HashMap<String, String> clients;
	private int battleFieldMapHeight = 0;
	private int battleFieldMapWidth = 0;

	public PlayerRunner(String battleServerLocation, String battleServer) {
		this.clients = new HashMap<String, String>();
		this.battleServerLocation = battleServerLocation;
		this.battleServer = battleServer;
		this.getBattleFieldInfo();
	}

	@Override
	public void sendMessageToServer(Message msg) {
		// TODO Auto-generated method stub
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);

		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			RMIServer.receiveMessage(msg);

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			if (changeBackupToMainServer()) {
				sendMessageToServer(msg);
			} else {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sendMessageToClient(Message msg) {
		// TODO Auto-generated method stub
		IPlayerController RMIServer = null;
		String urlServer = new String("rmi://"
				+ clients.get(msg.getRecipient()) + "/" + msg.getRecipient());

		// Bind to RMIServer
		try {
			RMIServer = (IPlayerController) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			RMIServer.receiveMessage(msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		Log.log(myAddress, "Received: " + msg);
		switch (msg.getRequest()) {
		case MessageRequest.spawnUnit:
			try {
				clients.put(msg.getSender(), RemoteServer.getClientHost() + ":"
						+ msg.getMiddlemanPort());
			} catch (ServerNotActiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendMessageToServer(msg);
			break;
		case MessageRequest.disconnectUnit:
			disconnectUser(msg.getSender());
			clients.remove(msg.getSender());
			break;
		case MessageRequest.moveUnit:
			moveUnit(msg.getSender(), (Direction) msg.get("direction"));
			break;
		case MessageRequest.spawnAck:
			sendMessageToClient(msg);
			// case MessageRequest.getBattleFieldInfo:
			// this.battleFieldMapWidth = (int) msg.get("mapWidth");
			// this.battleFieldMapHeight = (int) msg.get("mapHeight");
		default:
			break;
		}
	}

	private void disconnectUser(String id) {
		// Send disconnect message to the main server.
		Message msg = new Message(battleServer);
		msg.setRequest(MessageRequest.disconnectUnit);
		msg.put("playerID", id);
		this.sendMessageToServer(msg);
	}

	/*
	 * Retrieve information about the battlefield from the main server. (We need
	 * MapHeight and MapWidth)
	 */
	private void getBattleFieldInfo() {
		// Message msg = new Message(battleServer);
		// msg.setRequest(MessageRequest.getBattleFieldInfo);
		// this.sendMessageToServer(msg);

		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);
		// Bind to RMIServer
		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			this.battleFieldMapWidth = RMIServer.getMapWidth();
			this.battleFieldMapHeight = RMIServer.getMapHeight();

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			if (changeBackupToMainServer()) {
				getBattleFieldInfo();
			} else {
				e.printStackTrace();
			}
		}
	}

	private void moveUnit(String id, Direction direction) {
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);
		int[] pos = null;
		// Bind to RMIServer
		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			pos = RMIServer.getPosition(id);
			int targetX = pos[0];
			int targetY = pos[1];
			switch (direction) {
			case up:
				if (pos[1] <= 0)
					// The player was at the edge of the map, so he can't move
					// north and there are no units there
					break;
				targetY = targetY - 1;
				break;
			case down:
				if (pos[1] >= this.battleFieldMapHeight - 1)
					// The player was at the edge of the map, so he can't move
					// south and there are no units there
					break;
				targetY = targetY + 1;
				break;
			case left:
				if (pos[0] <= 0)
					// The player was at the edge of the map, so he can't move
					// west and there are no units there
					break;
				targetX = targetX - 1;
				break;
			case right:
				if (pos[0] >= this.battleFieldMapWidth - 1)
					// The player was at the edge of the map, so he can't move
					// east and there are no units there
					break;
				targetX = targetX + 1;
				break;
			}
			Log.log(myAddress, "Position of player: " + targetX + " " + targetY);
			// Get what unit lies in the target square
			UnitType adjacentUnitType = RMIServer.getType(targetX, targetY);
			Message msg;
			switch (adjacentUnitType) {
			case undefined:
				// There is no unit in the square. Move the player to this
				// square
				msg = new Message(battleServer);
				msg.setRequest(MessageRequest.moveUnit);
				msg.put("playerID", id);
				msg.put("x", targetX);
				msg.put("y", targetY);
				this.sendMessageToServer(msg);
				break;
			case player:
				// There is a player in the square, attempt a healing
				//this.healDamage(id, targetX, targetY);
				msg = new Message(battleServer);
				msg.setRequest(MessageRequest.healDamage);
				msg.put("playerID", id);
				msg.put("x", targetX);
				msg.put("y", targetY);
				this.sendMessageToServer(msg);
				break;
			case dragon:
				// There is a dragon in the square, attempt a dragon slaying
				//this.dealDamage(id, targetX, targetY);
				msg = new Message(battleServer);
				msg.setRequest(MessageRequest.dealDamage);
				msg.put("playerID", id);
				msg.put("x", targetX);
				msg.put("y", targetY);
				this.sendMessageToServer(msg);
				break;
			}
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			if (changeBackupToMainServer()) {
				moveUnit(id, direction);
			} else {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void ping() throws RemoteException {
		// Log.log(myAddress, "Still alive");
	}

	@Override
	public void registerWithServer(String player, String address)
			throws RemoteException {
		this.clients.put(player, address);

	}

	private synchronized boolean changeBackupToMainServer() {
		if (notifyBackupToBecomeMain()) {
			this.battleServerLocation = new String(
					this.backupBattleServerLocation);
			this.backupBattleServerLocation = null;
			Log.log(myAddress, "Battle is now at: " + battleServerLocation);
			return true;
		}

		return false;
	}

	private boolean notifyBackupToBecomeMain() {
		if (backupBattleServerLocation == null) {
			return false;
		}
		try {
			IBattleField RMIServer = (IBattleField) Naming.lookup("rmi://"
					+ backupBattleServerLocation + "/" + battleServer);
			RMIServer.promoteBackupToMain();
			return true;
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean setBackupServerLocation(String serverLocation)
			throws RemoteException {
		this.backupBattleServerLocation = serverLocation;
		Log.log(myAddress, "Backup server updated with: " + serverLocation);
		return true;
	}

	public void setMyAddress(String myAddress) {
		this.myAddress = myAddress;
	}
}
