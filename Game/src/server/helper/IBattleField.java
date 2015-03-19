package server.helper;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.Message;

public interface IBattleField extends Remote {
	
	public void sendMessage(Message msg) throws RemoteException;
	
	public void receiveMessage(Message msg) throws RemoteException;

}
