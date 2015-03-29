package server.master;

import java.io.Serializable;

import common.IUnit;

public abstract class Unit implements Serializable, IUnit {
	// Position of the unit
		protected int x, y;
		
	// Health
	private int maxHitPoints;
	protected int hitPoints;

	// Attack points
	protected int attackPoints;
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -8843472568878441565L;

	public Unit(int maxHealth, int attackPoints) {
		// Initialize the max health and health
		hitPoints = maxHitPoints = maxHealth;

		// Initialize the attack points
		this.attackPoints = attackPoints;
	}

	public void adjustHitPoints(Integer modifier) {
		if (hitPoints <= 0)
			return;

		hitPoints += modifier;

		if (hitPoints > maxHitPoints)
			hitPoints = maxHitPoints;

//		if (hitPoints <= 0)
//			removeUnit(x, y);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	
	/**
	 * @return the attack points
	 */
	public int getAttackPoints() {
		return attackPoints;
	}
	
	/**
	 * @return the current number of hitpoints.
	 */	
	public int getHitPoints() {
		// TODO Auto-generated method stub
		return this.hitPoints;
	}
	
	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints() {
		return maxHitPoints;		
	}

	/**
	 * @return the x position
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * @return the y position
	 */
	public int getY() {
		return this.y;
	}
	
	@Override
	public void dealDamage(int x, int y, int damage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void healDamage(int x, int y, int healed) {
		// TODO Auto-generated method stub
		
	}	

	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

}
