package server.helper;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Iterator;

import common.Enums.Direction;
import common.Enums.UnitType;
import common.IBattleField;
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
	
	/**
	 * Metrics
	 */
	private int totalMessagesSend;
	private int totalMessagesSendToServer;
	private int totalMessagesSendToClient;
	private int totalMessagesReceived;
	private int totalMessagesFailedToSend;
	private int totalMessagesFailedSendToServer;
	private int totalMessagesFailedSendToClient;
	private int totalMessagesFailedToReceive;
	private int totalChangeRequestsOfMainServer;
	private int totalNumberOfMaxClients;
	
	private long startTime;
	private long endTime;

	public PlayerRunner(String battleServerLocation, String battleServer) {
		this.startTime = System.currentTimeMillis();
		this.clients = new HashMap<String, String>();
		this.battleServerLocation = battleServerLocation;
		this.battleServer = battleServer;
		this.getBattleFieldInfo();
	}

	@Override
	public void sendMessageToServer(Message msg) {
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);

		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			RMIServer.receiveMessage(msg);
			totalMessagesSend += 1;
			totalMessagesSendToServer += 1;
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			if (changeBackupToMainServer()) {
				sendMessageToServer(msg);
			} else {
				e.printStackTrace();
				totalMessagesFailedSendToServer += 1;
			}
		}
	}

	@Override
	public void sendMessageToClient(Message msg) {
		IPlayerController RMIServer = null;
		String urlServer = new String("rmi://"
				+ clients.get(msg.getRecipient()) + "/" + msg.getRecipient());

		// Bind to RMIServer
		try {
			RMIServer = (IPlayerController) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			RMIServer.receiveMessage(msg);
			totalMessagesSend += 1;
			totalMessagesSendToClient += 1;
		} catch (Exception e) {
			e.printStackTrace();
			totalMessagesFailedToSend += 1;
			totalMessagesFailedSendToClient += 1;
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		Log.log(myAddress, "Received: " + msg);
		totalMessagesReceived += 1;
		switch (msg.getRequest()) {
		case MessageRequest.spawnUnit:
			try {
				clients.put(msg.getSender(), RemoteServer.getClientHost() + ":"
						+ msg.getMiddlemanPort());
				totalNumberOfMaxClients += 1;
			} catch (ServerNotActiveException e) {
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
			break;
		case MessageRequest.getTargets:
			msg.put("unitID", msg.getSender());
			sendMessageToServer(msg);
			break;
		case MessageRequest.returnTargets:
			sendMessageToClient(msg);
			break;
		case MessageRequest.healDamage:
			msg.put("unitID", msg.getSender());
			sendMessageToServer(msg);
			break;
		case MessageRequest.dealDamage:
			msg.put("unitID", msg.getSender());
			sendMessageToServer(msg);
			break;
		case MessageRequest.gameOver:
			sendMessageToClient(msg);
			break;
		default:
			break;
		}
	}

	private void disconnectUser(String id) {
		// Send disconnect message to the main server.
		Message msg = new Message(battleServer);
		msg.setRequest(MessageRequest.disconnectUnit);
		msg.put("unitID", id);
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
			totalMessagesReceived += 1;
			this.battleFieldMapHeight = RMIServer.getMapHeight();
			totalMessagesReceived += 1;

		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			if (changeBackupToMainServer()) {
				getBattleFieldInfo();
			} else {
				e.printStackTrace();
				totalMessagesFailedToReceive += 1;
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
			totalMessagesReceived += 1;
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
				msg.put("unitID", id);
				msg.put("x", targetX);
				msg.put("y", targetY);
				this.sendMessageToServer(msg);
				break;
			default:
				break;
			}
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			if (changeBackupToMainServer()) {
				moveUnit(id, direction);
			} else {
				e.printStackTrace();
				totalMessagesFailedToReceive += 1;
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
		totalNumberOfMaxClients += 1;

	}

	private synchronized boolean changeBackupToMainServer() {
		totalChangeRequestsOfMainServer += 1;
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
	
	public String getMetrics() {
		this.endTime = System.currentTimeMillis();
		String res = "\n";
		res += ("[H] Total Number of Clients ever Connected: " + totalNumberOfMaxClients + "\n");
		res += ("[H] Total Number of Clients at the end: " + clients.size() + "\n");
		res += ("[H] Total Messages Send: " + totalMessagesSend + "\n");
		res += ("[H] Total Messages Send to Client: " + totalMessagesSendToClient + "\n");
		res += ("[H] Total Messages Send to Server: " + totalMessagesSendToServer + "\n");
		res += ("[H] Total Messages Received: " + totalMessagesReceived + "\n");
		res += ("[H] Total Messages Failed To Send: " + totalMessagesFailedToSend + "\n" );
		res += ("[H] Total Messages Failed To Send to Client: " + totalMessagesFailedSendToClient + "\n" );
		res += ("[H] Total Messages Failed To Send to Server: " + totalMessagesFailedSendToServer + "\n" );
		res += ("[H] Total Messages Failed To Receive: " + totalMessagesFailedToReceive + "\n" );
		res += ("[H] Total CHangerequests of Main Server: " + totalChangeRequestsOfMainServer + "\n" );
		res += ("[H] Runtime: " + common.Common.getFormatedTime(endTime - startTime) + "\n");
		
		res += "\n" + getClientMetrics();
		
		return res;
	}
	
	private String getClientMetrics() {
		String res = "";
		Iterator<String> iterator = clients.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			res += getMetricOfClient(key);
		}
		return res;
	}
	
	private String getMetricOfClient(String client) {
		IPlayerController RMIServer;
		try {
			RMIServer = (IPlayerController) Naming.lookup("rmi://" + this.myAddress.split("/")[0] + "/" + client);
			return RMIServer.getMetrics();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		return "";
	}

}
