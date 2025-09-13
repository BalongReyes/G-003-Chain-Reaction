
package DataSystem.State;

import DataSystem.Data.Player;
import ManagerSystem.Manager.ManagerCell.ManagerTerritory;

public class StateTerritory{

    public Player territory;
    public int territoryDecay, territoryDestroy;
    
// Constructor ===============================================================================================
    
    public StateTerritory(ManagerTerritory managerTerritory){
        this.territory = managerTerritory.getTerritory();
        this.territoryDecay = managerTerritory.getTerritoryDecay();
        this.territoryDestroy = managerTerritory.getTerritoryDestroy();
    }
    
// Main Methods ==============================================================================================
    
    public Player getTerritory(){
        return territory;
    }

    public int getTerritoryDecay(){
        return territoryDecay;
    }

    public int getTerritoryDestroy(){
        return territoryDestroy;
    }

}
