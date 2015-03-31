package server.helper;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.Common;
import common.IBattleField;
import common.IRunner;
import common.Log;

public class HelperMain {

	public static void main(String[] args) throws RemoteException {

		/**
		 * battleServerLocation location should be provider in host:port format
		 */
		String battleServerLocation = args[0];
		String battleServer = "main_battle_server";
		String serverID = "helper_battle_server_" + Common.randomString(10);
		
		if (!isServerOnline(battleServerLocation, battleServer)) {
			Log.log(serverID, "The main server is not available");
			return;
		}

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
			runner.setMyAddress(address  + "/" + serverID);
			Log.log(address  + "/" + serverID, "Battlefield helper running, server: "
					+ serverID + ", reg: " + reg.toString());
			bindWithMainServer(battleServerLocation, battleServer, serverID,
					address);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isServerOnline(String serverLocation,
			String serverNode) {
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://" + serverLocation + "/"
				+ serverNode);

		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			RMIServer.ping();
			return true;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return false;
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

}
