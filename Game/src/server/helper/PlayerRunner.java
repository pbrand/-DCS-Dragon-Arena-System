package server.helper;

import java.rmi.Naming;

import common.IPlayerController;
import common.Message;

public class PlayerRunner implements IRunner {

	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub
		IBattleField RMIServer = null;
		String urlServer = new String("rmi://localhost/" + msg.getRecipient());

		// Bind to RMIServer
		try {
			RMIServer = (IBattleField) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
            RMIServer.receiveMessage(msg);

		} catch(Exception e) {
            e.printStackTrace();
		}
	}

	@Override
	public void receiveMessage(Message msg) {
		// TODO Auto-generated method stub
		IPlayerController RMIServer = null;
		String urlServer = new String("rmi://localhost/" + msg.getRecipient());

		// Bind to RMIServer
		try {
			RMIServer = (IPlayerController) Naming.lookup(urlServer);
			// Attempt to send messages the specified number of times
            RMIServer.receiveMessage(msg);

		} catch(Exception e) {
            e.printStackTrace();
		}
	}

}
