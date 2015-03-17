package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MessageHandler extends UnicastRemoteObject {
	
	protected MessageHandler() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8230784416900188436L;

	public static void main(String[] args) {
	        if (System.getSecurityManager() == null) {
	            System.setSecurityManager(new SecurityManager());
	        }
	        try {
	            String name = "MessageHandler";
	            Core core = new Core();
	            Core stub =
	                (Core) UnicastRemoteObject.exportObject(core, 0);
	            Registry registry = LocateRegistry.getRegistry();
	            registry.rebind(name, stub);
	            System.out.println("MessageHandler bound");
	        } catch (Exception e) {
	            System.err.println("MessageHandler exception:");
	            e.printStackTrace();
	        }
	    }

}
