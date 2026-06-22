
package MainSystem.Object.CellType;

import DataSystem.ID.IDDirection;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerPlayers;
import Settings.SettingsCell;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

public class CellCannon extends Cell{

// Constructor ===============================================================================================
    
    public CellCannon(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.cannon;
    }
    
// Main Methods ==============================================================================================

    public Cell CannonTarget = null;
    
    public void confirmCannonTarget(Cell tC){
        if(!getManagerAtoms().isMaxAtoms() || getManagerAtoms().isEmptyOrDead() || !tC.isCellPart(TypeCellPart.cannon)) return;
        
        main.saveState();
        CannonTarget = tC;
        setPop(main.handlerPlayers.getPlayer());
        main.handlerPlayers.nextPlayer();
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
    
    public Image ImageCannon = new ImageIcon(new javax.swing.ImageIcon(getClass().getResource("/DataSystem/Pictures/Cannon.png")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)).getImage();
    
    private void drawDesign(Graphics2D g){
        drawImage(g, ImageCannon, SettingsCell.getCellIconAlpha(focused));
        
        if(!main.isSimulating()){
            if(cellLeftPressed != null && cellLeftPressed == this){
                g.setColor(main.handlerPlayers.getPlayerColor());
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
        if(CannonTarget == null) {
            super.drawExplodeSet(g);
        } else {
            g.setColor(this.explodeColor);
            double animation = 1.0D - this.explodeAnimation;
            int N = Math.max(1, this.getManagerAtoms().getMaxAtoms());
            double startRadius = (N == 1) ? 3.0D : 8.0D;
            double angleIncrement = 360.0D / N;
            
            drawFlyingAtom(g, CannonTarget, 0, N, angleIncrement, startRadius, animation);
        }
    }

}
