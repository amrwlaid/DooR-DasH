package game.engine;

import game.engine.dataloader.DataLoader;
import game.engine.monsters.Monster;

import java.io.IOException;
import java.util.ArrayList;

public class Game {

    private Board board;
    private ArrayList<Monster> allMonsters;
    private Monster player;
    private Monster opponent;
    private Monster current;



    public Game(Role playerRole) throws IOException{

        Role oppnentRole;
        if (playerRole == Role.LAUGHER) {oppnentRole = Role.SCARER;}
        else {oppnentRole = Role.LAUGHER;}

        board = new Board(DataLoader.readCards());
        allMonsters = DataLoader.readMonsters();
        player = selectRandomMonsterByRole(playerRole);
        opponent = selectRandomMonsterByRole(oppnentRole);
        current = player;

    }


    private Monster selectRandomMonsterByRole(Role role){
        Monster monster;

        while (true){
            int random = (int) (Math.random()*allMonsters.size());
            if (allMonsters.get(random).getRole() == role){
                monster = allMonsters.get(random);
                break;
            }
        }

        return monster;

    }





    public Board getBoard() {
        return board;
    }

    public ArrayList<Monster> getAllMonsters() {
        return allMonsters;
    }

    public Monster getOpponent(){
        return opponent;
    }

    public Monster getPlayer() {
        return player;
    }

    public Monster getCurrent() {
        return current;
    }

    public void setCurrent(Monster current) {
        this.current = current;
    }




}
