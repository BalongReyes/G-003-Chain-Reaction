
package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class CellSpecial extends Cell{

    public CellSpecial(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.special;
    }

// Renderable ================================================================================================
    
// Layer 2 ---------------------------------------------------------------------------------------------------

    @Override
    public void renderLayer2(Graphics2D g){
        super.renderLayer2(g);
        if(isCellPart(TypeCellPart.space)) return;
        drawDesign(g);
    }
    
// ...........................................................................................................
    
    private void drawDesign(Graphics2D g){
        g.setColor(Color.gray);
        g.setStroke(new BasicStroke(2.0F));
        this.gDrawRect(g, 12, 12, -24, -24);
        g.setStroke(new BasicStroke(1.0F));
    }
    
}
