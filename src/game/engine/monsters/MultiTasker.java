package game.engine.monsters;
// monsters package
import game.engine.Role;


public class MultiTasker extends Monster {
private int normalSpeedTurns;
public MultiTasker(String name, String description, Role role, int energy){
	super( name,  description,  role,  energy);	 
	normalSpeedTurns = 0;
}
public int getNormalSpeedTurns(){
	return normalSpeedTurns;
}
public void setNormalSpeedTurns(int n){
	normalSpeedTurns = n;
}
public String toString(){
	return super.toString() + "\n" + "NormalSpeedTurns : " + normalSpeedTurns;
}
}

