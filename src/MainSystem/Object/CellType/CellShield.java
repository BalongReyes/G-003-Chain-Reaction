
package MainSystem.Object.CellType;

import DataSystem.Data.Player;
import DataSystem.State.StateShield;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Manager.ManagerCell.ManagerShield;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Stack;

public class CellShield extends Cell{

    public CellShield(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        
        managerShield = new ManagerShield(this);
        cellPart = TypeCellPart.shield;
    }
    
// ManagerShield =============================================================================================
    
    private ManagerShield managerShield;
    
    public ManagerShield getManagerShield(){
        return managerShield;
    }
    
    public void resetManagerShield(){
        managerShield.reset();
    }
    
// Main Methods ==============================================================================================

    @Override
    public boolean abstractConfirmAddAtoms(Player player, boolean explodeAdd){
        if(getManagerShield().shieldNotOwned()){
            getManagerShield().setShield(player);
            getManagerAtoms().replaceAll(player);
            return false;
        }else if(!getManagerShield().shieldCheckOwner(player)){
            if(explodeAdd){
                getManagerShield().incrementShieldDestroy();
                if(getManagerShield().getShieldDestroy() >= SettingsCell.maxShieldHeath){
                    getManagerShield().reset();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean abstractValidateClickAddAtom(Player player){
        return getManagerShield().shieldCheckOwner(player, Player.Dead);
    }

    @Override
    public void reset(){
        super.reset();
        getManagerShield().reset();
    }
    
// Tickable ==================================================================================================

    @Override
    public boolean getExplodeReady(){
        if(getManagerShield().shieldOwned()){
            return getManagerAtoms().atomsSize() >= this.getManagerAtoms().getMaxAtoms();
        }
        return false;
    }

    private int tickBlinkShieldDecay = 0;
    public boolean blinkShieldDecay = false;
    
    @Override
    protected void tickAnimations(){
        super.tickAnimations();
        
        tickBlinkShieldDecay++;
        if(blinkShieldDecay){
            if(tickBlinkShieldDecay > 150){
                tickBlinkShieldDecay = 0;
                blinkShieldDecay = false;
            }
        }else{
            if(tickBlinkShieldDecay > 1000){
                tickBlinkShieldDecay = 0;
                blinkShieldDecay = true;
            }
        }
    }
    
    @Override
    public void tickTurn(){
        super.tickTurn();
        
        Player shieldOwner = getManagerShield().getShield();
        
        if(shieldOwner != null && shieldOwner != Player.Dead && shieldOwner == HandlerPlayers.currentPlayer){
            if(getManagerAtoms().isEmptyOrDead()){
                getManagerShield().incrementDecay();
            }else{
                getManagerShield().resetShieldDecay();
            }
            if(getManagerShield().getShieldDecay() > 1 || shieldOwner.atoms == 0){
                getManagerShield().reset();
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
        drawShieldDesign(g);
    }
    
// ...........................................................................................................
    
    private void drawShieldDesign(Graphics2D g){
        Player shield = getManagerShield().getShield();
        if(shield != null){
            if(focused && isInvalidMove()){
                g.setColor(Color.red);
            }else{
                if(HandlerPlayers.checkPlayer(getManagerShield().getShield()) && !focused){
                    if(getManagerAtoms().isEmptyOrDead()){
                        if(blinkShieldDecay){
                            g.setColor(Color.red);
                        }else{
                            g.setColor(shield.color);
                        }
                    }else{
                        g.setColor(shield.color);
                    }
                }else{
                    g.setColor(shield.color);
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
        if(isCellSpace()) return;
    }

// State =====================================================================================================
    
    public Stack<StateShield> stateCellStack = new Stack<>();
    
    @Override
    public void resetState(){
        super.resetState();
        stateCellStack.clear();
    }
    
    @Override
    public void saveState(){
        super.saveState();
        this.stateCellStack.add(new StateShield(getManagerShield()));
        if(this.stateCellStack.size() > main.undoLimit){
            this.stateCellStack.remove(0);
        }
    }

    @Override
    public void undoState(){
        super.undoState();
        if(stateCellStack.empty()) return;
        StateShield sC = (StateShield) this.stateCellStack.pop();
        this.getManagerShield().setStateCell(sC);
    }
    
}
