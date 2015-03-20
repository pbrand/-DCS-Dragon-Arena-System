package client;

import java.rmi.Naming;
import java.rmi.RemoteException;

import common.Enums.Direction;
import common.IPlayerController;
import common.IRunner;
import common.Message;
import common.MessageRequest;

public class PlayerController implements IPlayerController {
	
	private String playerID;
	private static String battleServerLocation = "145.94.181.115:29242";
	private static String battleHelperLocation = "145.94.181.115:6115";
	private static String battleServer = "main_battle_server";
	private static String battleHelper = "helper_battle_server";
	private int myPort;
	
	private boolean running = false;
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;

	
	public PlayerController(String playerID, int myPort) {
		// TODO Auto-generated constructor stub
		this.playerID = playerID;
		this.myPort = myPort;
	}
	
	public void run() {
		Direction direction;
		this.running = true;

		int i = 0;
		// TODO This is an infinite loop until it receives a message that it should stop. That's tricky.
		while(/*GameState.getRunningState() &&*/ this.running) {
			i += 1;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ( i > 4 ) {
				break;
			}
			
			
			/* Sleep while the player is considering its next move */
			// Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));
			
			/* Stop if the player runs out of hitpoints */
			// Receive a message here?? (if hitpoints <= 0) -> then set running to false so that the mainloop quits.
			
			// Randomly choose one of the four wind directions to move to if there are no units present
			direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];
			movePlayer(direction);
		}
	}
	
	public void spawnPlayer() {
		Message spawn = createSpawnMessage();
		System.out.println(spawn);
		sendMessage(spawn);
	}
	
	private Message createSpawnMessage() {
		String recipient = battleServer;
		Message msg = new Message(recipient);
		msg.setSender(playerID);
		msg.setSendersPort(myPort);
		msg.setMiddleman(battleHelper);
		msg.setMiddlemanPort(Integer.parseInt(battleHelperLocation.split(":")[1]));
		msg.setRequest(MessageRequest.spawnUnit);
			
		return msg;
	}
	
	public void movePlayer(Direction direction) {
		Message move = createMoveMessage(direction);
		sendMessage(move);
	}

	private Message createMoveMessage(Direction direction) {
		String recipient = battleServer;
		Message msg = new Message(recipient);
		msg.setSender(playerID);
		msg.setSendersPort(myPort);
		msg.setRequest(MessageRequest.moveUnit);
//		msg.put("Unit", );
//		msg.put("x", );
//		msg.put("y", );
		
		return msg;
	}
	
	private void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub
		IRunner RMIServer = null;
		String urlServer = new String("rmi://" + battleHelperLocation + "/" + battleHelper);

		// Bind to RMIServer
		try {
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
		System.out.println("PlayerController: received message");
		System.out.println("PlayerController: msg: " + msg.toString());
		// Update somethings
		switch(msg.getRequest()) {
			case MessageRequest.spawnAck:
				System.out.println(msg);
				if((boolean) msg.get("spawned")) {
					this.run();
				}
				else {
					
				}
			default:
				break;
		}
	}

}
