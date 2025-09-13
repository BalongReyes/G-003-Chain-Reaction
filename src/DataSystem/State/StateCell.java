
package DataSystem.State;

import DataSystem.Data.Player;
import DataSystem.Data.Position;

public class StateCell{

    public Player[] dataPlayerArray;
    public Position lastPosition;
    
    public StateCell(Player[] dataPlayerArray, Position lastPosition){
        this.dataPlayerArray = dataPlayerArray;
        this.lastPosition = lastPosition;
    }

    public Player[] getDataPlayerArray(){
        return dataPlayerArray;
    }

    public Position getLastPosition(){
        return lastPosition;
    }

}
