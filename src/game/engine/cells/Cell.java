package game.engine.cells;

import game.engine.monsters.Monster;

public class Cell {
	private String name;
	private Monster monster;
	
	public String getName() {
		return name;
	}
	public Monster getMonster() {
		return monster;
	}
	public void setMonster(Monster monster) {
		this.monster=monster;
	}
	public Cell() {
		this.monster = null;
	}
	public Cell(String name) {
		this.name = name;
		this.monster = null;
	}
	
}
