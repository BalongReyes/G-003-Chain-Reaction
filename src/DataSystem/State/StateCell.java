// src/DataSystem/State/StateCell.java

package DataSystem.State;

import DataSystem.Data.Player;
import DataSystem.Data.Position;

public class StateCell{

    public Player[] dataPlayerArray;
    public Position lastPosition;
    public StateTerritory stateTerritory;
    
    public StateCell(Player[] dataPlayerArray, Position lastPosition, StateTerritory stateTerritory){
        this.dataPlayerArray = dataPlayerArray;
        this.lastPosition = lastPosition;
        this.stateTerritory = stateTerritory;
    }

    public Player[] getDataPlayerArray(){
        return dataPlayerArray;
    }

    public Position getLastPosition(){
        return lastPosition;
    }

    public StateTerritory getStateTerritory() {
        return stateTerritory;
    }

}