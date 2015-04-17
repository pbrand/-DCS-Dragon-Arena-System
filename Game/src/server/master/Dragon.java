package server.master;


public class Dragon extends Unit {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7453124200846854178L;
	// The minimum and maximum amount of hitpoints that a particular dragon starts with
	public static final int MIN_HITPOINTS = 50;
	public static final int MAX_HITPOINTS = 100;
	// The minimum and maximum amount of hitpoints that a particular dragon has
	public static final int MIN_ATTACKPOINTS = 5;
	public static final int MAX_ATTACKPOINTS = 20;
	
	protected String serverAddress;
	
	/**
	 * Spawn a new dragon, initialize the 
	 * reaction speed 
	 *
	 */
	public Dragon(String id, String serverAddress) {
		/* Spawn the dragon with a random number of hitpoints between
		 * 50..100 and 5..20 attackpoints. */
		super((int)(Math.random() * (MAX_HITPOINTS - MIN_HITPOINTS) + MIN_HITPOINTS), (int)(Math.random() * (MAX_ATTACKPOINTS - MIN_ATTACKPOINTS) + MIN_ATTACKPOINTS));

		this.unitID = id;
		this.serverAddress = serverAddress;		
	}

	
}
