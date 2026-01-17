package ManagerSystem.Manager.ManagerCell;

import DataSystem.Data.Player;
import DataSystem.State.StateShield;
import MainSystem.Object.Cell;

public class ManagerShield{

    public Cell parent;
    public Player shield = Player.Dead;

    public ManagerShield(Cell parent){
        this.parent = parent;
    }

    public void reset(){
        if(this.shield != null){
            this.shield = Player.Dead;
            resetShieldDecay();
            resetShieldDestroy();
        }
    }

    public void toggleShield(){
        this.shield = this.shield == null ? Player.Dead : null;
        resetShieldDecay();
        resetShieldDestroy();
    }

    public void setShield(Player shieldDataPlayer){
        this.shield = shieldDataPlayer;
        resetShieldDecay();
        resetShieldDestroy();
    }

    public Player getShield(){
        return shield;
    }

    public boolean shieldCheckOwner(Player player){
        return shield != null && shield == player;
    }
    
    public boolean shieldCheckOwner(Player... players){
        if(shield == null) return false;
        for(Player p : players){
            if(shield == p) return true;
        }
        return false;
    }

    public boolean shieldOwned(){
        return shield != null && this.shield != Player.Dead;
    }

    public boolean shieldNotOwned(){
        return shield != null && this.shield == Player.Dead;
    }

// Destroy ---------------------------------------------------------------------------------------------------
    
    public int shieldDestroy = 0;
    
    public void resetShieldDestroy(){
        shieldDestroy = 0;
    }
    
    public void incrementShieldDestroy(){
        shieldDestroy++;
    }
    
    public void setShieldDestroy(int shieldDestroy){
        this.shieldDestroy = shieldDestroy;
    }

    public int getShieldDestroy(){
        return shieldDestroy;
    }
    
// Decay -----------------------------------------------------------------------------------------------------
    
    private int shieldDecay = 0;
    
    public void resetShieldDecay(){
        shieldDecay = 0;
    }
    
    public void incrementDecay(){
        shieldDecay++;
    }
    
    public void setShieldDecay(int shieldDecay){
        this.shieldDecay = shieldDecay;
    }

    public int getShieldDecay(){
        return shieldDecay;
    }
    
// State -----------------------------------------------------------------------------------------------------
    
    public void setStateCell(StateShield sC){
        setShield(sC.getShield());
        setShieldDecay(sC.getShieldDecay());
        setShieldDestroy(sC.getShieldDestroy());
    }
}
