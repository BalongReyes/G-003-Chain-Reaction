package MainSystem.Object.CellType;

import DataSystem.Data.Player;
import DataSystem.Type.TypeCellPart;
import MainSystem.Methods.MethodsNumber;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Manager.ManagerCell.ManagerDuplicator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class CellDuplicator extends Cell {

    public ManagerDuplicator managerDuplicator;
    private boolean isSyncing = false;
    private int animationTick = 0;
    private int startAngle = 0;

    public CellDuplicator(double x, double y, int rx, int ry) {
        super(x, y, rx, ry);
        cellPart = TypeCellPart.duplicator;
        managerDuplicator = new ManagerDuplicator(this);
        startAngle = MethodsNumber.getRandomNumber(0, 359);
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

    @Override
    protected void tickAnimations() {
        super.tickAnimations();
        animationTick++;
        if (animationTick > 50) {
            animationTick = 0;
            startAngle = (startAngle + 5) % 360;
        }
    }

    @Override
    public void renderLayer2(Graphics2D g) {
        super.renderLayer2(g);
        if (isCellPart(TypeCellPart.space)) {
            return;
        }
        drawDesign(g);
    }

    private void drawDesign(Graphics2D g) {
        g.setColor(Color.lightGray);
        g.setStroke(new BasicStroke(2.0F));
        g.drawArc(getX() + 5, getY() + 5, 30, 30, startAngle, 90);
        g.drawArc(getX() + 5, getY() + 5, 30, 30, startAngle + 180, 90);
        g.setStroke(new BasicStroke(1.0F));
    }
}