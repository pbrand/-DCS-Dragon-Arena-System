package server.helper;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import server.master.BattleField;

public class Main {

	public final static String serverID = "main_battle_server";

	/* The static singleton */
	private static BattleField battlefield;

	public static void main(String[] args) throws RemoteException {

		// Bind to RMI registry
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {

			IBattleField battlefield = BattleField.getBattleField();

			IBattleField stub = (IBattleField) UnicastRemoteObject
					.exportObject(battlefield, 0);

			Registry reg = LocateRegistry.createRegistry(0);

			reg.rebind(serverID, stub);
			// registry.rebind("Process", stub);
			System.out.println("Battlefield running, server: " + serverID + ", reg: " + reg.toString());
		} catch (RemoteException e) {
			Registry reg = LocateRegistry.createRegistry(1099);
			reg.rebind(serverID, battlefield);
			e.printStackTrace();
		}
	}

}
