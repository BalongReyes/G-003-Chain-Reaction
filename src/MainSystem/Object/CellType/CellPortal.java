
package MainSystem.Object.CellType;

import DataSystem.ID.IDDirection;
import DataSystem.Type.TypeCellPart;
import static MainSystem.Abstract.AbstractObject.main;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerPlayers;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class CellPortal extends Cell{

    public CellPortal(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.portal;
    }

// Renderable ================================================================================================
    
// Layer 1 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer1(Graphics2D g){
        super.renderLayer1(g);
        if(isCellSpace()) return;
        drawPortalLine(g);
    }  
    
// ...........................................................................................................
    
    private void drawPortalLine(Graphics2D g){
        for(IDDirection d : IDDirection.values()) if(this.getManagerSideCell().haveSide(d)){
            Cell c = this.getManagerSideCell().getSide(d);
            if(!c.isCellPart(TypeCellPart.portal) || d != IDDirection.U && d != IDDirection.L){
                if(!main.isSimulating()){
                    if(main.isCellHideHint()){
                        g.setColor(this.portalLineColor);
                    }else if(this.focused){
                        g.setColor(!this.isInvalidMove() ? HandlerPlayers.getPlayerColor() : SettingsCell.invalidColor);
                    }else if(c.focused){
                        g.setColor(!c.isInvalidMove() ? HandlerPlayers.getPlayerColor() : SettingsCell.invalidColor);
                    }else{
                        g.setColor(this.portalLineColor);
                    }
                }else{
                    g.setColor(this.portalLineColor);
                }

                if(this.getManagerSideCell().getDistance(c, d) >= 2){
                    g.setStroke(new BasicStroke(3.0F));
                    g.drawLine(this.getX() + 20, this.getY() + 20, c.getX() + 20, c.getY() + 20);
                    g.setStroke(new BasicStroke(1.0F));
                }
            }
        }
    }
    
}
