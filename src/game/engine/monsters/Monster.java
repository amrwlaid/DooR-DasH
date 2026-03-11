package game.engine.monsters;
import game.engine.Role;

public abstract class Monster implements Comparable<Monster>{
	private String name;
	private String description;
	private Role role;
	private  Role originalRole;
	private int energy;
	private int position; // 0 - 99
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;

	public Monster(String name, String description, Role originalRole, int energy){
		this.name = name;
		this.description = description;
		this.originalRole = originalRole;
		this.role = originalRole;
		if (energy >= 0) this.energy = energy;
		else this.energy = 0;
		this.position = this.confusionTurns = 0;
		this.shielded = this.frozen = false;
	}
//Name
public String getName(){
	return name;}
//description
public String getDescription(){
	return description;}
//original role
public Role getOriginalRole(){
	return originalRole;}
// role
public Role getRole(){
	return role;}
public void setRole(Role r){
	role = r;
}
//energy
public int getEnergy(){
	return energy;}
public void setEnergy(int e) {
		if (e >= 0)  energy = e;
		else energy = 0;


	}
//position
public int getPosition(){
	return position;
}

	public void setPosition(int p){

		if (p >= 100){ p -= 100;}
		position = p;
	}
//freezing
public boolean isFrozen(){
	return frozen;
}
public void setFrozen(boolean f){
	frozen = f;
}
//shielding
public boolean isShielded(){
	return shielded;
}
public void setShielded(boolean f){
	shielded = f;
}
//confusing
public int getConfusionTurns(){
	return confusionTurns;
}

public void setConfusionTurns(int c){
	confusionTurns = c;
}
//comparing
public int compareTo(Monster o){
	return position - o.position;
}
//display

public String toString(){
return "Name : " + name + "\n" +
"Description : " + description + "\n" + 
"Role : "	+ role + "\n" +
"OriginalRole : " + originalRole + "\n" + 
"Energy : " + energy + "\n" + 
"Position : " + position + "\n" + 
"isFreezing : " + frozen + "\n" +
"isShielded : " + shielded + "\n" + 
"ConfusionTurns : " + confusionTurns;
}	
	
	
}

