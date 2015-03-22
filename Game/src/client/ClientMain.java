package client;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import server.helper.IBattleField;

import common.IPlayerController;

public class ClientMain {

	public static String serverID;

	private static String helper_host;
	private static int helper_port;

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

			String battleServer = "main_battle_server";
			String battleServerLocation = "192.168.56.1:6115";
			String res = getHelperServer(battleServerLocation, battleServer);
			if (res.equals("noServers")) {
				return;
			}

			String[] helper = res.split(":");

			String battle_helper = helper[0];
			helper_host = helper[1];
			helper_port = Integer.parseInt(helper[2]);

			System.out.println("host: " + helper_host + ":" + helper_port);

			player = new PlayerController(playerName, helper_host, helper_port,
					battle_helper, battleServerLocation, battleServer);

			stub = (IPlayerController) UnicastRemoteObject.exportObject(player,
					0);

			Registry reg = LocateRegistry.getRegistry(helper_host, helper_port);

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

	public static String getHelperServer(String battleServerLocation,
			String battleServer) {
		String helper = null;

		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);

		// Bind to RMIServer
		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			helper = RMIServer.getRandomHelper();
			while (helper == null) {
				helper = RMIServer.getRandomHelper();
			}
			if (helper.equals("noServers")) {
				System.out.println("No server online to connect to");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return helper;
	}

	private static void printRegistry(String[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.println("item[" + i + "]: " + array[i]);
		}
	}

}
