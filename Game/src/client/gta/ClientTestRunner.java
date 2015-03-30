package client.gta;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

import common.IPlayerController;

import client.ClientMain;

public class ClientTestRunner {

	private static List<GtaClient> clients;
	private static String address;

	public static void main(String[] args) {
		String file = "SC2_Edge_Detailed";
		clients = FileParser.getClientsData(file);
		executeClients(clients, args[0]);
		address = args[0];
	}

	private static void executeClients(List<GtaClient> clients,
			String mainAddress) {
		for (int i = 0; i < clients.size() && i < 101; i++) {
			String[] arguments = { mainAddress, "p_" + clients.get(i).getId() };
			long runtime = 0;
			clientRunner(arguments, runtime);
			if (i < clients.size()) {
				double time = (clients.get(i + 1).getTimestamp() - clients.get(i).getTimestamp()) / 1000;
				System.out.println("Wait for: " + time);
				try {
					Thread.sleep((long) time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void clientRunner(final String[] arguments, long runtime) {
		Runnable myRunnable = new Runnable() {
			public void run() {
				try {
					ClientMain.main(arguments);
					Thread.sleep(runtime);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(myRunnable);
		thread.start();
	}
	
	private static void disconnectPlayer(String player) {
		try {
			IPlayerController RMIServer = (IPlayerController) Naming
					.lookup("rmi://" + address + "/"
							+ player);
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}

}
