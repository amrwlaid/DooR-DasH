package game.engine.exceptions;

public class OutOfEnergyException extends GameActionException{
    static private final String MSG = "Not Enough Energy for Power Up";

    public OutOfEnergyException(){
        super(MSG);
    }

    public OutOfEnergyException(String message){
        super(message);
    }
}
