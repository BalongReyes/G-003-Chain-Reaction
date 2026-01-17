
package MainSystem.Object.CellType;

import DataSystem.ID.IDDirection;
import DataSystem.Type.TypeCellPart;
import static MainSystem.Abstract.AbstractObject.main;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerPlayers;
import Settings.SettingsCell;
import java.awt.Color;
import java.awt.Graphics2D;

public class CellCannon extends Cell{

// Constructor ===============================================================================================
    
    public CellCannon(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.cannon;
    }
    
// Main Methods ==============================================================================================

    public Cell CannonTarget = null;
    
    public void confirmCannonTarget(Cell tC){
        if(!getManagerAtoms().isMaxAtoms() || !tC.isCellPart(TypeCellPart.cannon)) return;
        
        main.saveStates();
        CannonTarget = tC;
        setPop(HandlerPlayers.getPlayer());
        HandlerPlayers.nextPlayer();
    }
    
// Tickable ==================================================================================================
    
    @Override
    public void tick(){
        super.tick();
        if(isCellSpace()) return;
        
        if(!isSimulatingPop()){
            if(!pop && CannonTarget != null){
                CannonTarget = null;
            }
        }
    }
    
    @Override
    public void tickPopReady(){
        if(CannonTarget != null){
            CannonTarget.addFutureAtoms(popPlayer);
        }else{
            for(IDDirection d : IDDirection.values()){
                Cell c = getManagerSideCell().getSide(d);
                if(c != null){
                    c.addFutureAtoms(popPlayer);
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
    }
    
// Layer 2 ---------------------------------------------------------------------------------------------------

    @Override
    public void renderLayer2(Graphics2D g){
        super.renderLayer2(g);
        if(isCellSpace()) return;
        drawDesign(g);
    }
    
// ...........................................................................................................
    
    private void drawDesign(Graphics2D g){
        if(!main.isSimulating()){
            if(cellLeftPressed != null && cellLeftPressed == this){
                g.setColor(HandlerPlayers.getPlayerColor());
            }else if(focused){
                if(cellLeftPressed != null){
                    g.setColor(Color.lightGray);
                }else{
                    g.setColor(isInvalidMove() ? SettingsCell.invalidColor : Color.lightGray);
                }
            }else{
                g.setColor(Color.gray);
            }
        }else{
            g.setColor(Color.gray);
        }
        this.gFillRect(g, 13, 4, -25, -38);
        this.gFillRect(g, 13, 34, -25, -38);
        this.gFillRect(g, 4, 13, -38, -25);
        this.gFillRect(g, 34, 13, -38, -25);
    }
    
// Layer 3 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer3(Graphics2D g){
        super.renderLayer3(g);
        if(isCellSpace()) return;
    }
    
// Layer 4 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer4(Graphics2D g){
        super.renderLayer4(g);
        if(isCellSpace()) return;
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        super.renderLayer5(g);
        if(isCellSpace()) return;
    }

// ...........................................................................................................
    
    @Override
    protected void drawExplodeSet(Graphics2D g){
        if(CannonTarget == null) super.drawExplodeSet(g);
        g.setColor(this.explodeColor);

        double animation = 1 - this.explodeAnimation;

        if(CannonTarget != null){
            int tRx = CannonTarget.rx - this.rx;
            int tRy = CannonTarget.ry - this.ry;
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
