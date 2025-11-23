
package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import static MainSystem.Abstract.AbstractObject.main;
import MainSystem.Methods.MethodsNumber;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Manager.ManagerCell.ManagerTeleport;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class CellTeleport extends Cell{

    public CellTeleport(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        
        cellPart = TypeCellPart.teleport;
        managerTeleport = new ManagerTeleport(this);
    }
    
// ManagerMirror =============================================================================================
    
    private ManagerTeleport managerTeleport;
    
    public ManagerTeleport getManagerTeleport(){
        return managerTeleport;
    }
    
    public void setTeleport(int teleportCellPart){
        managerTeleport.setTeleportCellPart(teleportCellPart);
    }
    
    public boolean isTeleportPart(int teleportCellPart){
        return getManagerTeleport().getTeleportCellPart() == teleportCellPart;
    }
    
    public void updateTeleport(){
        Cell[] teleports = HandlerCell.getCellTeleport(getManagerTeleport().getTeleportCellPart());
        if(teleports != null) for(Cell c : teleports) if(c != this){
            getManagerTeleport().setTeleportCell(c);
        }
    }
    
// Tickable ==================================================================================================

    @Override
    public void tickPopReady(){
        super.tickPopReady();
        
        tickAnimations();
        
        Cell cT = getManagerTeleport().getTeleportCell();
        if(cT != null){
            cT.addFutureAtoms(popPlayer);
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private int animationTick = 0;
    
    @Override
    protected void tickAnimations(){
        super.tickAnimations();
        
        animationTick++;
        int animationTickMax = 100;
        if(focused || partFocused){
            animationTickMax = 10;
        }
        if(animationTick > animationTickMax){
            animationTick = 0;
            dashPhase--;
            if(dashPhase <= 0){
                dashPhase = 40;
            }
            
            dashPhase2++;
            if(dashPhase2 >= 26){
                dashPhase2 = 1;
            }
        }
    }
    
// -----------------------------------------------------------------------------------------------------------

    @Override
    protected void setFocused(boolean focus){
        super.setFocused(focus);
        
        Cell c = getManagerTeleport().getTeleportCell();
        if(c != null) c.partFocused = focus;
    }
    
// Renderable ================================================================================================

// Layer 1 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer1(Graphics2D g){
        super.renderLayer1(g);
        if(isCellPart(TypeCellPart.space)) return;
    }
    
// Layer 2 ---------------------------------------------------------------------------------------------------

    @Override
    public void renderLayer2(Graphics2D g){
        super.renderLayer2(g);
        if(isCellPart(TypeCellPart.space)) return;
        drawDesign(g);
    }
    
// ...........................................................................................................
    
    private int dashPhase = MethodsNumber.getRandomNumber(1, 40);
    private int dashPhase2 = MethodsNumber.getRandomNumber(1, 26);
    
    private void drawDesign(Graphics2D g){
        Color dash1;
        Color dash2;
        
        if(focused && isInvalidMove()){
            dash1 = Color.red;
            dash2 = Color.red;
        }else if(partFocused && getManagerTeleport().getTeleportCell().isInvalidMove()){
            dash1 = Color.red;
            dash2 = Color.red;
        }else{
            if(focused || partFocused){
                dash1 = Color.white;
                dash2 = Color.gray;
            }else{
                dash1 = Color.gray;
                dash2 = Color.darkGray;
            }
        }

        g.setColor(dash1);
        g.setStroke(new BasicStroke(2.0F, 1, 1, 1.0F, new float[]{20.0f}, dashPhase));
        this.gDrawRect(g, 10, 10, -20, -20);
        g.setStroke(new BasicStroke(1.0F));
        
        g.setColor(dash2);
        g.setStroke(new BasicStroke(2.0F, 1, 1, 1.0F, new float[]{12.0f}, dashPhase2));
        this.gDrawRect(g, 14, 14, -28, -28);
        g.setStroke(new BasicStroke(1.0F));
    }

// Layer 3 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer3(Graphics2D g){
        super.renderLayer3(g);
        if(isCellPart(TypeCellPart.space)) return;
    }
    
// Layer 4 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer4(Graphics2D g){
        super.renderLayer4(g);
        if(isCellPart(TypeCellPart.space)) return;
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        super.renderLayer5(g);
        if(isCellPart(TypeCellPart.space)) return;
    }

// ...........................................................................................................
    
    @Override
    protected void drawCellBorderFocused(Graphics2D g){
        super.drawCellBorderFocused(g);
        
        if(isCellPart(TypeCellPart.teleport)){
            Cell c = getManagerTeleport().getTeleportCell();
            c.drawBorder = isInvalidMove() ? SettingsCell.invalidColor : HandlerPlayers.getPlayerColor();
        }
    }
    
// ...........................................................................................................
    
    @Override
    protected void drawExplodeSet(Graphics2D g){
        super.drawExplodeSet(g);
        g.setColor(this.explodeColor);

        double animation = 1 - this.explodeAnimation;

        Cell cellTeleport = getManagerTeleport().getTeleportCell();
        if(cellTeleport != null){
            int tRx = cellTeleport.rx - this.rx;
            int tRy = cellTeleport.ry - this.ry;
            int aRx, aRy;

            if(tRx < 0){
                if(tRy < 0){
                    aRx = (int) (animation * (double)(40 + main.gapSize) * (double)tRx);
                    aRy = (int) (animation * (double)(40 + main.gapSize) * (double)tRy);
                    gEllipse(g, getX(drawExplodeHalf + aRx), getY(drawExplodeHalf + aRy), atomSize);
                }else{
                    tRy *= -1;
                    aRx = (int) (animation * (double)(40 + main.gapSize) * (double)tRx);
                    aRy = (int) (animation * (double)(40 + main.gapSize) * (double)tRy);
                    gEllipse(g, getX(drawExplodeHalf + aRx), getY(drawExplodeHalf - aRy), atomSize);
                }
            }else{
                tRx *= -1;
                if(tRy < 0){
                    aRx = (int) (animation * (double)(40 + main.gapSize) * (double)tRx);
                    aRy = (int) (animation * (double)(40 + main.gapSize) * (double)tRy);
                    gEllipse(g, getX(drawExplodeHalf - aRx), getY(drawExplodeHalf + aRy), atomSize);
                }else{
                    tRy *= -1;
                    aRx = (int) (animation * (double)(40 + main.gapSize) * (double)tRx);
                    aRy = (int) (animation * (double)(40 + main.gapSize) * (double)tRy);
                    gEllipse(g, getX(drawExplodeHalf - aRx), getY(drawExplodeHalf - aRy), atomSize);
                }
            }
        }
    }
    
}
