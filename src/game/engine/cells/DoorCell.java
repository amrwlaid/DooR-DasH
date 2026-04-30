package game.engine.cells;
import java.util.*;
import game.engine.Board;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;
public class DoorCell extends Cell implements CanisterModifier {
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	
	public Role getRole() {
		return role;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean isActivated) {
		this.activated = isActivated;
	}
	
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		if (monster.getRole() == this.role) {
			monster.alterEnergy(canisterValue);
		} else {
			monster.alterEnergy(-canisterValue);
		}
	}
	
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		if(this.activated) {
			return;
		}
		ArrayList<Monster> teammates=new ArrayList<>();
		ArrayList<Monster> stationed=Board.getStationedMonsters();
		if(stationed!=null) {
			for(int i=0;i<stationed.size();i++) {
				Monster m=stationed.get(i);
				if(m.getRole()==landingMonster.getRole()) {
					teammates.add(m);
				}
			}
		}
		if(landingMonster.getRole()==this.role) {
			this.modifyCanisterEnergy(landingMonster, energy);
			for(int i=0;i<teammates.size();i++) {
				this.modifyCanisterEnergy(teammates.get(i), this.energy);
			}
			this.setActivated(true);
		}else{
			boolean shieldFound=false;
			if(landingMonster.isShielded()==true) {
				shieldFound=true;
				landingMonster.setShielded(false);
			}else{
				for(int i=0;i<teammates.size();i++) {
					if(teammates.get(i).isShielded()==true) {
						shieldFound=true;
						teammates.get(i).setShielded(false);
						break;
					}
				}
			}
			if(shieldFound==true) {
				return;
			}else{
				this.modifyCanisterEnergy(landingMonster, energy);
				for(int i=0;i<teammates.size();i++) {
					this.modifyCanisterEnergy(teammates.get(i), energy);
				}
				this.setActivated(true);
			}
		}	
	}
}
