
package MainSystem.Object.CellType;

import DataSystem.Data.Position;
import DataSystem.ID.IDDirection;
import DataSystem.State.StateCell;
import DataSystem.Type.TypeCellPart;
import static MainSystem.Abstract.AbstractObject.main;
import MainSystem.Object.Cell;
import ManagerSystem.CustomGraphics;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerPlayers;
import Settings.SettingsCell;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;

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
        Cell tC = main.handlerCell.getAllCell(dp.rx, dp.ry);
        if(tC != null){
            main.handlerCell.swap(this, tC);
            main.handlerCell.updateCells(main);
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
            main.saveState();
        }
        
        Cell tC = null;
        IDDirection d = null;
        
        switch(moveable){
            case 1 -> {
                Cell cellD = main.handlerCell.getAllCell(this, IDDirection.D, 1);
                if(cellD != null && cellD.isCellSpace()){
                    d = IDDirection.D;
                }else{
                    Cell cellU = main.handlerCell.getAllCell(this, IDDirection.U, 1);
                    if(cellU != null && cellU.isCellSpace()){
                        d = IDDirection.U;
                    }
                }
            }
            case 2 -> {
                Cell cellL = main.handlerCell.getAllCell(this, IDDirection.L, 1);
                if(cellL != null && cellL.isCellSpace()){
                    d = IDDirection.L;
                }else{
                    Cell cellR = main.handlerCell.getAllCell(this, IDDirection.R, 1);
                    if(cellR != null && cellR.isCellSpace()){
                        d = IDDirection.R;
                    }
                }
            }
        }
        
        if(d == null){
            return false;
        }
                
        int distance = 0;
        int maxDistance = Math.max(SettingsCell.xCell, SettingsCell.yCell);
        while((tC == null || !tC.getManagerSideCell().haveSide(d)) && distance < maxDistance){
            distance++;
            tC = main.handlerCell.getAllCell(this, d, distance);
        }
        if(tC == null || distance <= 1 || tC.reservedForMove){
            return false;
        }
        
        tC.reservedForMove = true;
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
                main.handlerCell.removeAllSideCells(this);
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
                
                cellMove.reservedForMove = false;
                
                main.handlerCell.swapPosition(this, cellMove, sOriginalPosition, tOriginalPosition);
                main.handlerCell.updateCells(main);
                
                if(moveThenAdd){
                    confirmAddAtoms(main.handlerPlayers.getPlayer(), false);
                    main.handlerPlayers.nextPlayer();
                }
                
                cellMove = null;
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
        drawMoveableDesign(g);
    }
    
// ...........................................................................................................
    
    public Image ImageMoveH = new ImageIcon(new javax.swing.ImageIcon(getClass().getResource("/DataSystem/Pictures/MoveH.png")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)).getImage();
    public Image ImageMoveV = new ImageIcon(new javax.swing.ImageIcon(getClass().getResource("/DataSystem/Pictures/MoveV.png")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)).getImage();
    
    public void drawMoveableDesign(Graphics2D g){
        Color overlayColor = Color.white;
        
        if(this.focused){
            if(isInvalidMove() || this.futureMove){
                overlayColor = Color.red;
                g.setColor(Color.red);
            }else{
                overlayColor = Color.white;
                g.setColor(Color.lightGray);
            }
        }else{
            if(this.futureMove){
                overlayColor = Color.red;
                g.setColor(Color.red);
            }else{
                overlayColor = Color.white;
                g.setColor(Color.gray);
            }
        }
        
        switch(moveable){
            case 1 -> {
                drawImage(g, CustomGraphics.OverlayColor(g, overlayColor, ImageMoveV), SettingsCell.getCellIconAlpha(focused));
            }
            case 2 -> {
                drawImage(g, CustomGraphics.OverlayColor(g, overlayColor, ImageMoveH), SettingsCell.getCellIconAlpha(focused));
            }
        }
        
//        g.setStroke(new BasicStroke(2.0F));
//        g.drawLine(getX(12), getY(12), getX(12), getY(14));
//        g.drawLine(getXW(-12), getY(12), getXW(-12), getY(14));
//        g.drawLine(getX(12), getYH(-12), getX(12), getYH(-14));
//        g.drawLine(getXW(-12), getYH(-12), getXW(-12), getYH(-14));
//        g.drawLine(getX(12), getYH(-12), getX(14), getYH(-12));
//        g.drawLine(getX(12), getY(12), getX(14), getY(12));
//        g.drawLine(getXW(-12), getY(12), getXW(-14), getY(12));
//        g.drawLine(getXW(-12), getYH(-12), getXW(-14), getYH(-12));
//        g.setStroke(new BasicStroke(1.0F));
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        super.renderLayer5(g);
        if(isCellSpace()) return;
    }
    
}
