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
    public void tickPopReady() {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                Cell c = main.handlerCell.getAllCell(this.rx + dx, this.ry + dy);
                if (c != null && !c.isCellSpace()) {
                    c.addFutureAtoms(popPlayer);
                }
            }
        }
    }

    @Override
    protected void drawExplodeSet(Graphics2D g) {
        g.setColor(this.explodeColor);
        double animation = 1 - this.explodeAnimation;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue;
                Cell c = main.handlerCell.getAllCell(this.rx + dx, this.ry + dy);
                if (c == null || c.isCellSpace())
                    continue;

                int aRx = (int) (animation * (double) (40 + main.gapSize) * (double) dx);
                int aRy = (int) (animation * (double) (40 + main.gapSize) * (double) dy);

                gEllipse(g, getX(drawExplodeHalf + aRx), getY(drawExplodeHalf + aRy), atomSize);
            }
        }
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
