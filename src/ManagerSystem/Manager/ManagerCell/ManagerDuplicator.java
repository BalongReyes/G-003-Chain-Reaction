package ManagerSystem.Manager.ManagerCell;

import MainSystem.Object.Cell;

public class ManagerDuplicator{

    private Cell parent;
    private int duplicateCellPart;

    public ManagerDuplicator(Cell parent){
        this.parent = parent;
    }

    public void setDuplicateCellPart(int duplicateCellPart){
        this.duplicateCellPart = duplicateCellPart;
    }

    public int getDuplicatorCellPart(){
        return duplicateCellPart;
    }
    
// ===========================================================================================================
    
    private Cell duplicatorCell;

    public void setDuplicatorCell(Cell duplicatorCell){
        this.duplicatorCell = duplicatorCell;
    }

    public Cell getDuplicatorCell(){
        return duplicatorCell;
    }
    
}
