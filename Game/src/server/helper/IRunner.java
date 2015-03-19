package server.helper;

import common.Message;

public interface IRunner {
	
	public void sendMessage(Message msg);
	
	public void receiveMessage(Message msg);

}
