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
	// private int sendersPort = 1099;

	private String recipient;
	private String middleman;
	private int middlemanPort = 1099;
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

	public int getMiddlemanPort() {
		return middlemanPort;
	}

	public void setMiddlemanPort(int middlemanPort) {
		this.middlemanPort = middlemanPort;
	}

	public void put(String key, Object object) {
		values.put(key, object);
	}

	public String getMiddleman() {
		return middleman;
	}

	public void setMiddleman(String middleman) {
		this.middleman = middleman;
	}

	@Override
	public String toString() {
		return "Message [messsageID=" + messsageID + ", sender=" + sender
				+ ", recipient=" + recipient + ", middleman=" + middleman
				+ ", middlemanPort=" + middlemanPort + ", updates=" + updates
				+ ", request=" + request + ", values=" + values + "]";
	}

}
