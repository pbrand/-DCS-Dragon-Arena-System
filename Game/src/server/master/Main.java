	package server.master;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.IBattleField;

public class Main {
	@SuppressWarnings("unused")
	private static BattleFieldViewer bfv;
	public static String serverID = "main_battle_server";

	public static void main(String[] args) throws RemoteException {

		// Bind to RMI registry
		/*
		 * if (System.getSecurityManager() == null) {
		 * System.setSecurityManager(new SecurityManager()); }
		 */
		int port = 0;

		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		boolean isMain = false;

		IBattleField stub = null;
		try {

			BattleField battlefield = null;
			
			if (args.length > 1 && args[1].equals("backup")) {
				battlefield = BattleField.createBackupBattleField();
			} else {
				battlefield = BattleField.getBattleField();
				isMain = true;
			}
			
			stub = (IBattleField) UnicastRemoteObject.exportObject(battlefield,
					0);

			Registry reg = LocateRegistry.createRegistry(port);

			reg.rebind(serverID, stub);
			String address = reg.toString().split("endpoint:\\[")[1]
					.split("\\]")[0];
			battlefield.initiateBattleService(address + "/" + serverID);
			bfv = new BattleFieldViewer(battlefield);

			System.out.println("Battlefield running, server: " + serverID
					+ ", reg: " + reg.toString());
			
			if (isMain && args.length > 1) {
				setBackup(address, args[1]);
			}
			
			mainCommander(address);

		} catch (RemoteException e) {
			Registry reg = LocateRegistry.getRegistry(port);
			reg.rebind(serverID, stub);
			System.out.println("Number of servers: " + reg.list().length);
			System.out.println("Battlefield running, server: " + serverID
					+ ", reg: " + reg.toString());
		}
	}
	
	private static void mainCommander(final String address) {
		Runnable myRunnable = new Runnable() {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));

			public void run() {
				System.out.println("Commander Running");
				while (true) {
					String line = "";
					try {
						line = in.readLine();
						String[] res = line.split(" ");
						if (res.length > 0 && res[0].equals("setbackup")) {
							setBackup(address, res[1]);								
						}
						if (res.length > 0 && res[0].equals("q")) {
							saveMetrics(address);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
				// do something
				// in.close();
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}
	
	private static void setBackup(String mainServer, String backup) {
		try {
			IBattleField RMIServer = (IBattleField) Naming.lookup("rmi://" +  mainServer + "/" + serverID);
			RMIServer.setBackupAddress(backup + "/" + serverID);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace(); 
		} 
	}
	
	private static void saveMetrics(String mainServer) {
		try {
			IBattleField RMIServer = (IBattleField) Naming.lookup("rmi://" +  mainServer + "/" + serverID);
			RMIServer.saveMetrics();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace(); 
		} 
	}

}
