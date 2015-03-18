package core;

import common.Enums.UnitType;

public interface IUnit {

	public void dealDamage(int x, int y, int damage);
	
	public void healDamage(int x, int y, int healed);

	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints();

	/**
	 * @return the unique unit identifier.
	 */
	public int getUnitID();

	/**
	 * @return the x position
	 */
	public int getX();

	/**
	 * @return the y position
	 */
	public int getY();

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints();

	/**
	 * @return the attack points
	 */
	public int getAttackPoints();
	
	public UnitType getType(int x, int y);

	public void moveUnit(int x, int y);
	
	// Disconnects the unit from the battlefield by exiting its run-state
	public void disconnect();
}
