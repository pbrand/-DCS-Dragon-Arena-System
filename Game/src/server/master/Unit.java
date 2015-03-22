package server.master;

import java.io.Serializable;

public abstract class Unit implements Serializable {
	// Position of the unit
		protected int x, y;
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -8843472568878441565L;

	public void adjustHitPoints(Integer integer) {
		// TODO Auto-generated method stub
		
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getHitPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getX() {
		// TODO Auto-generated method stub
		return this.x;
	}
	
	public int getY() {
		// TODO Auto-generated method stub
		return this.y;
	}

	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

}
