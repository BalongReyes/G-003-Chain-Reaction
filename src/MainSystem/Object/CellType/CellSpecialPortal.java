
package MainSystem.Object.CellType;

import DataSystem.ID.IDDirection;
import static DataSystem.ID.IDDirection.D;
import static DataSystem.ID.IDDirection.R;
import DataSystem.Type.TypeCellPart;
import static MainSystem.Abstract.AbstractObject.main;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerPlayers;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Arrays;

public class CellSpecialPortal extends Cell{

    public CellSpecialPortal(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.specialPortal;
    }

// Tickable ==================================================================================================

    private int animationTick1 = 0;
    
    @Override
    protected void tickAnimations(){
        super.tickAnimations();
        if((focused || sideFocused != null) && !main.isCellHideHint()){
            animationTick1++;
            if(animationTick1 > 25){
                animationTick1 = 0;
                if(focused){
                    specialPortalDashPhase[1]--;
                    if(specialPortalDashPhase[1] < 1) specialPortalDashPhase[1] = 21;
                    specialPortalDashPhase[3]--;
                    if(specialPortalDashPhase[3] < 1) specialPortalDashPhase[3] = 21;
                }
                if(sideFocused != null) switch(sideFocused){
                    case D, R -> {
                        if(sideFocused == IDDirection.D){
                            specialPortalDashPhase[1]++;
                            if(specialPortalDashPhase[1] > 21) specialPortalDashPhase[1] = 1;
                        }
                        if(sideFocused == IDDirection.R){
                            specialPortalDashPhase[3]++;
                            if(specialPortalDashPhase[3] > 21) specialPortalDashPhase[3] = 1;
                        }
                    }
                }
            }
        }
    }
    
// Renderable ================================================================================================

// Layer 1 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer1(Graphics2D g){
        super.renderLayer1(g);
        if(isCellSpace()) return;
        drawSpecialPortalLine(g);
    }
    
// ...........................................................................................................
    
    private Color portalLineUndergroundColor = new Color(60, 60, 60);
    
    public int[] specialPortalDashPhase = new int[]{0, 0, 0, 0};
    public boolean[] specialPortalUnderground = new boolean[4];
    
    public void resetSpecialPortalUnderground(){
        Arrays.fill(specialPortalUnderground, false);
    }
    
    private void drawSpecialPortalLine(Graphics2D g){
        if(!isCellPart(TypeCellPart.specialPortal)) return;
        
        for(IDDirection d : IDDirection.values()) if(this.getManagerSideCell().haveSide(d)){
            Cell c = this.getManagerSideCell().getSide(d);
            if((!c.isCellPart(TypeCellPart.specialPortal) || d != IDDirection.U && d != IDDirection.L) && !c.isCellPart(TypeCellPart.portal)){
                
                if(!main.isSimulating() && (this.focused || c.focused) && !main.isCellHideHint()){
                    if(this.focused){
                        g.setColor(!this.isInvalidMove() ? HandlerPlayers.getPlayerColor() : SettingsCell.invalidColor);
                    }else if(c.focused){
                        g.setColor(!c.isInvalidMove() ? HandlerPlayers.getPlayerColor() : SettingsCell.invalidColor);
                    }
                }else if(this.specialPortalUnderground[d.index]){
                    g.setColor(this.portalLineUndergroundColor);
                }else{
                    g.setColor(this.portalLineColor);
                }

                Stroke dashed = new BasicStroke(3.0F, 0, 2, 0.0F, new float[]{12.0f, 9.0f}, specialPortalDashPhase[d.index]);
                
                if(this.getManagerSideCell().getDistance(c, d) >= 2){
                    switch(d){
                        case D -> {
                            if(this.specialPortalUnderground[d.index]){
                                g.setStroke(dashed);
                                g.drawLine(this.getX() + 20, this.getY() + 20, c.getX() + 20, c.getY() + 20);
                            }else{
                                g.setStroke(new BasicStroke(3.0F));
                                g.drawLine(this.getX() + 20, this.getY() + 20, c.getX() + 20, c.getY() + 20);
                            }
                        }
                        case R -> {
                            if(this.specialPortalUnderground[d.index]){
                                g.setStroke(dashed);
                                g.drawLine(this.getX() + 20, this.getY() + 20, c.getX() + 20, c.getY() + 20);
                            }else{
                                g.setStroke(new BasicStroke(3.0F));
                                g.drawLine(this.getX() + 20, this.getY() + 20, c.getX() + 20, c.getY() + 20);
                            }
                        }
                    }
                    g.setStroke(new BasicStroke(1.0F));
                }
            }
        }
    }
    
}
