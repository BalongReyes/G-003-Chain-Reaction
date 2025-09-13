
package MainSystem.Object.CellType;

import DataSystem.Data.Position;
import DataSystem.ID.IDDirection;
import DataSystem.State.StateCell;
import DataSystem.Type.TypeCellPart;
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
    public void clickRightPressed(){
        moveCell(true);
    }
    
    public void moveCell(boolean rightClicked){
        if((isInvalidMove() || main.isSimulating()) && rightClicked) return;
        if(rightClicked){
            main.saveStates();
        }
        boolean move = false;
        Cell tC = null;
        switch(moveable){
            case 1 -> {
                if(HandlerCell.getAllCell(this, IDDirection.D, 1).isCellPart(TypeCellPart.space)){
                    int distance = 0;
                    while(tC == null || !tC.getManagerSideCell().haveD()){
                        distance++;
                        tC = HandlerCell.getAllCell(this, IDDirection.D, distance);
                    }
                    distance++;
                    Cell unmoveable = HandlerCell.getAllCell(this, IDDirection.D, distance);
                    if(unmoveable.isCellPart(TypeCellPart.moveable) && unmoveable.futureMove){
                        return;
                    }
                    move = true;
                }else if(HandlerCell.getAllCell(this, IDDirection.U, 1).isCellPart(TypeCellPart.space)){
                    int distance = 0;
                    while(tC == null || !tC.getManagerSideCell().haveU()){
                        distance++;
                        tC = HandlerCell.getAllCell(this, IDDirection.U, distance);
                    }
                    distance++;
                    Cell unmoveable = HandlerCell.getAllCell(this, IDDirection.U, distance);
                    if(unmoveable.isCellPart(TypeCellPart.moveable) && unmoveable.futureMove){
                        return;
                    }
                    move = true;
                }
//                if(!move){
//                    Cell cU = HandlerCell.getCell(this, IDDirection.U);
//                    Cell cD = HandlerCell.getCell(this, IDDirection.D);
//                    if(cU.isCellPart(TypeCellPart.moveable) && cU.futureMove){
//                        output = 2;
//                    }else if(cD.isCellPart(TypeCellPart.moveable) && cD.futureMove){
//                        output = 2;
//                    }else{
//                        output = 1;
//                    }
//                }
            }
            case 2 -> {
                if(HandlerCell.getAllCell(this, IDDirection.L, 1).isCellPart(TypeCellPart.space)){
                    int distance = 0;
                    while(tC == null || !tC.getManagerSideCell().haveL()){
                        distance++;
                        tC = HandlerCell.getAllCell(this, IDDirection.L, distance);
                    }
                    distance++;
                    Cell unmoveable = HandlerCell.getAllCell(this, IDDirection.L, distance);
                    if(unmoveable.isCellPart(TypeCellPart.moveable) && unmoveable.futureMove){
                        return;
                    }
                    move = true;
                }else if(HandlerCell.getAllCell(this, IDDirection.R, 1).isCellPart(TypeCellPart.space)){
                    int distance = 0;
                    while(tC == null || !tC.getManagerSideCell().haveR()){
                        distance++;
                        tC = HandlerCell.getAllCell(this, IDDirection.R, distance);
                    }
                    distance++;
                    Cell unmoveable = HandlerCell.getAllCell(this, IDDirection.R, distance);
                    if(unmoveable.isCellPart(TypeCellPart.moveable) && unmoveable.futureMove){
                        return;
                    }
                    move = true;
                }
//                if(!move){
//                    Cell cL = HandlerCell.getCell(this, IDDirection.L);
//                    Cell cR = HandlerCell.getCell(this, IDDirection.R);
//                    if(cL.isCellPart(TypeCellPart.moveable) && cL.futureMove){
//                        output = 2;
//                    }else if(cR.isCellPart(TypeCellPart.moveable) && cR.futureMove){
//                        output = 2;
//                    }else{
//                        output = 1;
//                    }
//                }
            }
        }
        if(move){
            HandlerCell.swap(this, tC);
            HandlerCell.updateCells();
            if(rightClicked){
                HandlerPlayers.nextPlayer();
            }
        }
    }
    
    
// Tickable ==================================================================================================

    @Override
    protected void setFocused(boolean focus){
        super.setFocused(focus);
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
        if(isCellPart(TypeCellPart.space)) return;
    }
    
}
