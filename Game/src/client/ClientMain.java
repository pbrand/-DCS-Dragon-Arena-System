package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.IPlayerController;

public class ClientMain {

	public static String serverID;
	
	private static String host = "192.168.56.1";
	private static int port = 6115;

	public static void main(String[] args) throws RemoteException {

		// Bind to RMI registry
		/*
		 * if (System.getSecurityManager() == null) {
		 * System.setSecurityManager(new SecurityManager()); }
		 */
		IPlayerController player = null;
		IPlayerController stub = null;
		try {
			String playerName = args[0];
			serverID = playerName;
			player = new PlayerController(playerName, host, port);

			stub = (IPlayerController) UnicastRemoteObject.exportObject(player,
					0);

			Registry reg = LocateRegistry.getRegistry(host, port);

			reg.rebind(serverID, stub);
			System.out.println("PlayerController running, server: " + serverID
					+ ", reg: " + reg.toString());
			printRegistry(reg.list());
			player.spawnPlayer();

		} catch (RemoteException e) {
			// Registry reg = LocateRegistry.createRegistry(0);
			// .rebind(serverID, player);
			e.printStackTrace();
		}
	}
	
	private static void printRegistry(String [] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.println("item[" + i + "]: " + array[i]);
		}
	}

}
