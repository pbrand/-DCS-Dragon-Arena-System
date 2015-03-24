package server.helper;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import common.IRunner;

public class HelperMain {

	public static void main(String[] args) throws RemoteException {

		/**
		 * battleServerLocation location should be provider in host:port format
		 */
		String battleServerLocation = args[0];
		String battleServer = "main_battle_server";
		String serverID = "helper_battle_server_" + randomString(10);

		// Bind to RMI registry
		// if (System.getSecurityManager() == null) {
		// System.setSecurityManager(new SecurityManager());
		// }
		try {

			IRunner runner = new PlayerRunner(battleServerLocation,
					battleServer);
			IRunner stub = (IRunner) UnicastRemoteObject
					.exportObject(runner, 0);
			Registry reg = LocateRegistry.createRegistry(0);
			reg.rebind(serverID, stub);
			String address = reg.toString().split("endpoint:\\[")[1]
					.split("\\]")[0];
			System.out.println("Battlefield helper running, server: "
					+ serverID + ", reg: " + reg.toString());
			bindWithMainServer(battleServerLocation, battleServer, serverID,
					address);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private static void bindWithMainServer(String battleServerLocation,
			String battleServer, String serverID, String myAddress) {
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + battleServerLocation + "/"
				+ battleServer);

		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			RMIServer.putHelper(serverID, myAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String randomString(int len) {
		final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}

}
