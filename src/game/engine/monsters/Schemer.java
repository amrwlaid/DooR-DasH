package game.engine.monsters;
import java.util.*;
import game.engine.Board;
import game.engine.Constants;

import game.engine.Role;
public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	private int stealEnergyFrom(Monster target){
		int what_i_will_steal = (target.getEnergy() > Constants.SCHEMER_STEAL) ?  Constants.SCHEMER_STEAL : target.getEnergy();
		target.setEnergy(target.getEnergy() - what_i_will_steal);
		return what_i_will_steal;
	}
	
	
	public void setEnergy(int e){
		super.setEnergy(e + Constants.SCHEMER_STEAL);
	}
	@Override
	public void executePowerupEffect(Monster opponentMonster) {

		int net = 0 ;
ArrayList<Monster> arr = Board.getStationedMonsters();
net += stealEnergyFrom(opponentMonster);
for (Monster m : arr) net += stealEnergyFrom(m);
alterEnergy(net);
	}
}
