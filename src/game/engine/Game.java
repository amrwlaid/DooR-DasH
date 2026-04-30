package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
		
		ArrayList<Monster> tempArrayList = new ArrayList<Monster>();	
		for(int i = 0; i<allMonsters.size(); i++) {
			
			Monster monster = allMonsters.get(i);
		
			if (!( (monster == player) || (monster == opponent) )) 
				tempArrayList.add(monster);
		}
		allMonsters = tempArrayList;
		
		
		
		
		
		System.out.println(player.getName() + " player name");
		System.out.println(opponent.getName() + " opponent name");
		for(Monster monster: allMonsters)
			System.out.println(monster.getName());

		Board.setStationedMonsters(allMonsters);
	}
	
	public Board getBoard() {
		return board;
	}
	
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters; 
	}
	
	public Monster getPlayer() {
		return player;
	}
	
	public Monster getOpponent() {
		return opponent;
	}
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	
	
	
	private Monster getCurrentOpponent() {
		if (this.current == player) {
			return getOpponent();
		}
		return getPlayer();
	}
	
	private int rollDice() {
		return (int) (Math.random()*6 + 1);
	}
	
	public void usePowerup() throws OutOfEnergyException{
		
		if (this.current.getEnergy() >= Constants.POWERUP_COST) {
			current.executePowerupEffect(getCurrentOpponent());
			current.alterEnergy(-500);
		}
		else
			throw new OutOfEnergyException();
	}
	
	
	public void playTurn() throws InvalidMoveException{
		int roll = rollDice();
		if (current.isFrozen()) {
			current.setFrozen(false);
			this.switchTurn();
			return;
		}
		
		this.board.moveMonster(current, roll, getCurrentOpponent());
		
		
		this.switchTurn();
		
		
	
		
		
	}
	
	 private void switchTurn() {
		 current = getCurrentOpponent();
	 }
	
	
	 private boolean checkWinCondition(Monster monster) {
		 if (monster.getEnergy() >= 1000 && monster.getPosition() == 99) {
			return true;
		 }
		 
		 return false;
		 
	 }
	 
	 
	public Monster getWinner() {
		if (checkWinCondition(current)) {
			return current;
		}
		else if (checkWinCondition(getCurrentOpponent())) {
			return getCurrentOpponent();
		}
		return null;
		
		
	}
	
	
	public static void main(String[] args) {
		try {
			Game game = new Game(Role.SCARER);
			game.getBoard().initializeBoard(DataLoader.readCells());
			for(int i = 0; i<100; i++)
				System.out.println(game.getBoard().getCell(i).getName() + " i = "+i);
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}