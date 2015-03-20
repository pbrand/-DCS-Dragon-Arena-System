package server.helper;

import java.rmi.Naming;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;

import server.master.Unit;
import common.Enums.Direction;
import common.IPlayerController;
import common.IRunner;
import common.Message;
import common.MessageRequest;

public class PlayerRunner implements IRunner {

	private static String battleServerLocation = "145.94.181.115:29242";
	private static String battleServer = "main_battle_server";
	private HashMap<String,String> clients;
	private int battleFieldMapHeight;
	private int battleFieldMapWidth;
	
	public PlayerRunner() {
		clients = new HashMap<String,String>();
	}
	
	@Override
	public void sendMessageToServer(Message msg) {
		// TODO Auto-generated method stub
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/" + battleServer);

		// Bind to RMIServer
		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
            RMIServer.receiveMessage(msg);

		} catch(Exception e) {
            e.printStackTrace();
		}
	}
	
	@Override
	public void sendMessageToClient(Message msg) {
		// TODO Auto-generated method stub
		IPlayerController RMIServer = null;
		String urlServer = new String("rmi://" + clients.get(msg.getRecipient()) + "/" + msg.getRecipient());

		// Bind to RMIServer
		try {
			RMIServer = (IPlayerController) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
            RMIServer.receiveMessage(msg);

		} catch(Exception e) {
            e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		System.out.println(msg);
		switch(msg.getRequest()) {
			case MessageRequest.spawnUnit:
			try {
				clients.put(msg.getSender(),RemoteServer.getClientHost() + ":" + msg.getSendersPort());
			} catch (ServerNotActiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				sendMessageToServer(msg);
				break;
			case MessageRequest.moveUnit:
				moveUnit((Direction) msg.get("direction"));
				break;
			case MessageRequest.spawnAck:
				sendMessageToClient(msg);
			default:
				break;
		}
	}
	
	/*
	 * Retrieve information about the battlefield from the main server. (We need MapHeight and MapWidth)
	 */
	private void getBattleFieldInfo() {
		// TODO Auto-generated method stub
		
	}
	
	private void moveUnit(Direction direction) {
		int targetX = this.getX();
		int targetY = this.getY();
		
		switch (direction) {
		case up:
			if (this.getY() <= 0)
				// The player was at the edge of the map, so he can't move north and there are no units there
				break;
			
			targetY = targetY - 1;
			break;
		case down:
			if (this.getY() >= this.battleFieldMapHeight - 1)
				// The player was at the edge of the map, so he can't move south and there are no units there
				break;
			
			targetY = targetY + 1;
			break;
		case left:
			if (this.getX() <= 0)
				// The player was at the edge of the map, so he can't move west and there are no units there
				break;

			targetX = targetX - 1;
			break;
		case right:
			if (this.getX() >= this.battleFieldMapWidth - 1)
				// The player was at the edge of the map, so he can't move east and there are no units there
				break;

			targetX = targetX + 1;
			break;
		}
	}

	/*
	 * Retrieve X position of the player from the battlefield
	 */
	private int getX() {
		return 0;
	}
	
	/*
	 * Retrieve Y position of the player from the battlefield
	 */
	private int getY() {
		return 0;
	}
}
