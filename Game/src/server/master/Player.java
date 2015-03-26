package server.master;

public class Player extends Unit {
	/* Reaction speed of the player
	 * This is the time needed for the player to take its next turn.
	 * Measured in half a seconds x GAME_SPEED.
	 */
	private static final long serialVersionUID = 2212365375426947223L;
	protected int timeBetweenTurns;
	public static final int MIN_TIME_BETWEEN_TURNS = 2;
	public static final int MAX_TIME_BETWEEN_TURNS = 7;
	public static final int MIN_HITPOINTS = 20;
	public static final int MAX_HITPOINTS = 10;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;

	/**
	 * Create a player, initialize both 
	 * the hit and the attackpoints. 
	 */
	public Player(int x, int y) {
		/* Initialize the hitpoints and attackpoints */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));

		/* Create a random delay */
		timeBetweenTurns = (int)(Math.random() * (MAX_TIME_BETWEEN_TURNS - MIN_TIME_BETWEEN_TURNS)) + MIN_TIME_BETWEEN_TURNS;

		if (!spawn(x, y))
			return; // We could not spawn on the battlefield

		/* Create a new player thread */
		//new Thread(this).start();
		runnerThread = new Thread(this);
		runnerThread.start();
	}	
	/**
	 * 
	 */
	

	public Player() {
		// TODO Auto-generated constructor stub
	}

}
