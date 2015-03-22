package client;

import java.rmi.Naming;

import common.Enums.Direction;
import common.IPlayerController;
import common.IRunner;
import common.Message;
import common.MessageRequest;

public class PlayerController implements IPlayerController {

	private String playerID;
	private static String battleServer = "main_battle_server";
	private static String battleHelper = "helper_battle_server";
	private int port; // helper port
	private String host; // helper host

	private boolean running = false;
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;

	public PlayerController(String playerID, String host, int port) {
		// TODO Auto-generated constructor stub
		this.playerID = playerID;
		this.port = port;
		this.host = host;
	}

	public void run() {
		Direction direction;
		this.running = true;

		int i = 0;
		// TODO This is an infinite loop until it receives a message that it
		// should stop. That's tricky.
		while (/* GameState.getRunningState() && */this.running) {
			i += 1;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (i > 4) {
				break;
			}

			/* Sleep while the player is considering its next move */
			// Thread.currentThread().sleep((int)(timeBetweenTurns * 500 *
			// GameState.GAME_SPEED));

			/* Stop if the player runs out of hitpoints */
			// Receive a message here?? (if hitpoints <= 0) -> then set running
			// to false so that the mainloop quits.

			// Randomly choose one of the four wind directions to move to if
			// there are no units present
			direction = Direction.values()[(int) (Direction.values().length * Math
					.random())];
			System.out.println("Move: " + direction.toString());
			movePlayer(direction);
		}
	}

	public void spawnPlayer() {
		Message spawn = createSpawnMessage();
		System.out.println(spawn);
		sendMessage(spawn);
	}

	private Message createSpawnMessage() {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.spawnUnit);
		return msg;
	}

	public void movePlayer(Direction direction) {
		Message move = createMoveMessage(direction);
		sendMessage(move);
	}

	private Message createMoveMessage(Direction direction) {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.moveUnit);
		msg.put("direction", direction);

		return msg;
	}

	private void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub
		IRunner RMIServer = null;
		String urlServer = new String("rmi://" + host + ":" + port + "/"
				+ battleHelper);

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
		switch (msg.getRequest()) {
		case MessageRequest.spawnAck:
			if ((boolean) msg.get("spawned")) {
				this.run();
			} else {

			}
		default:
			break;
		}
	}
	
	private Message createMessage(String recipient) {
		Message msg = new Message(recipient);
		msg.setSender(playerID);
		msg.setMiddleman(battleHelper);
		msg.setMiddlemanPort(port);
		
		return msg;
	}

}
