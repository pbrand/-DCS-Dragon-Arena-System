package server.helper;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import common.Enums.UnitType;
import common.Message;

public interface IBattleField extends Remote {

	public void sendMessage(Message msg) throws RemoteException;

	public void receiveMessage(Message msg) throws RemoteException;

	public void setMyAddress(String myAddress) throws RemoteException;

	public HashMap<String, String> getHelpers() throws RemoteException;

	public void putHelper(String key, String value) throws RemoteException;

	public int[] getPosition(String id) throws RemoteException;

	public int getMapHeight() throws RemoteException;

	public int getMapWidth() throws RemoteException;

	public UnitType getType(int x, int y) throws RemoteException;

	public String getRandomHelper() throws RemoteException;

}
