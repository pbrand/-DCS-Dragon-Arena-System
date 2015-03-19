package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.IPlayerController;

public class ClientMain {

	public static String serverID;
	private static int myPort;

	public static void main(String[] args) throws RemoteException {

		// Bind to RMI registry
		/*
		 * if (System.getSecurityManager() == null) {
		 * System.setSecurityManager(new SecurityManager()); }
		 */
		IPlayerController player = null;
		IPlayerController stub = null;
		try {
			// System.setProperty("java.rmi.server.hostname","192.168.56.1");
			myPort = Integer.parseInt(args[0]);

			String playerName = args[1];
			serverID = playerName;
			player = new PlayerController(playerName, myPort);

			stub = (IPlayerController) UnicastRemoteObject.exportObject(player,
					0);

			Registry reg = LocateRegistry.createRegistry(myPort);

			reg.rebind(serverID, stub);
			System.out.println("PlayerController running, server: " + serverID
					+ ", reg: " + reg.toString());

			player.spawnPlayer();

		} catch (RemoteException e) {
			// Registry reg = LocateRegistry.createRegistry(0);
			// .rebind(serverID, player);
			e.printStackTrace();
		}
	}

}
