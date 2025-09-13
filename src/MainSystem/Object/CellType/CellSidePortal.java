
package MainSystem.Object.CellType;

import DataSystem.ID.IDDirection;
import static DataSystem.ID.IDDirection.D;
import static DataSystem.ID.IDDirection.L;
import static DataSystem.ID.IDDirection.R;
import static DataSystem.ID.IDDirection.U;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import java.awt.Graphics2D;
import java.util.Arrays;

public class CellSidePortal extends Cell{

    public CellSidePortal(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.sidePortal;
    }
    
// Renderable ================================================================================================
    
// Layer 2 ---------------------------------------------------------------------------------------------------

    @Override
    public void renderLayer2(Graphics2D g){
        super.renderLayer2(g);
        if(isCellPart(TypeCellPart.space)) return;
        drawSidePortal(g);
    }
    
// ...........................................................................................................
    
    public boolean[] highlightSidePortal = new boolean[4];
    
    public void resetHighlightSidePortal(){
        Arrays.fill(this.highlightSidePortal, false);
    }
    
    private void drawSidePortal(Graphics2D g){
        for(IDDirection d : IDDirection.values()){
            if(!this.highlightSidePortal[d.index]) continue;
            switch(d){
                case U -> {
                    this.gFillRect(g, 14, 6, -36, -36);
                    this.gFillRect(g, 22, 6, -36, -36);
                }
                case D -> {
                    this.gFillRect(g, 14, 30, -36, -36);
                    this.gFillRect(g, 22, 30, -36, -36);
                }
                case L -> {
                    this.gFillRect(g, 6, 14, -36, -36);
                    this.gFillRect(g, 6, 22, -36, -36);
                }
                case R -> {
                    this.gFillRect(g, 30, 14, -36, -36);
                    this.gFillRect(g, 30, 22, -36, -36);
                }
            }
        }
    }

}
