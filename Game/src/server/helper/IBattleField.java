package server.helper;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Enums.UnitType;
import common.Message;

public interface IBattleField extends Remote {
	
	public void sendMessage(Message msg) throws RemoteException;
	
	public void receiveMessage(Message msg) throws RemoteException;
	
	public int[] getPosition(String id) throws RemoteException;
	
	public int getMapHeight() throws RemoteException;
	
	public int getMapWidth() throws RemoteException;
	
	public UnitType getType(int x, int y) throws RemoteException;

}
