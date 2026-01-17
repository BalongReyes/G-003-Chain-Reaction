
package MainSystem.Object.CellType;

import DataSystem.ID.IDDirection;
import static DataSystem.ID.IDDirection.D;
import static DataSystem.ID.IDDirection.L;
import static DataSystem.ID.IDDirection.R;
import static DataSystem.ID.IDDirection.U;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class CellNoEntry extends Cell{

    public CellNoEntry(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.noEntry;
    }

// ManagerOneWay =============================================================================================
    
    private IDDirection noEntry;

    public void setNoEntry(int mapBitNoEntry){
        this.noEntry = IDDirection.getValue(mapBitNoEntry);
    }

    public IDDirection getNoEntry(){
        return noEntry;
    }
    
// Renderable ================================================================================================
    
// Layer 4 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer4(Graphics2D g){
        super.renderLayer4(g);
        if(isCellSpace()) return;
        drawNoEntryDesign(g);
    }
    
// ...........................................................................................................
    
    public void drawNoEntryDesign(Graphics2D g){
        g.setStroke(new BasicStroke(2.0F));
        if(this.focused){
            g.setColor(Color.red);
        }else{
            g.setColor(Color.gray);
        }
        switch(noEntry){

            case U -> {
                g.drawLine(getX(12), getY(12), getXW(-12), getY(12));
                g.drawLine(getX(12), getY(12), getX(12), getY(14));
                g.drawLine(getXW(-12), getY(12), getXW(-12), getY(14));
            }
            case D -> {
                g.drawLine(getX(12), getYH(-12), getXW(-12), getYH(-12));
                g.drawLine(getX(12), getYH(-12), getX(12), getYH(-14));
                g.drawLine(getXW(-12), getYH(-12), getXW(-12), getYH(-14));
            }
            case L -> {
                g.drawLine(getX(12), getY(12), getX(12), getYH(-12));
                g.drawLine(getX(12), getYH(-12), getX(14), getYH(-12));
                g.drawLine(getX(12), getY(12), getX(14), getY(12));
            }
            case R -> {
                g.drawLine(getXW(-12), getY(12), getXW(-12), getYH(-12));
                g.drawLine(getXW(-12), getY(12), getXW(-14), getY(12));
                g.drawLine(getXW(-12), getYH(-12), getXW(-14), getYH(-12));
            }
        }
        g.setStroke(new BasicStroke(1.0F));
    }
    
}
