// src/ManagerSystem/Manager/ManagerCell/ManagerTerritory.java

package ManagerSystem.Manager.ManagerCell;

import DataSystem.Data.Player;
import DataSystem.State.StateTerritory;
import MainSystem.Object.Cell;

public class ManagerTerritory{

    public Cell parent;
    public Player territory = Player.Dead;

    public ManagerTerritory(Cell parent){
        this.parent = parent;
    }

    public void reset(){
        if(this.territory != null){
            this.territory = Player.Dead;
            resetTerritoryDecay();
            resetTerritoryDestroy();
        }
    }

    public void toggleTerritory(){
        this.territory = this.territory == null ? Player.Dead : null;
        resetTerritoryDecay();
        resetTerritoryDestroy();
    }

    public void setTerritory(Player territoryDataPlayer){
        this.territory = territoryDataPlayer;
        resetTerritoryDecay();
        resetTerritoryDestroy();
    }

    public Player getTerritory(){
        return territory;
    }

    public boolean territoryCheckOwner(Player player){
        return territory != null && territory == player;
    }
    
    public boolean territoryCheckOwner(Player... players){
        if(territory == null) return false;
        for(Player p : players){
            if(territory == p) return true;
        }
        return false;
    }

    public boolean territoryOwned(){
        return territory != null && this.territory != Player.Dead;
    }

    public boolean territoryNotOwned(){
        return territory != null && this.territory == Player.Dead;
    }

// Destroy ---------------------------------------------------------------------------------------------------
    
    public int territoryDestroy = 0;
    
    public void resetTerritoryDestroy(){
        territoryDestroy = 0;
    }
    
    public void incrementTerritoryDestroy(){
        territoryDestroy++;
    }
    
    public void setTerritoryDestroy(int territoryDestroy){
        this.territoryDestroy = territoryDestroy;
    }

    public int getTerritoryDestroy(){
        return territoryDestroy;
    }
    
// Decay -----------------------------------------------------------------------------------------------------
    
    private int territoryDecay = 0;
    
    public void resetTerritoryDecay(){
        territoryDecay = 0;
    }
    
    public void incrementDecay(){
        territoryDecay++;
    }
    
    public void setTerritoryDecay(int territoryDecay){
        this.territoryDecay = territoryDecay;
    }

    public int getTerritoryDecay(){
        return territoryDecay;
    }
    
// State -----------------------------------------------------------------------------------------------------
    
    public void setStateCell(StateTerritory sC){
        setTerritory(sC.getTerritory());
        setTerritoryDecay(sC.getTerritoryDecay());
        setTerritoryDestroy(sC.getTerritoryDestroy());
    }
}