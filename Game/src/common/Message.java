package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8140103891165919475L;
	private int messsageID;

	private String sender;
	private int sendersPort;

	private String recipient;
	private ArrayList<String> updates;
	private String request;
	private HashMap<String, Object> values;

	public Message(String recipient) {
		updates = new ArrayList<String>();
		values = new HashMap<String, Object>();
		this.recipient = recipient;
	}

	public Message(int messageID, String sender, String recipient,
			String request) {
		updates = new ArrayList<String>();
		this.sender = sender;
		this.messsageID = messageID;
		this.request = request;
		this.recipient = recipient;
		values = new HashMap<String, Object>();
	}

	public int getSendersPort() {
		return sendersPort;
	}

	public void setSendersPort(int sendersPort) {
		this.sendersPort = sendersPort;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public ArrayList<String> getUpdates() {
		return updates;
	}

	public int getMesssageID() {
		return messsageID;
	}

	public String getRequest() {
		return request;
	}

	public Object get(String key) {
		// TODO Auto-generated method stub
		return values.get(key);
	}

	public void put(String key, Object object) {
		values.put(key, object);
	}

	@Override
	public String toString() {
		return "Message [messsageID=" + messsageID + ", sender=" + sender
				+ ", sendersPort=" + sendersPort + ", recipient=" + recipient
				+ ", updates=" + updates + ", request=" + request + ", values="
				+ values + "]";
	}

}
