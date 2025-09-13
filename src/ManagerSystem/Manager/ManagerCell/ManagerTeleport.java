package ManagerSystem.Manager.ManagerCell;

import MainSystem.Object.Cell;

public class ManagerTeleport{

    private Cell parent;
    private int teleportCellPart;

    public ManagerTeleport(Cell parent){
        this.parent = parent;
    }

    public void setTeleportCellPart(int teleportCellPart){
        this.teleportCellPart = teleportCellPart;
    }

    public int getTeleportCellPart(){
        return teleportCellPart;
    }
    
// ===========================================================================================================
    
    private Cell teleportCell;
    
    public void setTeleportCell(Cell teleportCell){
        this.teleportCell = teleportCell;
    }

    public Cell getTeleportCell(){
        return teleportCell;
    }
    
}
