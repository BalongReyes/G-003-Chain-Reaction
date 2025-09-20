package MainSystem.Object.CellType;

import DataSystem.Data.Player;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Manager.ManagerCell.ManagerDuplicator;
import java.util.List;

public class CellDuplicator extends Cell {

    public ManagerDuplicator managerDuplicator;
    private boolean isSyncing = false;

    public CellDuplicator(double x, double y, int rx, int ry) {
        super(x, y, rx, ry);
        cellPart = TypeCellPart.duplicator;
        managerDuplicator = new ManagerDuplicator(this);
    }

    public ManagerDuplicator getManagerDuplicator() {
        return managerDuplicator;
    }

    public void setDuplicator(int duplicatorCellPart) {
        managerDuplicator.setDuplicateCellPart(duplicatorCellPart);
    }

    public boolean isDuplicatorPart(int duplicatorCellPart) {
        return getManagerDuplicator().getDuplicatorCellPart() == duplicatorCellPart;
    }

    public void updateDuplicator() {
        List<Cell> duplicators = HandlerCell.getCellDuplicator(getManagerDuplicator().getDuplicatorCellPart());
        if (duplicators != null) {
            for (Cell c : duplicators) {
                if (c != this) {
                    getManagerDuplicator().addDuplicatorCell(c);
                }
            }
        }
    }

    @Override
    public void confirmAddAtoms(Player player, boolean explodeAdd) {
        if (isSyncing) {
            super.confirmAddAtoms(player, explodeAdd);
            return;
        }

        isSyncing = true;
        super.confirmAddAtoms(player, explodeAdd);
        for (Cell duplicatorCell : getManagerDuplicator().getDuplicatorCells()) {
            if (duplicatorCell != null) {
                ((CellDuplicator) duplicatorCell).isSyncing = true;
                duplicatorCell.confirmAddAtoms(player, explodeAdd);
                ((CellDuplicator) duplicatorCell).isSyncing = false;
            }
        }
        isSyncing = false;
    }

    @Override
    public void setPop(Player popPlayer) {
        if (this.pop) {
            return;
        }
        super.setPop(popPlayer);
        for (Cell duplicatorCell : getManagerDuplicator().getDuplicatorCells()) {
            if (duplicatorCell != null) {
                duplicatorCell.setPop(popPlayer);
            }
        }
    }
}