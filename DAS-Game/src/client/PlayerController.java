package client;

import server.BattleField;
import core.IUnit;

public class PlayerController implements IUnit {
	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;
	
	public PlayerController() {
		// Create a new player at server side
		// Retrieve battlefield and player details
		// run()
		// disconnect
		run();
		disconnect();
	}
	
	private void run() {
		boolean running = true;
		
		while(running) {
			
		}
	}

	@Override
	public void dealDamage(int x, int y, int damage) {
		/* Create a new message, notifying the board
		 * that a unit has been dealt damage.
		 */
		int id;
		Message damageMessage;
		synchronized (this) {
			id = localMessageCounter++;
		
			damageMessage = new Message();
			damageMessage.put("request", MessageRequest.dealDamage);
			damageMessage.put("x", x);
			damageMessage.put("y", y);
			damageMessage.put("damage", damage);
			damageMessage.put("id", id);
		}
	}

	@Override
	public void healDamage(int x, int y, int healed) {
		/* Create a new message, notifying the board
		 * that a unit has been healed.
		 */
		int id;
		Message healMessage;
		synchronized (this) {
			id = localMessageCounter++;

			healMessage = new Message();
			healMessage.put("request", MessageRequest.healDamage);
			healMessage.put("x", x);
			healMessage.put("y", y);
			healMessage.put("healed", healed);
			healMessage.put("id", id);
		}
	}

	@Override
	public int getMaxHitPoints() {
		/* Create a new message, notifying the board
		 * that a unit has been healed.
		 */
		int maxHitPoints = -1;
		Message maxHitPointsMessage;
		synchronized (this) {
			maxHitPointsMessage = new Message();
			// TODO: Fix get messages, how they know which player to address, how to know what type the return type is.
			maxHitPoints = (int) maxHitPointsMessage.get("maxHitPoints");
		}
		return maxHitPoints;
	}

	@Override
	public int getUnitID() {
		int unitID = -1;
		Message unitIDMessage;
		synchronized (this) {
			unitIDMessage = new Message();
			// TODO: Fix get messages, how they know which player to address, how to know what type the return type is.
			unitID = (int) unitIDMessage.get("unitID");
		}
		return unitID;
	}

	@Override
	public int getX() {
		int x = -1;
		Message xMessage;
		synchronized (this) {
			xMessage = new Message();
			// TODO: Fix get messages, how they know which player to address, how to know what type the return type is.
			x = (int) xMessage.get("x");
		}
		return x;
	}

	@Override
	public int getY() {
		int y = -1;
		Message yMessage;
		synchronized (this) {
			yMessage = new Message();
			// TODO: Fix get messages, how they know which player to address, how to know what type the return type is.
			y = (int) yMessage.get("x");
		}
		return y;
	}

	@Override
	public int getHitPoints() {
		int hitPoints = -1;
		Message hitPointsMessage;
		synchronized (this) {
			hitPointsMessage = new Message();
			// TODO: Fix get messages, how they know which player to address, how to know what type the return type is.
			hitPoints = (int) hitPointsMessage.get("hitPoints");
		}
		return hitPoints;
	}

	@Override
	public int getAttackPoints() {
		int attackPoints = -1;
		Message attackPointsMessage;
		synchronized (this) {
			attackPointsMessage = new Message();
			// TODO: Fix get messages, how they know which player to address, how to know what type the return type is.
			attackPoints = (int) attackPointsMessage.get("hitPoints");
		}
		return attackPoints;
	}

	@Override
	public void moveUnit(int x, int y) {
		/* Create a new message, notifying the board
		 * that a unit has been healed.
		 */
		int id;
		Message moveMessage;
		synchronized (this) {
			id = localMessageCounter++;

			moveMessage = new Message();
			moveMessage.put("request", MessageRequest.healDamage);
			moveMessage.put("x", x);
			moveMessage.put("y", y);
			moveMessage.put("id", id);
		}
	}

	@Override
	public void disconnect() {
		/* Create a new message, notifying the server
		 * that this client wants to disconnect.
		 */
		int id;
		Message disconnectMessage;
		synchronized (this) {
			id = localMessageCounter++;

			disconnectMessage = new Message();
			disconnectMessage.put("request", MessageRequest.disconnect);
			disconnectMessage.put("id", id);
		}
	}
	
}
