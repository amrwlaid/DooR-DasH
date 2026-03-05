package game.engine.dataloader;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.Schemer;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Monster;
import game.engine.Role;
import javax.smartcardio.Card;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

public class DataLoader {
	 private static String CARDS_File_NAME = "cards.csv";
	 private static String Cells_File_NAME = "cells.csv";
	 private static String Monsters_File_NAME = "monsters.csv";

	 public static ArrayList<Card> readCards() throws IOException {
		 	BufferedReader r = new BufferedReader(new FileReader(CARDS_File_NAME));
		    ArrayList<Card> arr = new ArrayList<>();

		    String line ; 

		    while ((line = r.readLine()) != null) {  // read each card
		        String[] data = line.split(",");     // split CSV columns

		        String type = data[0];
		        String name = data[1];
		        String description = data[2];
		        int rarity = Integer.parseInt(data[3]);
		        
		        if (type.equals("SwapperCard"))
		        {
		        	SwapperCard card = new SwapperCard(type , name , description , rarity , true);	
		        arr.add(card);
		        }
		        else if (type.equals("ShieldCard") ){
		        	ShieldCard card = new ShieldCard(type , name , description , rarity , true);	
			        arr.add(card);
			        	
		        }
		        else if (type.equals("EnergyStealCard"))
		        {
		        	EnergyStealCard card = new EnergyStealCard(type , name , description , rarity , true , data[4]);	
		        arr.add(card);
		        }
		        else if (type.equals("forStartOverCard"))
		        {
		        	forStartOverCard card = new forStartOverCard(type , name , description , rarity ,  data[4]);	
		        	arr.add(card);
		        }
		        else {
		        	ConfusionCard card = new ConfusionCard(type , name , description , rarity , false , data[4]);
		        arr.add(card);
		        }
			}
		    r.close();
		    return arr;
	 }
		 
	 public static ArrayList<Cell> readCells() throws IOException{
		   BufferedReader r = new BufferedReader(new FileReader(Cells_File_NAME));
		    ArrayList<Cell> arr = new ArrayList<>();

		    String line ; 
		    while ((line = r.readLine()) != null) {  
		        String[] data = line.split(",");     
		    	if (data.length == 3){
		    		DoorCell cell = new DoorCell(data[0] , data[1] , Integer.parseInt(data[2]));
		    		arr.add(cell);
		    	}
		    	else{
		    		if (Integer.parseInt(data[1] ) < 0) {
		    			ConveyorBelt cell = new ConveyorBelt(data[0] , Integer.parseInt(data[1] ));
		    			arr.add(cell);
		    		}
		    		else {
						ContaminationSock cell = new ContaminationSock(data[0] , Integer.parseInt(data[1] ));
	    				arr.add(cell);
					}
				}
		    }


		    r.close();
		    return arr;
		 
		 
		 
	 }
	 public static ArrayList<Monster> readMonsters() throws IOException {
		 BufferedReader r = new BufferedReader(new FileReader(Cells_File_NAME));
		 ArrayList<Monster> arr = new ArrayList<>();

		 String line;
		 while ((line = r.readLine()) != null) {
			 String[] data = line.split(",");
			 Role role;
			 if (data[3].equals("laugher")) role = Role.LAUGHER;
			 else role = Role.SCARER;
			 if (data[0].equals("DYNAMO")) {

				 Dynamo m = new Dynamo(data[1], data[2], role, Integer.parseInt(data[4]));
				 arr.add(m);
			 } else if (data[0].equals("SCHEMER")) {

				 Schemer m = new Schemer(data[1], data[2], role, Integer.parseInt(data[4]));
				 arr.add(m);
			 } else if (data[0].equals("MULTITASKER")) {
				 MultiTasker m = new MultiTasker(data[1], data[2], role, Integer.parseInt(data[4]));
				 arr.add(m);
			 } else {
				 Dasher m = new Dasher(data[1], data[2], role, Integer.parseInt(data[4]));
				 arr.add(m);
			 }





		 }


		 r.close();
		 return arr;


	 }

}

