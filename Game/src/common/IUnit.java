package common;


public interface IUnit {

	//public void sendMessage(Message msg);

	//public void receiveMessage(Message msg);
	
	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints();

	/**
	 * @return the unique unit identifier.
	 */
	public String getUnitID();

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints();

	/**
	 * @return the attack points
	 */
	public int getAttackPoints();

	public int getX();
	
	public int getY();

}
