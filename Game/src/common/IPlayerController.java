package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IPlayerController extends Remote {

	public void sendMessage(Message msg) throws RemoteException;

	public void receiveMessage(Message msg) throws RemoteException;
	
	public void spawnPlayer() throws RemoteException;
}
