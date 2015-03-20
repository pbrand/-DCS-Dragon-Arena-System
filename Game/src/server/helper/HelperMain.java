package server.helper;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.IRunner;

import server.master.BattleField;

public class HelperMain {

	public final static String serverID = "helper_battle_server";


	public static void main(String[] args) throws RemoteException {

		// Bind to RMI registry
//		if (System.getSecurityManager() == null) {
//			System.setSecurityManager(new SecurityManager());
//		}
		try {

			IRunner runner = new PlayerRunner();

			IRunner stub = (IRunner) UnicastRemoteObject
					.exportObject(runner, 0);

			Registry reg = LocateRegistry.createRegistry(6115);

			reg.rebind(serverID, stub);
			// registry.rebind("Process", stub);
			System.out.println("Battlefield helper running, server: " + serverID + ", reg: " + reg.toString());
		} catch (RemoteException e) {
			//Registry reg = LocateRegistry.createRegistry(1099);
			//reg.rebind(serverID, runner);
			e.printStackTrace();
		}
	}

}
