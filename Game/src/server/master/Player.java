package server.master;


public class Player extends Unit {
	private static final long serialVersionUID = 2212365375426947223L;
	public static final int MIN_HITPOINTS = 20;
	public static final int MAX_HITPOINTS = 10;
	public static final int MIN_ATTACKPOINTS = 1;
	public static final int MAX_ATTACKPOINTS = 10;
	public static final int HEAL_RANGE = 5;

	/**
	 * Create a player, initialize both 
	 * the hit and the attackpoints. 
	 */
	public Player(String id, int x, int y) {
		/* Initialize the hitpoints and attackpoints */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));
		this.unitID = id;
		setPosition(x,y);
	}

	public Player(String id) {
		/* Initialize the hitpoints and attackpoints */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));
		this.unitID = id;
	}


}
