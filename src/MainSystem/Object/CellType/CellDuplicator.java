package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Manager.ManagerCell.ManagerDuplicator;

public class CellDuplicator extends Cell {

    public ManagerDuplicator managerDuplicator;

    public CellDuplicator(double x, double y, int rx, int ry) {
        super(x, y, rx, ry);
        cellPart = TypeCellPart.duplicator;
        managerDuplicator = new ManagerDuplicator(this);
    }

    public ManagerDuplicator getManagerDuplicator(){
        return managerDuplicator;
    }
    
    public void setDuplicator(int duplicatorCellPart){
        managerDuplicator.setDuplicateCellPart(duplicatorCellPart);
    }
    
    public boolean isDuplicatorPart(int duplicatorCellPart){
        return getManagerDuplicator().getDuplicatorCellPart() == duplicatorCellPart;
    }
    
    public void updateTeleport(){
        Cell[] teleports = HandlerCell.getCellDuplicator(getManagerDuplicator().getDuplicatorCellPart());
        if(teleports != null) for(Cell c : teleports) if(c != this){
            getManagerDuplicator().setDuplicatorCell(c);
        }
    }
    
}