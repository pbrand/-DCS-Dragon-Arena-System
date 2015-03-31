package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.Enums.Direction;
import common.IPlayerController;
import common.IRunner;
import common.IUnit;
import common.Message;
import common.MessageRequest;

public class PlayerController implements IPlayerController {

	private String playerID;
	private String battleServerLocation;
	private String battleServer;
	private String battleHelper;
	private int port; // helper port
	private String host; // helper host

	private boolean running = false;
	private boolean targets = false;
	private ArrayList<IUnit> players;
	private ArrayList<IUnit> dragons;
	private int x;
	private int y;
	/* Reaction speed of the player
	 * This is the time needed for the player to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	protected int timeBetweenTurns;
	private int closestEnemyX;
	private int closestEnemyY;
	private double lifespan;
	private boolean closestEnemy;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	
	/**
	 * Metrics
	 */
	private int totalMessagesSend;
	private int totalMessagesReceived;
	private int totalMessagesFailedToSend;
	private int totalMessagesFailedToReceive;
	private int totalNumberOfHelperSwitched;
	
	private long startTime;
	private long endTime;
	
	public PlayerController(String playerID, String host, int port,
			String battle_helper, String battleServerLocation,
			String battle_server, double lifespan) {
		this(playerID, host, port, battle_helper, battleServerLocation, battle_server);
		this.lifespan = lifespan;
		
		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;
	}

	public PlayerController(String playerID, String host, int port,
			String battle_helper, String battleServerLocation,
			String battle_server) {
		startTime = System.currentTimeMillis();
		this.playerID = playerID;
		this.port = port;
		this.host = host;
		this.battleHelper = battle_helper;
		this.battleServer = battle_server;
		this.battleServerLocation = battleServerLocation;
		
		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;
	}

	public void run() {
		this.setRunning(true);

		int i = 0;
		int life = (int) Math.round(lifespan/ 100);
		// TODO This is an infinite loop until it receives a message that it
		// should stop. That's tricky.
		while (/* GameState.getRunningState() && */this.running) {
			i += 1;
				/* Sleep while the player is considering its next move */
				//Thread.sleep(1000);
				try {
					Thread.sleep((int)(timeBetweenTurns * 500/* * GameState.GAME_SPEED*/));
					
					if (i > 20) {
						//disconnectPlayer();
						break;
					}
					this.requestTargets();
					// Wait a while if the request from the server is not yet answered.
					int attempts = 0;
					while(!this.targets) {
						if(attempts < 10) {
							Thread.sleep(5);
						}
						else {
							break;
						}
						attempts++;
					}
					if(this.targets) {
						// set targets to false again for the next iteration
						this.targets = false;
						
						boolean action = false;
						if(players.size() != 0) {
							for(IUnit target : players) {
								if(target.getHitPoints() > 0 && (double) target.getHitPoints() / (double) target.getMaxHitPoints() < 0.5) {
									healDamage(target.getX(), target.getY());
									action = true;
									break;
								}
							}
						}
						if(!action) {
							if(dragons.size() != 0) {
								int minimumHealth = Integer.MAX_VALUE;
								IUnit dragonToSlay = null;
								for(IUnit target : dragons) {
									if(target.getHitPoints() < minimumHealth){
										minimumHealth = target.getHitPoints();
										dragonToSlay = target;
									}
								}
								this.dealDamage(dragonToSlay.getX(), dragonToSlay.getY());
							}
							// Move closer to a dragon.
							else if(this.closestEnemy){
								this.closestEnemy = false;
								// Randomly choose one of the four wind directions to move to if
								// there are no units present	
								Direction direction;
								int dX = this.x - this.closestEnemyX;
								int dY = this.y - this.closestEnemyY;
								if(dX == 0) {
									if(dY > 0) {
										direction = Direction.up;
									}
									else {
										direction = Direction.down;
									}
								}
								else if(dY == 0) {
									if(dX > 0) {
										direction = Direction.left;
									}
									else {
										direction = Direction.right;
									}									
								}
								else if(Math.abs(dY) < Math.abs(dX)) {
									if(dY > 0) {
										direction = Direction.up;
									}
									else {
										direction = Direction.down;
									}
								}
								else {	
									if(dX > 0) {
										direction = Direction.left;
									}
									else {
										direction = Direction.right;
									}
								}
								
								movePlayer(direction);
							}
							else{
								Direction direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];
								movePlayer(direction);
							}
						}
					}
					
				} catch (InterruptedException | RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};

			if (i > life) {
				disconnect();
				break;
			}

			/* Stop if the player runs out of hitpoints */
			// Receive a message here?? (if hitpoints <= 0) -> then set running
			// to false so that the mainloop quits.
		}
		System.out.println("Player: "+playerID+" has stopped running and requested disconnect.");
	}

	private void healDamage(int x, int y) {
		Message heal = createHealDamageMessage(x,y);
		log(heal.toString());
		sendMessage(heal);
	}

	private Message createHealDamageMessage(int x, int y) {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.healDamage);
		msg.put("x", x);
		msg.put("y", y);
		return msg;
	}
	
	private void dealDamage(int x, int y) {
		Message attack = createDealDamageMessage(x,y);
		log(attack.toString());
		sendMessage(attack);
	}

	private Message createDealDamageMessage(int x, int y) {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.dealDamage);
		msg.put("x", x);
		msg.put("y", y);
		return msg;
	}

	public void spawnPlayer() {
		Message spawn = createSpawnMessage();
		log(spawn.toString());
		sendMessage(spawn);
	}
	
	public void disconnect() {
		Message disconnect = createDisconnectMessage();
		log(disconnect.toString());
		sendMessage(disconnect);
	}

	private Message createSpawnMessage() {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.spawnUnit);
		return msg;
	}
	
	private Message createDisconnectMessage() {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.disconnectUnit);
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

	public void requestTargets() {
		Message targets = createRequestTargetsMessage();
		sendMessage(targets);
	}

	private Message createRequestTargetsMessage() {
		Message msg = createMessage(battleServer);
		msg.setRequest(MessageRequest.getTargets);
		
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
			totalMessagesSend += 1;
		} catch (NotBoundException | MalformedURLException | RemoteException e) {
			e.printStackTrace();
			if (this.resetHelperServer()) {
				this.sendMessage(msg);
			} else {
				log("no server is online");
				this.running = false;
				totalMessagesFailedToSend += 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			totalMessagesFailedToSend += 1;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receiveMessage(Message msg) {
		// TODO Auto-generated method stub
		log("PlayerController: received message");
		log("PlayerController: msg: " + msg.toString());
		totalMessagesReceived += 1;
		// Update somethings
		switch (msg.getRequest()) {
		case MessageRequest.spawnAck:
			if ((boolean) msg.get("spawned") && !running) {
				this.run();
			}
			break;
		case MessageRequest.returnTargets:
			this.players = (ArrayList<IUnit>) msg.get("players");
			this.dragons = (ArrayList<IUnit>) msg.get("dragons");
			this.x = (int) msg.get("playerX");
			this.y = (int) msg.get("playerY");
			if(msg.get("enemyX") != null && msg.get("enemyY") != null) {
				this.closestEnemyX = (int) msg.get("enemyX");
				this.closestEnemyY = (int) msg.get("enemyY");
				this.closestEnemy = true;
			}
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

	private Message createMessage(String recipient) {
		Message msg = new Message(recipient);
		msg.setSender(playerID);
		msg.setMiddleman(battleHelper);
		msg.setMiddlemanPort(port);

		return msg;
	}

	private boolean resetHelperServer() {
		boolean reset = false;
		String res = ClientMain.getHelperServer(battleServerLocation,
				battleServer);

		if (res.equals("noServers")) {
			this.running = false;
			return reset;
		}

		String[] newHelper = res.split(":");
		this.battleHelper = newHelper[0];
		this.host = newHelper[1];
		this.port = Integer.parseInt(newHelper[2]);

		try {
			Registry reg = LocateRegistry.getRegistry(host, port);
			reg.rebind(this.playerID, this);
		} catch (RemoteException e)  {
			e.printStackTrace();
			return reset;
		}

		String urlServer = new String("rmi://" + host + ":" + port + "/"
				+ battleHelper);

		try {
			IRunner RMIServer = (IRunner) Naming.lookup(urlServer);
			RMIServer.registerWithServer(this.playerID, host + ":" + "/");
			totalNumberOfHelperSwitched += 1;
		} catch (Exception e) {
			e.printStackTrace();
		}

		log("Reset succesfull, server: " + host + ":" + port);
		reset = true;

		return reset;
	}
	
	public String getMetrics() {
		this.endTime = System.currentTimeMillis();
		String res = "\n";
		res += ("[" + playerID + "] Total Messages Send: " + totalMessagesSend + "\n");
		res += ("[" + playerID + "] Total Messages Received: " + totalMessagesReceived + "\n");
		res += ("[" + playerID + "] Total Messages Failed To Send: " + totalMessagesFailedToSend + "\n" );
		res += ("[" + playerID + "] Total Messages Failed To Receive: " + totalMessagesFailedToReceive + "\n" );
		res += ("[" + playerID + "] Total Times Helpers switched: " + totalNumberOfHelperSwitched + "\n" );
		res += ("[" + playerID + "] Runtime: " + common.Common.getFormatedTime(endTime - startTime) + "\n");
		
		return res;
	}
	
	private void log(String text) {
		common.Log.log(host + ":" + port + "/" + playerID, text);
	}

}
