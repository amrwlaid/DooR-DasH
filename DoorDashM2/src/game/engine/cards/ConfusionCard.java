package game.engine.cards;

import game.engine.monsters.Monster;
import game.engine.Role;
public class ConfusionCard extends Card {
	private int duration;
	
	public ConfusionCard(String name, String description, int rarity, int duration) {
		super(name, description, rarity, false);
		this.duration = duration;
	}
	 
	public int getDuration() {
		return duration;
	}

	
	public void performAction(Monster Player, Monster opponent) {
		Role tempRole = Player.getRole();
		Player.setRole(opponent.getRole());
		opponent.setRole(tempRole);
		Player.setConfusionTurns(this.getDuration());
		opponent.setConfusionTurns(this.getDuration());
	}

}
