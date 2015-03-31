package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRunner extends Remote {

	public void sendMessageToClient(Message msg) throws RemoteException;

	public void sendMessageToServer(Message msg) throws RemoteException;	
	
	public void receiveMessage(Message msg) throws RemoteException;
	
	public void ping() throws RemoteException;
	
	public void registerWithServer(String player, String address) throws RemoteException;
	
	public boolean setBackupServerLocation(String serverLocation) throws RemoteException;
	
	public void setMyAddress(String address) throws RemoteException;
	
	public String getMetrics() throws RemoteException;

}
