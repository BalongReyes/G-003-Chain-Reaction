
package MainSystem.Object.CellType;

import DataSystem.Data.Player;
import DataSystem.State.StateTerritory;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Manager.ManagerCell.ManagerTerritory;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Stack;

public class CellTerritory extends Cell{

    public CellTerritory(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        
        managerTerritory = new ManagerTerritory(this);
        cellPart = TypeCellPart.territory;
    }
    
// ManagerTerritory ==========================================================================================
    
    private ManagerTerritory managerTerritory;
    
    public ManagerTerritory getManagerTerritory(){
        return managerTerritory;
    }
    
    public void resetManagerTerritory(){
        managerTerritory.reset();
    }
    
// Main Methods ==============================================================================================

    @Override
    public boolean abstractConfirmAddAtoms(Player player, boolean explodeAdd){
        if(getManagerTerritory().territoryNotOwned()){
            getManagerTerritory().setTerritory(player);
            getManagerAtoms().replaceAll(player);
            return false;
        }else if(!getManagerTerritory().territoryCheckOwner(player)){
            if(explodeAdd){
                getManagerTerritory().incrementTerritoryDestroy();
                if(getManagerTerritory().getTerritoryDestroy() >= SettingsCell.maxTerritoryHeath){
                    getManagerTerritory().reset();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean abstractValidateClickAddAtom(Player player){
        return getManagerTerritory().territoryCheckOwner(player, Player.Dead);
    }

    @Override
    public void reset(){
        super.reset();
        getManagerTerritory().reset();
    }
    
// Tickable ==================================================================================================

    @Override
    public boolean getExplodeReady(){
        if(getManagerTerritory().territoryOwned()){
            return getManagerAtoms().atomsSize() >= this.getManagerAtoms().getMaxAtoms();
        }
        return false;
    }

    private int tickBlinkTerritoryDecay = 0;
    public boolean blinkTerritoryDecay = false;
    
    @Override
    protected void tickAnimations(){
        super.tickAnimations();
        
        tickBlinkTerritoryDecay++;
        if(blinkTerritoryDecay){
            if(tickBlinkTerritoryDecay > 150){
                tickBlinkTerritoryDecay = 0;
                blinkTerritoryDecay = false;
            }
        }else{
            if(tickBlinkTerritoryDecay > 1000){
                tickBlinkTerritoryDecay = 0;
                blinkTerritoryDecay = true;
            }
        }
    }
    
    @Override
    public void tickTurn(){
        super.tickTurn();
        
        Player territoryOwner = getManagerTerritory().getTerritory();
        
        if(territoryOwner != null && territoryOwner != Player.Dead && territoryOwner == HandlerPlayers.currentPlayer){
            if(getManagerAtoms().isEmptyOrDead()){
                getManagerTerritory().incrementDecay();
            }else{
                getManagerTerritory().resetTerritoryDecay();
            }
            if(getManagerTerritory().getTerritoryDecay() > 1 || territoryOwner.atoms == 0){
                getManagerTerritory().reset();
            }
        }
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
        drawTerritoryDesign(g);
    }
    
// ...........................................................................................................
    
    private void drawTerritoryDesign(Graphics2D g){
        Player territory = getManagerTerritory().getTerritory();
        if(territory != null){
            if(focused && isInvalidMove()){
                g.setColor(Color.red);
            }else{
                if(HandlerPlayers.checkPlayer(getManagerTerritory().getTerritory()) && !focused){
                    if(getManagerAtoms().isEmptyOrDead()){
                        if(blinkTerritoryDecay){
                            g.setColor(Color.red);
                        }else{
                            g.setColor(territory.color);
                        }
                    }else{
                        g.setColor(territory.color);
                    }
                }else{
                    g.setColor(territory.color);
                }
            }
            g.setStroke(new BasicStroke(2.0F));
            this.gDrawRect(g, 6, 6, -12, -12);
            g.setStroke(new BasicStroke(1.0F));
        }
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        super.renderLayer5(g);
        if(isCellPart(TypeCellPart.space)) return;
    }

// State =====================================================================================================
    
    public Stack<StateTerritory> stateCellStack = new Stack<>();
    
    @Override
    public void resetState(){
        super.resetState();
        stateCellStack.clear();
    }
    
    @Override
    public void saveState(){
        super.saveState();
        this.stateCellStack.add(new StateTerritory(getManagerTerritory()));
        if(this.stateCellStack.size() > main.undoLimit){
            this.stateCellStack.remove(0);
        }
    }

    @Override
    public void undoState(){
        super.undoState();
        if(stateCellStack.empty()) return;
        StateTerritory sC = (StateTerritory) this.stateCellStack.pop();
        this.getManagerTerritory().setStateCell(sC);
    }
    
}
