package client;

import java.rmi.Naming;

import server.helper.IBattleField;
import server.master.Player;
import server.master.Unit;
import common.IPlayerController;
import common.Message;
import common.MessageRequest;

public class PlayerController implements IPlayerController {
	
	private String playerID;
	private static String battleServerLocation = "192.168.56.1:29242";
	private static String battleServer = "main_battle_server";
	private Player player;
	private int myPort;
	
	public PlayerController(String playerID, int myPort) {
		// TODO Auto-generated constructor stub
		this.player = new Player();
		this.playerID = playerID;
		this.myPort = myPort;
	}
	
	public void spawnPlayer() {
		Message spawn = createSpawnMessage(player, 2, 2);
		sendMessage(spawn);
	}
	
	private Message createSpawnMessage(Unit unit, int x, int y) {
		String recipient = battleServer;
		Message msg = new Message(recipient);
		msg.setSender(playerID);
		msg.setSendersPort(myPort);
		msg.setRequest(MessageRequest.spawnUnit);
		msg.put("unit", unit);
		msg.put("x", x);
		msg.put("y", y);
		
		return msg;
	}

	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/" + msg.getRecipient());

		// Bind to RMIServer
		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
			RMIServer.receiveMessage(msg);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		// TODO Auto-generated method stub
		System.out.println("PlayerController: received message");
		System.out.println("PlayerController: msg: " + msg.toString());
		// Update somethings
	}

}
