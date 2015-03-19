package core;

import java.io.Serializable;

import client.Message;

public interface IMessage extends Serializable {
	
	public void receiveMessage(Message msg);

}
