package server.master;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import server.helper.IBattleField;
import server.master.BattleField;

public class Main {

	public static String serverID = "main_battle_server";

	public static void main(String[] args) throws RemoteException {

		// Bind to RMI registry
		/*
		 * if (System.getSecurityManager() == null) {
		 * System.setSecurityManager(new SecurityManager()); }
		 */
		int port = 1099;
		IBattleField stub = null;
		try {

			IBattleField battlefield = BattleField.getBattleField();

			stub = (IBattleField) UnicastRemoteObject
					.exportObject(battlefield, 0);

			Registry reg = LocateRegistry.createRegistry(port);

			reg.rebind(serverID, stub);
			// registry.rebind("Process", stub);
			System.out.println("Battlefield running, server: " + serverID
					+ ", reg: " + reg.toString());
		} catch (RemoteException e) {			
			Registry reg = LocateRegistry.getRegistry(port);
			serverID = "battle_server" + "_" + reg.list().length;
			reg.rebind(serverID, stub);
			//e.printStackTrace();
			System.out.println("Number of servers: " + reg.list().length);
			System.out.println("Battlefield running, server: " + serverID
					+ ", reg: " + reg.toString());
		}
	}

}
