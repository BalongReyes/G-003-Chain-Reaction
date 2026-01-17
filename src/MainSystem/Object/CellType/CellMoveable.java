
package MainSystem.Object.CellType;

import DataSystem.Data.Position;
import DataSystem.ID.IDDirection;
import DataSystem.State.StateCell;
import DataSystem.Type.TypeCellPart;
import static MainSystem.Abstract.AbstractObject.main;
import MainSystem.Object.Cell;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerPlayers;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class CellMoveable extends Cell{

    public CellMoveable(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        
        cellPart = TypeCellPart.moveable;
    }
    
// ManagerMoveable ===========================================================================================
    
    private int moveable;

    public void setMoveable(int mapBitMoveable){
        moveable = mapBitMoveable;
    }

    public int getMoveable(){
        return moveable;
    }
    
    public void undoStateMove(StateCell sC){
        Position dp = sC.getLastPosition();
        Cell tC = HandlerCell.getAllCell(dp.rx, dp.ry);
        if(tC != null){
            HandlerCell.swap(this, tC);
            HandlerCell.updateCells();
        }
    }
    
// Clickable =================================================================================================
    
    @Override
    public void clickRightConfirmed(){
        moveCell(true);
    }
    
    public boolean moveCell(boolean rightClicked){
        if((isInvalidMove() || main.isSimulating()) && rightClicked) return false;
        if(rightClicked){
            main.saveStates();
        }
        
        Cell tC = null;
        IDDirection d = null;
        
        switch(moveable){
            case 1 -> {
                if(HandlerCell.getAllCell(this, IDDirection.D, 1).isCellSpace()){
                    d = IDDirection.D;
                }else if(HandlerCell.getAllCell(this, IDDirection.U, 1).isCellSpace()){
                    d = IDDirection.U;
                }
            }
            case 2 -> {
                if(HandlerCell.getAllCell(this, IDDirection.L, 1).isCellSpace()){
                    d = IDDirection.L;
                }else if(HandlerCell.getAllCell(this, IDDirection.R, 1).isCellSpace()){
                    d = IDDirection.R;
                }
            }
        }
        
        if(d == null){
            return false;
        }
                
        int distance = 0;
        while(tC == null || !tC.getManagerSideCell().haveSide(d)){
            distance++;
            tC = HandlerCell.getAllCell(this, d, distance);
        }
        if(distance <= 1){
            return false;
        }
        
        setSimualteMove(tC, d, rightClicked);
        return true;
    }
    
    
// Tickable ==================================================================================================

    @Override
    public void tick(){
        super.tick();
        if(isCellSpace()) return;
        
        if(!isSimulatingPop()){
            tickMove();
            if(!moving && simulateMove){
                moveAnimation = 1.0D;
                moveTick = main.moveBuffer;
                HandlerCell.removeAllSideCells(this);
            }
        }
    }
    
    @Override
    protected void setFocused(boolean focus){
        super.setFocused(focus);
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private Cell cellMove = null;
    private IDDirection cellMoveDirection = null;
    private boolean moveThenAdd = false;
    private boolean simulateMove = false;
    
    private Position sOriginalPosition = null, tOriginalPosition = null;
    
    public void setSimualteMove(Cell cellMove, IDDirection direction, boolean moveThenAdd){
        sOriginalPosition = this.getPosition();
        tOriginalPosition = cellMove.getPosition();
        
        this.cellMoveDirection = direction;
        this.cellMove = cellMove;
        this.moveThenAdd = moveThenAdd;
        simulateMove = true;
    }
    
    public boolean isSimualteMove(){
        return simulateMove;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private int moveTick = 0;
    public double moveAnimation = 0.0;
    
    public boolean moving = false;
    
    private void tickMove(){
        if(moveTick > 0){
            moving = true;
            
            moveTick--;
            moveAnimation = (double) moveTick / (double) main.moveBuffer;
            
            double animation = 1 - this.moveAnimation;
            double aRD, aR, aN;
            
            switch(cellMoveDirection){
                case U, D -> {
                    aRD = sOriginalPosition.y - cellMove.y;
                    aR = animation * aRD;
                    aN = sOriginalPosition.y - aR;
                    setNewY(aN);
                }
                case L, R -> {
                    aRD = sOriginalPosition.x - cellMove.x;
                    aR = animation * aRD;
                    aN = sOriginalPosition.x - aR;
                    setNewX(aN);
                }
            }
            
            if(this.moveTick <= 0){
                moveTick = 0;
                moveAnimation = 0.0D;
                simulateMove = false;
                moving = false;
                
                HandlerCell.swapPosition(this, cellMove, sOriginalPosition, tOriginalPosition);
                HandlerCell.updateCells();
                
                if(moveThenAdd){
                    confirmAddAtoms(HandlerPlayers.getPlayer(), false);
                    HandlerPlayers.nextPlayer();
                }
                
                cellMove = null;
            }
        }
    }
    
    @Override
    protected void drawExplodeSet(Graphics2D g){
        g.setColor(this.explodeColor);

        double animation = 1 - this.explodeAnimation;
        int aRD, aR;

        if(this.getManagerSideCell().haveU()){
            aRD = this.ry - this.getManagerSideCell().getSide(IDDirection.U).ry;
            aR = (int) (animation * (double)(40 + main.gapSize) * (double)aRD);
            gEllipse(g, getX(drawExplodeHalf), getY(drawExplodeHalf - aR), atomSize);
        }
        if(this.getManagerSideCell().haveD()){
            aRD = this.getManagerSideCell().getSide(IDDirection.D).ry - this.ry;
            aR = (int) (animation * (double)(40 + main.gapSize) * (double)aRD);
            gEllipse(g, getX(drawExplodeHalf), getY(drawExplodeHalf + aR), atomSize);
        }
        if(this.getManagerSideCell().haveL()){
            aRD = this.rx - this.getManagerSideCell().getSide(IDDirection.L).rx;
            aR = (int) (animation * (double)(40 + main.gapSize) * (double)aRD);
            gEllipse(g, getX(drawExplodeHalf - aR), getY(drawExplodeHalf), atomSize);
        }
        if(this.getManagerSideCell().haveR()){
            aRD = this.getManagerSideCell().getSide(IDDirection.R).rx - this.rx;
            aR = (int) (animation * (double)(40 + main.gapSize) * (double)aRD);
            gEllipse(g, getX(drawExplodeHalf + aR), getY(drawExplodeHalf), atomSize);
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
        drawMoveableDesign(g);
    }
    
// ...........................................................................................................
    
    public void drawMoveableDesign(Graphics2D g){
        g.setStroke(new BasicStroke(2.0F));
        if(this.focused){
            if(isInvalidMove() || this.futureMove){
                g.setColor(Color.red);
            }else{
                g.setColor(Color.lightGray);
            }
        }else{
            if(this.futureMove){
                g.setColor(Color.red);
            }else{
                g.setColor(Color.gray);
            }
        }
        
        g.drawLine(getX(12), getY(12), getX(12), getY(14));
        g.drawLine(getXW(-12), getY(12), getXW(-12), getY(14));
        g.drawLine(getX(12), getYH(-12), getX(12), getYH(-14));
        g.drawLine(getXW(-12), getYH(-12), getXW(-12), getYH(-14));
        g.drawLine(getX(12), getYH(-12), getX(14), getYH(-12));
        g.drawLine(getX(12), getY(12), getX(14), getY(12));
        g.drawLine(getXW(-12), getY(12), getXW(-14), getY(12));
        g.drawLine(getXW(-12), getYH(-12), getXW(-14), getYH(-12));
        g.setStroke(new BasicStroke(1.0F));
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        super.renderLayer5(g);
        if(isCellSpace()) return;
    }
    
}
