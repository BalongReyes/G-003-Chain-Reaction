package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import java.awt.Color;
import java.awt.Graphics2D;

public class CellBlackHole extends Cell {

    public CellBlackHole(double x, double y, int rx, int ry) {
        super(x, y, rx, ry);
        cellPart = TypeCellPart.blackHole;
    }

    @Override
    public boolean supportDiagonal() {
        return true;
    }

    @Override
    public void renderLayer2(Graphics2D g) {
        if (isCellSpace())
            return;

        g.setColor(new Color(25, 25, 30));
        int coreSize = 34;
        int offset = (40 - coreSize) / 2;
        g.fillOval(getX(offset), getY(offset), coreSize, coreSize);

        g.setColor(new Color(10, 10, 15));
        coreSize = 20;
        offset = (40 - coreSize) / 2;
        g.fillOval(getX(offset), getY(offset), coreSize, coreSize);
    }
}
