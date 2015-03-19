package core;

import java.rmi.Remote;

import client.Message;

public interface ICore extends Remote {
	
	public void receiveMessage(Message msg);
	
	public void sendMessage(Message msg);

}
