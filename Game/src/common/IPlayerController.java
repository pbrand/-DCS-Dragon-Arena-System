package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Enums.Direction;

public interface IPlayerController extends Remote {
	public void sendMessage(Message msg) throws RemoteException;

	public void receiveMessage(Message msg) throws RemoteException;

	public void spawnPlayer() throws RemoteException;

	public void movePlayer(Direction direction) throws RemoteException;

	public String getMetrics() throws RemoteException;

	public void setMyPort(int myPort) throws RemoteException;

	public void setMyHost(String myHost) throws RemoteException;

}
