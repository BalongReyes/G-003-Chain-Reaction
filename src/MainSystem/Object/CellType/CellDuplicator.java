package MainSystem.Object.CellType;

import DataSystem.Data.Player;
import DataSystem.Type.TypeCellPart;
import MainSystem.Methods.MethodsNumber;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Manager.ManagerCell.ManagerDuplicator;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class CellDuplicator extends Cell {

    public ManagerDuplicator managerDuplicator;
    private boolean isSyncing = false;
    
    private int animationTickCount1 = 0, animationTickCount2 = 0;
    private int animationTickDelay1 = 50, animationTickDelay2 = 50;
    private int animationTick1 = 0, animationTick2 = 0;
    private boolean animationPhase1 = false, animationPhase2 = false;

    public CellDuplicator(double x, double y, int rx, int ry) {
        super(x, y, rx, ry);
        cellPart = TypeCellPart.duplicator;
        managerDuplicator = new ManagerDuplicator(this);
        
        animationTick1 = MethodsNumber.getRandomNumber(0, 40);
        animationTick2 = MethodsNumber.getRandomNumber(0, 40);
    }

    public ManagerDuplicator getManagerDuplicator() {
        return managerDuplicator;
    }

    public void setDuplicator(int duplicatorCellPart) {
        managerDuplicator.setDuplicateCellPart(duplicatorCellPart);
    }

    public boolean isDuplicatorPart(int duplicatorCellPart) {
        return getManagerDuplicator().getDuplicatorCellPart() == duplicatorCellPart;
    }

    public void updateDuplicator() {
        List<Cell> duplicators = HandlerCell.getCellDuplicator(getManagerDuplicator().getDuplicatorCellPart());
        if (duplicators != null) {
            for (Cell c : duplicators) {
                if (c != this) {
                    getManagerDuplicator().addDuplicatorCell(c);
                }
            }
        }
    }

    @Override
    public void confirmAddAtoms(Player player, boolean explodeAdd) {
        if (isSyncing) {
            super.confirmAddAtoms(player, explodeAdd);
            return;
        }

        isSyncing = true;
        super.confirmAddAtoms(player, explodeAdd);
        for (Cell duplicatorCell : getManagerDuplicator().getDuplicatorCells()) {
            if (duplicatorCell != null) {
                ((CellDuplicator) duplicatorCell).isSyncing = true;
                duplicatorCell.confirmAddAtoms(player, explodeAdd);
                ((CellDuplicator) duplicatorCell).isSyncing = false;
            }
        }
        isSyncing = false;
    }

    @Override
    public void setPop(Player popPlayer) {
        if (this.pop) {
            return;
        }
        super.setPop(popPlayer);
        for (Cell duplicatorCell : getManagerDuplicator().getDuplicatorCells()) {
            if (duplicatorCell != null) {
                duplicatorCell.setPop(popPlayer);
            }
        }
    }

    @Override
    protected void tickAnimations() {
        super.tickAnimations();
        
        animationTickCount1++;
        if(animationTickCount1 > animationTickDelay1){
            animationTickCount1 = 0;
            
            if(animationPhase1){
                animationTick1--;
                if(animationTick1 <= 0){
                    animationTick1 = 0;
                    animationPhase1 = false;
                    animationTickDelay1 = MethodsNumber.getRandomNumber(20, 40);
                }
            }else{
                animationTick1++;
                if(animationTick1 >= 80){
                    animationTick1 = 80;
                    animationPhase1 = true;
                    animationTickDelay1 = MethodsNumber.getRandomNumber(20, 40);
                }
            }
        }
        
        animationTickCount2++;
        if(animationTickCount2 > animationTickDelay2){
            animationTickCount2 = 0;
            
            if(animationPhase2){
                animationTick2--;
                if(animationTick2 <= 0){
                    animationTick2 = 0;
                    animationPhase2 = false;
                    animationTickDelay2 = MethodsNumber.getRandomNumber(20, 40);
                }
            }else{
                animationTick2++;
                if(animationTick2 >= 80){
                    animationTick2 = 80;
                    animationPhase2 = true;
                    animationTickDelay2 = MethodsNumber.getRandomNumber(20, 40);
                }
            }
        }
    }

// -----------------------------------------------------------------------------------------------------------
    
    @Override
    protected void setFocused(boolean focus){
        super.setFocused(focus);
        
        for(Cell c : getManagerDuplicator().getDuplicatorCells()){
            c.partFocused = focus;
        }
    }
    
// Layer 2 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer2(Graphics2D g) {
        super.renderLayer2(g);
        if (isCellSpace()) {
            return;
        }
        drawDesign(g);
    }

    private void drawDesign(Graphics2D g) {
        g.setStroke(new BasicStroke(2.0F));
        
        g.setColor((focused || partFocused) ? Color.white : Color.darkGray);
        if(animationTick1 < 40){
            g.drawLine(getX(animationTick1), getY(), getX(), getY(animationTick1));
        }else{
            g.drawLine(getXW(), getY(animationTick1 - 40), getX(animationTick1 - 40), getYH());
        }
        
        g.setColor((focused || partFocused) ? Color.white : Color.gray);
        if(animationTick2 < 40){
            g.drawLine(getXW(-animationTick2), getY(), getXW(), getY(animationTick2));
        }else{
            g.drawLine(getX(), getY(animationTick2 - 40), getXW(-(animationTick2 - 40)), getYH());
        }
        
        g.setStroke(new BasicStroke(1.0F));
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        super.renderLayer5(g);
        if(isCellSpace()) return;
    }

// ...........................................................................................................
    
    @Override
    public void drawCellBorderFocused(Graphics2D g){
        super.drawCellBorderFocused(g);
        
        for(Cell c : getManagerDuplicator().getDuplicatorCells()){
            if(c.drawBorder != null) continue;
            c.drawBorder = isInvalidMove() ? SettingsCell.invalidColor : HandlerPlayers.getPlayerColor();
            c.drawCellBorderFocused(g);
        }
    }
    
}