package game.engine;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.MonsterCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.monsters.Monster;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
		setCardsByRarity();
		reloadCards();
		
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}
	
	
	
	private int[] indexToRowCol(int index) {
		int row = index/10;
		
		int temp = index%10;
		int column;
		if(row%2 == 1) {
			column = 9 - temp;
		}
		else {
			column = temp;
		}
		
		int[] result = {row,column};
		
		return result;
	}
	
	
	public Cell getCell(int index) {
		int[] temp = this.indexToRowCol(index);
		return this.boardCells[temp[0]][temp[1]];
	}
	
	
	private void setCell(int index, Cell cell) {
		int[] temp = this.indexToRowCol(index);
		this.boardCells[temp[0]][temp[1]] = cell;
	}
	
	
	public void initializeBoard(ArrayList<Cell> specialCells) {
		ArrayList<Cell> cells = specialCells;
		
		//System.out.println(cells.size());
			
		//sets odd indicies to door cells
		for(int i = 0; i < 100; i++) {
			if(!cells.isEmpty() && i%2 == 1) {
				Cell cell = cells.remove(0);
				this.setCell(i, cell);
			}
			
			else if(i%2 == 0) {
				setCell(i, new Cell("rest cell"));
			}
		}
			
		//sets even indicies to rest cells
		/*for(int i = 0; i<50; i++) {
			setCell(2*i, new Cell("rest cell"));
		}*/
			/*
			public static final int[] MONSTER_CELL_INDICES = {2, 18, 34, 54, 82, 88};

		    public static final int[] CONVEYOR_CELL_INDICES = {6, 22, 44, 52, 66};

		    public static final int[] SOCK_CELL_INDICES = {32, 42, 74, 84, 98};

		    public static final int[] CARD_CELL_INDICES = {4, 12, 28, 36, 48, 56, 60, 76, 86, 90};
		    */
		/*
		System.out.println("cells length: " + cells.size());
		for(Cell cell : cells)
			System.out.println(cell.getName());
			*/
		int conveyorCellIndex = 0;
		int sockCellIndex = 0;
		int cardCellIndex = 0;
		
		for(int i = 0; i < 10; i++) {
			/*System.out.println("cells length: " + cells.size());
			for(Cell cell : cells)
				System.out.println(cell.getName());*/
			
			
			Cell cell = cells.remove(0);
			
			
			
			//System.out.println("cells length: " + cells.size() + " i = "+i);
			//System.out.println(cell.getName());
			//System.out.println("idfk");
				
			
			
				
			if(cell instanceof ConveyorBelt) {
				//System.out.println("conveyor");
				ConveyorBelt conveyorBelt = (ConveyorBelt) cell;
				this.setCell(Constants.CONVEYOR_CELL_INDICES[conveyorCellIndex], conveyorBelt);
				conveyorCellIndex++;
			}
				
			else if(cell instanceof ContaminationSock) {
				//System.out.println("sock");
				ContaminationSock contaminationSock = (ContaminationSock)cell;
				this.setCell(Constants.SOCK_CELL_INDICES[sockCellIndex], contaminationSock);
				sockCellIndex++;
			}
				
			else if(cell instanceof CardCell) {
				//System.out.println("cardcell");
				CardCell cardCell = (CardCell) cell;
				this.setCell(Constants.CARD_CELL_INDICES[cardCellIndex], cardCell);
				cardCellIndex++;
			}
			
		}
		
		
		
		
		for(int i = 0; i< Constants.CARD_CELL_INDICES.length; i++) {
			setCell(Constants.CARD_CELL_INDICES[i], new CardCell("card cell"));
		}
		

		
		for(int i = 0; i<Constants.MONSTER_CELL_INDICES.length; i++) {
			if (getStationedMonsters() != null && !getStationedMonsters().isEmpty() && getStationedMonsters().size() != i){
				Monster monster = getStationedMonsters().get(i);
				//System.out.println("i = "+i + " mosnter name = " + monster.getName());
				MonsterCell monsterCell = new MonsterCell(monster.getName(), monster);
				monster.setPosition(Constants.MONSTER_CELL_INDICES[i]);
				setCell(Constants.MONSTER_CELL_INDICES[i], monsterCell);
			}
			
			
		}	
			
		
			
			
			
			
			
			
		
		
		
		
		
		
		
	}
	
	
	private void setCardsByRarity() {
		
		
		ArrayList<Card> newOriginalCards = new ArrayList<>();
		
		
		for(int i = 0; i<Board.originalCards.size(); i++) {
			Card currentCard = Board.originalCards.get(i);
			
			int currentCardRarity = currentCard.getRarity();
			
			for(int j = 0; j < currentCardRarity; j++) {
				newOriginalCards.add(currentCard);
				
			}
		}
		
		Board.originalCards = newOriginalCards;
	}
	
	
	public static void reloadCards() {
		
		
		for(Card card : Board.originalCards) {
			cards.add(card);
		}
		
		Collections.shuffle(cards);
		
		
		
		
		
	}
	
	static public Card drawCard() {
		if (Board.cards.isEmpty()) {
			reloadCards();
		}
		return Board.cards.remove(0);
		
	}
	
	
	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException{
		int oldPosition = currentMonster.getPosition();
		currentMonster.move(roll);
		int currentConfusion = currentMonster.getConfusionTurns();
		Cell cell = getCell(currentMonster.getPosition());
		
		
		
		if (currentMonster.getPosition() == opponentMonster.getPosition()) {
			currentMonster.setPosition(oldPosition);
			throw new InvalidMoveException();
		}
		
		
		if (currentConfusion == currentMonster.getConfusionTurns() && currentMonster.getConfusionTurns() != 0) {
			currentMonster.decrementConfusion();
			opponentMonster.decrementConfusion();
		}
		cell.onLand(currentMonster, opponentMonster);
		
		
		updateMonsterPositions(currentMonster, opponentMonster);
		
	}
	
	
	private void updateMonsterPositions(Monster player, Monster opponent){
		
		for (int i = 0; i < 100; i++) {
			Cell cell = getCell(i);
			cell.setMonster(null);
		}
		
		for(Monster monster: stationedMonsters) {
			Cell cell = getCell(monster.getPosition());
			cell.setMonster(monster);
		}
		
		
		
	}
	
	
	 /*
	 void onLand(Monster landingMonster, Monster opponentMonster): A method that executes
	 the cell’s unique effect when a monster lands on it. A cell should keep track of the landing monster
	 by setting its attributes.
	 */
	
	
	
	
	
	
	
	
	
	
	
}
