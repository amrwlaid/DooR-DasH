package game.engine.dataloader;
import game.engine.cards.*;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.Schemer;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Monster;
import game.engine.Role;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class DataLoader {
	 private static final String CARDS_FILE_NAME = "cards.csv";
	 private static final String CELLS_FILE_NAME = "cells.csv";
	 private static final String MONSTERS_FILE_NAME = "monsters.csv";

	 public static ArrayList<Card> readCards() throws IOException {
		 	BufferedReader r = new BufferedReader(new FileReader(CARDS_FILE_NAME));
		    ArrayList<Card> loadedCards = new ArrayList<>();

		    ArrayList<String > cardsList = new ArrayList<String>();
		    String line = r.readLine(); 
		    while (line  != null) {  
		    	cardsList.add(line);
		    	 line = r.readLine();	
		    }
		    for (int i = 0 ; i < cardsList.size() ; i ++){
		    		        String[] csvRow = cardsList.get(i).trim().split(",");
		        String type = csvRow[0].trim();
		        String name = csvRow[1].trim();
		        String description = csvRow[2].trim();
		        int rarity = Integer.parseInt(csvRow[3].trim());
		        
		        if (type.equals("SWAPPER"))
		        {
		        	SwapperCard card = new SwapperCard(name , description , rarity);
		        	loadedCards.add(card);
		        }
		        else if (type.equals("SHIELD") ){
		        	ShieldCard card = new ShieldCard(name , description , rarity);
		        	loadedCards.add(card);
			        	
		        }
		        else if (type.equals("ENERGYSTEAL"))
		        {
		        	EnergyStealCard card = new EnergyStealCard(name , description , rarity ,  Integer.parseInt(csvRow[4].trim()));
		        	loadedCards.add(card);
		        }
		        else if (type.equals("STARTOVER"))
		        {
					boolean lucky;
					if (csvRow[4].trim().equals("true")){lucky = true;}
					else {lucky = false;}
		        	StartOverCard card = new StartOverCard(name , description , rarity ,  lucky);
		        	loadedCards.add(card);
		        }
		        else 
		        {
		        	ConfusionCard card = new ConfusionCard(name , description , rarity , Integer.parseInt(csvRow[4].trim()));
		        	loadedCards.add(card);
		        }
			}
		    r.close();
		    return loadedCards;
	 }
	 public static ArrayList<Cell> readCells() throws IOException{
		   BufferedReader r = new BufferedReader(new FileReader(CELLS_FILE_NAME));
		    ArrayList<Cell> loadedCells = new ArrayList<>();
		    ArrayList<String > cellsList = new ArrayList<String>();
		    String line = r.readLine(); 
		    while (line  != null) {  
		    	cellsList.add(line);
		    	 line = r.readLine();	
		    }
		    for (int i = 0 ; i < cellsList.size() ; i ++){
		        String[] csvRow = cellsList.get(i).trim().split("," , -1);     
		    	if (csvRow.length == 3){
					Role role;
					if (csvRow[1].trim().equals("SCARER")){role = Role.SCARER;}
					else {role = Role.LAUGHER;}

					Cell cell = new DoorCell(csvRow[0].trim() , role , Integer.parseInt(csvRow[2].trim()));
					loadedCells.add(cell);
		    	}
		    	else{
		    		if (Integer.parseInt(csvRow[1].trim() ) < 0) {
		    			Cell cell = new ConveyorBelt(csvRow[0].trim() , Integer.parseInt(csvRow[1].trim() ));
		    			loadedCells.add(cell);
		    		}
		    		else {
						Cell cell = new ContaminationSock(csvRow[0].trim() , Integer.parseInt(csvRow[1].trim() ));
						loadedCells.add(cell);
					}
				}
		    }


		    r.close();
		    return loadedCells;
		 
		 
		 
	 }
	 public static ArrayList<Monster> readMonsters() throws IOException {
		 BufferedReader r = new BufferedReader(new FileReader(MONSTERS_FILE_NAME));
		 ArrayList<Monster> loadedMonsters = new ArrayList<>();
		 ArrayList<String > monstersList = new ArrayList<String>();
		 String line = r.readLine();
		 while (line != null) {
monstersList.add(line);		



			  line = r.readLine();
		 }
for (int i = 0 ; i < monstersList.size() ; i ++)
	
		 
{
	 String[] csvRow = monstersList.get(i).split("," , -1);
	 Role role;
	 if (csvRow[3].trim().equals("LAUGHER")) role = Role.LAUGHER;
	 else role = Role.SCARER;
	 if (csvRow[0].trim().equals("DYNAMO")) {

		 Monster monster = new Dynamo(csvRow[1].trim(), csvRow[2].trim(), role, Integer.parseInt(csvRow[4].trim()));
		 loadedMonsters.add(monster);
	 } else if (csvRow[0].trim().equals("SCHEMER")) {

		 Monster monster = new Schemer(csvRow[1].trim(), csvRow[2].trim(), role, Integer.parseInt(csvRow[4].trim()));
		 loadedMonsters.add(monster);
	 } else if (csvRow[0].trim().equals("MULTITASKER")) {
		 Monster monster = new MultiTasker(csvRow[1].trim(), csvRow[2].trim(), role, Integer.parseInt(csvRow[4].trim()));
		 loadedMonsters.add(monster);
	 } else {
		 Monster monster = new Dasher(csvRow[1].trim(), csvRow[2].trim(), role, Integer.parseInt(csvRow[4].trim()));
		 loadedMonsters.add(monster);
	 }

	
}
		 r.close();
		 return loadedMonsters;


	 }


public static void main(String [ ]args) throws IOException{
	ArrayList<Monster> m = readMonsters();;
for(int i = 0 ; i < m.size(); i ++)
	System.out.println(m.get(i));
	
	
}
}



