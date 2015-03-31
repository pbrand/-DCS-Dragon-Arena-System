package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IUnit extends Remote {

	public void sendMessage(Message msg) throws RemoteException;

	public void receiveMessage(Message msg) throws RemoteException;
	
	/**
	 * @return the maximum number of hitpoints.
	 */
	public int getMaxHitPoints() throws RemoteException;

	/**
	 * @return the unique unit identifier.
	 */
	public String getUnitID() throws RemoteException;

	/**
	 * @return the current number of hitpoints.
	 */
	public int getHitPoints() throws RemoteException;

	/**
	 * @return the attack points
	 */
	public int getAttackPoints() throws RemoteException;

	public int getX() throws RemoteException;
	
	public int getY() throws RemoteException;

}
