
package DataSystem.State;

import DataSystem.Data.Player;
import ManagerSystem.Manager.ManagerCell.ManagerShield;

public class StateShield{

    public Player shield;
    public int shieldDecay, shieldDestroy;
    
// Constructor ===============================================================================================
    
    public StateShield(ManagerShield managerShield){
        this.shield = managerShield.getShield();
        this.shieldDecay = managerShield.getShieldDecay();
        this.shieldDestroy = managerShield.getShieldDestroy();
    }
    
// Main Methods ==============================================================================================
    
    public Player getShield(){
        return shield;
    }

    public int getShieldDecay(){
        return shieldDecay;
    }

    public int getShieldDestroy(){
        return shieldDestroy;
    }

}
