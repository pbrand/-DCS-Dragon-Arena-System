package client;

import common.MessageRequest;

import common.Enums.UnitType;
import common.Enums.Direction;
import core.IUnit;

public class PlayerController implements IUnit {
	// Is used for mapping an unique id to a message sent by this unit
	private int localMessageCounter = 0;
	protected int timeBetweenTurns;
	
	public PlayerController() {
		// Create a new player at server side
		// Retrieve battlefield and player details
		// run()
		// disconnect
		run();
		disconnect();
	}
	
	private void run() {
		// TODO: - remove auto play based on math.random (so handle real user-input).
		//       - Update the targeting system.
		boolean running = true;
		Direction direction;
		UnitType adjacentUnitType;
		int targetX = 0, targetY = 0;
		
		while(running) {
			try {			
				/* Sleep while the player is considering its next move */
				Thread.currentThread().sleep((int)(timeBetweenTurns * 500 * GameState.GAME_SPEED));

				/* Stop if the player runs out of hitpoints */
				if (getHitPoints() <= 0)
					break;

				// Randomly choose one of the four wind directions to move to if there are no units present
				direction = Direction.values()[ (int)(Direction.values().length * Math.random()) ];
				adjacentUnitType = UnitType.undefined;

				switch (direction) {
				case up:
					if (this.getY() <= 0)
						// The player was at the edge of the map, so he can't move north and there are no units there
						continue;
					
					targetX = this.getX();
					targetY = this.getY() - 1;
					break;
				case down:
					if (this.getY() >= BattleField.MAP_HEIGHT - 1)
						// The player was at the edge of the map, so he can't move south and there are no units there
						continue;

					targetX = this.getX();
					targetY = this.getY() + 1;
					break;
				case left:
					if (this.getX() <= 0)
						// The player was at the edge of the map, so he can't move west and there are no units there
						continue;

					targetX = this.getX() - 1;
					targetY = this.getY();
					break;
				case right:
					if (this.getX() >= BattleField.MAP_WIDTH - 1)
						// The player was at the edge of the map, so he can't move east and there are no units there
						continue;

					targetX = this.getX() + 1;
					targetY = this.getY();
					break;
			}

				// Get what unit lies in the target square
				adjacentUnitType = this.getType(targetX, targetY);
				
				switch (adjacentUnitType) {
					case undefined:
						// There is no unit in the square. Move the player to this square
						this.moveUnit(targetX, targetY);
						break;
					case player:
						// There is a player in the square, attempt a healing
						this.healDamage(targetX, targetY, getAttackPoints());
						break;
					case dragon:
						// There is a dragon in the square, attempt a dragon slaying
						this.dealDamage(targetX, targetY, getAttackPoints());
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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

	@Override
	public UnitType getType(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
