package game.engine.cells;  

import game.engine.monsters.*;
import game.engine.exceptions.*;
public class MonsterCell extends Cell {
	private Monster cellMonster;

	public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}

	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		if(landingMonster.getRole()==this.cellMonster.getRole()) {
			try{
					landingMonster.executePowerupEffect(opponentMonster);
			} 
			catch (OutOfEnergyException e) {
					e.printStackTrace();
			}	
		}else{
			
			if(landingMonster.getEnergy()>this.cellMonster.getEnergy()) {
				int landingEnergy = landingMonster.getEnergy();
				int CellEnergy = this.cellMonster.getEnergy();
				landingMonster.alterEnergy(CellEnergy - landingEnergy);
				this.cellMonster.setEnergy(landingEnergy);
			}
		}
		
	}

}
