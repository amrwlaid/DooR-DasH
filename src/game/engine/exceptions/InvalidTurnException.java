package game.engine.exceptions;

public class InvalidTurnException extends GameActionException{
    static private final String MSG = "Action done on wrong turn";

    public InvalidTurnException(){
        super(MSG);
    }

    public InvalidTurnException(String message){
        super(message);
    }

}
