
package MainSystem.Object;

import DataSystem.Data.Player;
import DataSystem.Data.Position;
import DataSystem.ID.IDDirection;
import DataSystem.State.StateCell;
import DataSystem.ID.IDObject;
import DataSystem.Interface.Clickable;
import DataSystem.Interface.Renderable;
import DataSystem.Interface.Tickable;
import DataSystem.Type.TypeCellPart;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Methods.MethodsNumber;
import MainSystem.Object.CellType.CellMoveable;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Handlers.HandlerSystem.HandlerTick;
import ManagerSystem.Manager.ManagerCell.ManagerAtoms;
import ManagerSystem.Manager.ManagerCell.ManagerSideCell;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Stack;

public class Cell extends AbstractObject implements Tickable, Renderable, Clickable{

    public int rx;
    public int ry;
    
    protected TypeCellPart cellPart = TypeCellPart.space;
    
// Constructor ===============================================================================================

    public Cell(double x, double y, int rx, int ry){
        super(x, y, 40, IDObject.Cell);
        
        this.rx = rx;
        this.ry = ry;
        
        this.managerSideCell = new ManagerSideCell(this);
        this.managerAtoms = new ManagerAtoms(this);
    }

// Main Methods ==============================================================================================
    
    public Position getPosition(){
        return new Position(x, y, rx, ry);
    }
    
    public void setNewPosition(Position dp){
        this.x = dp.x;
        this.y = dp.y;
        this.rx = dp.rx;
        this.ry = dp.ry;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public boolean abstractConfirmAddAtoms(Player player, boolean explodeAdd){
        return false;
    }
    
    public void confirmAddAtoms(Player player, boolean explodeAdd){
        
        if(isCellPart(TypeCellPart.territory)){
            if(!abstractConfirmAddAtoms(player, explodeAdd)){
                return;
            }
        }
        
        if(getManagerAtoms().checkAtoms(Player.Dead)){
            getManagerAtoms().replaceAll(player);
            if(!explodeAdd) return;
        }

        if(getManagerAtoms().atomsSize() < this.getManagerAtoms().getMaxAtoms()) switch(getManagerAtoms().getMaxAtoms()){
            case 1 -> {
                getManagerAtoms().add(player);
            }
            case 2 -> {
                switch(getManagerAtoms().atomsSize()){
                    case 0 -> {
                        getManagerAtoms().add(player);
                    }
                    case 1 -> {
                        if(explodeAdd && !getManagerAtoms().checkAtoms(player)){
                            getManagerAtoms().replaceAllThenAdd(Player.Dead);
                        }else{
                            getManagerAtoms().add(player);
                        }
                    }
                }
            }
            case 3 -> {
                switch(getManagerAtoms().atomsSize()){
                    case 0, 1 -> {
                        getManagerAtoms().add(player);
                    }
                    case 2 -> {
                        if(explodeAdd){
                            if(getManagerAtoms().checkAtoms(player)){
                                getManagerAtoms().add(player);
                            }else{
                                getManagerAtoms().replaceAllThenAdd(Player.Dead);
                            }
                        }else{
                            if(getManagerAtoms().checkAtoms(player)){
                                getManagerAtoms().replaceAllThenAdd(player);
                            }
                        }
                    }
                }
            }
            case 4 -> {
                switch(getManagerAtoms().atomsSize()){
                    case 0, 1, 2 -> {
                        getManagerAtoms().add(player);
                    }
                    case 3 -> {
                        if(explodeAdd){
                            if(getManagerAtoms().checkAtoms(player)){
                                getManagerAtoms().add(player);
                            }else{
                                getManagerAtoms().replaceAllThenAdd(Player.Dead);
                            }
                        }else{
                            if(getManagerAtoms().checkAtoms(player)){
                                getManagerAtoms().replaceAllThenAdd(player);
                            }
                        }
                    }
                }
            }
        }else if(getManagerAtoms().isMaxAtoms()){
            setPop(player);
        }
    }
    
// ...........................................................................................................
    
    public boolean abstractValidateClickAddAtom(Player player){
        return true;
    }
    
    public boolean validateClickAddAtom(Player player){
        boolean valid = false;
        
        if(getManagerAtoms().checkAtoms(Player.Dead) || getManagerAtoms().isEmpty()){
            valid = true;
        }else{
            if(getManagerAtoms().atomsSize() >= getManagerAtoms().getMaxAtoms()){
                if(getManagerAtoms().checkAtoms(player)){
                    valid = true;
                }
            }else{
                switch(getManagerAtoms().getMaxAtoms()){
                    case 1, 2 -> {
                        valid = true;
                    }
                    case 3 -> {
                        switch(getManagerAtoms().getDifferentAtoms().length){
                            case 0 -> {
                                valid = true;
                            }
                            case 1 -> {
                                if(getManagerAtoms().atomsSize() == 2){
                                    if(getManagerAtoms().checkAtoms(player)){
                                        valid = true;
                                    }
                                }else{
                                    valid = true;
                                }
                            }
                            case 2 -> {
                                if(getManagerAtoms().checkAtoms(player)){
                                    valid = true;
                                }
                            }
                        }
                    }
                    case 4 -> {
                        switch(getManagerAtoms().getDifferentAtoms().length){
                            case 0, 1 -> {
                                valid = true;
                            }
                            case 2 -> {
                                if(getManagerAtoms().atomsSize() == 3){
                                    if(getManagerAtoms().checkAtoms(player)){
                                        valid = true;
                                    }
                                }else{
                                    valid = true;
                                }
                            }
                            case 3 -> {
                                if(getManagerAtoms().checkAtoms(player)){
                                    valid = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if(valid && isCellPart(TypeCellPart.territory)){
            if(!abstractValidateClickAddAtom(player)){
                valid = false;
            }
        }
        
        return valid;
    }
    
// TypeCell --------------------------------------------------------------------------------------------------
    
    public TypeCellPart getCellPart(){
        return this.cellPart;
    }

    public boolean isCellPart(TypeCellPart cellPart){
        return this.cellPart == cellPart;
    }
    
// ManagerAtoms ----------------------------------------------------------------------------------------------
    
    private ManagerAtoms managerAtoms;
    
    public ManagerAtoms getManagerAtoms(){
        return managerAtoms;
    }
    
    public void updateMaxAtoms(){
        managerAtoms.updateMaxAtoms(cellPart, managerSideCell);
    }
    
// ManagerSideCell -------------------------------------------------------------------------------------------
    
    private ManagerSideCell managerSideCell;
    
    public ManagerSideCell getManagerSideCell(){
        return this.managerSideCell;
    }
    
    public void resetManagerSideCell(){
        managerSideCell.reset();
    }
    
// Pop -------------------------------------------------------------------------------------------------------
    
    public boolean pop = false;
    public Player popPlayer = null;
    
    public void setPop(Player popPlayer){
        this.popPlayer = popPlayer;
        this.pop = true;
    }

    private boolean simulatePop = false;
    
    public void setSimulatePop(){
        this.simulatePop = true;
    }
    
    public boolean isSimulatingPop(){
        return simulatePop;
    }
    
// Future move -----------------------------------------------------------------------------------------------
    
    public boolean futureMove = false;
    public boolean futureMoveReady = false;
    
    public boolean processFutureMove(){
        boolean repeat = false;
        if(futureMove){
            ((CellMoveable)this).moveCell(false);
            futureMove = false;
        }
        return repeat;
    }
    
    public void processFutureMoveReady(){
        if(futureMoveReady){
            futureMove = true;
            futureMoveReady = false;
        }
    }
    
// Future atoms ----------------------------------------------------------------------------------------------
    
    public ArrayList<Player> futureAtoms = new ArrayList();
    
    public boolean hasFutureAtoms(){
        return !futureAtoms.isEmpty();
    }
    
    public void addFutureAtoms(Player player){
        this.futureAtoms.add(player);
    }

    public void processFutureAtoms(){
        
        for(Player a : futureAtoms.toArray(Player[]::new)){
            confirmAddAtoms(a, true);
            if(isCellPart(TypeCellPart.moveable)){
                if(explodeReady){
                    futureMoveReady = true;
                }
            }
            if(this.pop){
                getManagerAtoms().replaceAll(a);
            }
        }

        this.futureAtoms.clear();
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public boolean isInvalidMove(){
        return !validateClickAddAtom(HandlerPlayers.getPlayer());
    }
    
// Abstract Methods ==========================================================================================
    
    @Override
    public void reset(){
        this.getManagerSideCell().reset();
        this.getManagerAtoms().reset();
        this.futureAtoms.clear();
        this.resetFocused();
    }

// State ========================================================================== <editor-fold desc="State">
    
    public Stack<StateCell> stateCellStack = new Stack<>();
    
    public void resetState(){
        stateCellStack.clear();
    }
    
    protected Position lastPosition = null;
    
    public void saveState(){
        this.stateCellStack.add(new StateCell(getManagerAtoms().getArray(), getPosition()));
        if(this.stateCellStack.size() > main.undoLimit){
            this.stateCellStack.remove(0);
        }
    }

    public void undoState(){
        if(stateCellStack.empty()) return;
        StateCell sC = (StateCell) this.stateCellStack.pop();
        this.getManagerAtoms().setStateCell(sC);
        if(isCellPart(TypeCellPart.moveable)){
            ((CellMoveable)this).undoStateMove(sC);
        }
    }
    
// </editor-fold>
    
// Clickable ================================================================== <editor-fold desc="Clickable">
    
    private boolean leftPressed = false;
    
    @Override
    public void clickLeftPressed(){
        if(!HandlerTick.pause && !leftPressed && cellPart != TypeCellPart.space){
            if(!main.isSimulating() && validateClickAddAtom(HandlerPlayers.getPlayer())){
                main.saveStates();
                confirmAddAtoms(HandlerPlayers.getPlayer(), false);
                if(isCellPart(TypeCellPart.moveable)){
                    ((CellMoveable)this).moveCell(false);
                }
                HandlerPlayers.nextPlayer();
                leftPressed = true;
            }
        }
    }

    @Override
    public void clickLeftReleased(){
        leftPressed = false;
    }

    @Override
    public boolean isLeftPressed(){
        return leftPressed;
    }

    private boolean rightPressed = false;
    
    @Override
    public void clickRightPressed(){
        rightPressed = true;
    }

    @Override
    public void clickRightReleased(){
        rightPressed = false;
    }

    @Override
    public boolean isRightPressed(){
        return rightPressed;
    }

    @Override
    public Rectangle getClickableBounds(){
        int boundX = this.getX() - (int) Math.floor((double) (main.gapSize / 2));
        int boundY = this.getY() - (int) Math.floor((double) (main.gapSize / 2));
        return new Rectangle(boundX, boundY, 40 + main.gapSize, 40 + main.gapSize);
    }

// </editor-fold>
    
// Tickable ==================================================================== <editor-fold desc="Tickable">
    
    @Override
    public void tick(){
        tickCheckFocus();
        tickAnimations();
        
        if(getManagerAtoms().getMaxAtoms() != 0 && !isCellPart(TypeCellPart.space)){
            
            tickExplodeReady();
            tickAngle();
            tickExplode();

            if(pop && simulatePop){
                explodeAnimation = 1.0D;
                explodeTick = main.explodeBuffer;
                explodeColor = popPlayer.color;
                
                tickPopReady();
                
                pop = false;
                popPlayer = null;
                getManagerAtoms().reset();
            }
        }
    }
   
// -----------------------------------------------------------------------------------------------------------
    
    public void tickExplodeReady(){
        explodeReady = getManagerAtoms().atomsSize() >= this.getManagerAtoms().getMaxAtoms();
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    protected void tickAnimations(){
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public void tickPopReady(){
        for(IDDirection d : IDDirection.values()){
            Cell c = getManagerSideCell().getSide(d);
            if(c != null){
                c.addFutureAtoms(popPlayer);
            }
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public void tickTurn(){
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private int explodeTick = 0;
    public double explodeAnimation = 0.0;
    public Color explodeColor;
    
    private void tickExplode(){
        if(explodeTick > 0){
            explodeTick--;
            explodeAnimation = (double) this.explodeTick / (double) main.explodeBuffer;
            if(this.explodeTick <= 0){
                explodeTick = 0;
                explodeAnimation = 0.0D;
                simulatePop = false;
            }
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public boolean focused = false;
    public boolean focusedTeleport = false;
    
    public IDDirection sideFocused = null;
    public Cell sideFocusedCell = null;
    
    public void resetFocused(){
        focused = false;
        sideFocused = null;
        
        highlightXcoord = false;
        highlightYcoord = false;
    }
    
    private void tickCheckFocus(){
        if(!isCellPart(TypeCellPart.space) || focused){
            if(HandlerTick.cursorLocation != null && getClickableBounds().contains(HandlerTick.cursorLocation)){
                if(!focused){
                    setFocused(true);
                }
            }else if(focused){
                setFocused(false);
            }

            if(focused && this.isCellPart(TypeCellPart.space)){
                setFocused(false);
            }
        }
    }
    
    protected void setFocused(boolean focus){
        focused = focus;
        
        if(!focus){
            for(Cell c : managerSideCell.getArray()) if(c != null) c.sideFocused = null;
            highlightXcoord = false;
            highlightYcoord = false;
        }else{
            for(IDDirection d : IDDirection.values()) if(managerSideCell.haveSide(d)){
                Cell c = managerSideCell.getSide(d);
                c.sideFocused = d.getInverted();
                c.sideFocusedCell = this;
            }
            highlightXcoord = true;
            highlightYcoord = true;
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private double angle = (double)MethodsNumber.getRandomNumber(1, 359);
    
    private void tickAngle(){
        if(this.focused && this.getManagerAtoms().checkAtoms(HandlerPlayers.getPlayer())){
            angle += explodeReady ? 1.2 : 0.05;
        }else{
            angle += explodeReady ? 0.4 : 0.05;
        }
        if(angle >= 360.0){
            angle = 0.0;
        }
    }
    
// </editor-fold>
    
// Renderable ================================================================ <editor-fold desc="Renderable">
    
    protected boolean explodeReady = false;
    
    private Color coordinateDefaultColor = new Color(50, 50, 50);
    
    public Color portalLineColor = new Color(100, 100, 100);
    
    @Override
    public boolean isRenderReady(){
        return this.getManagerSideCell() != null || this.isCellPart(TypeCellPart.space);
    }

// Layer 1 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer1(Graphics2D g){
        if(isCellPart(TypeCellPart.space)) return;
    }

// Layer 2 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer2(Graphics2D g){
        drawCoordinate(g);
        if(isCellPart(TypeCellPart.space)) return;
        drawPortalDesign(g);
        drawCellBorder(g);
        drawDesign(g);
    }
    
// ...........................................................................................................
    
    public boolean drawXcoord = false;
    public boolean drawYcoord = false;
    
    public boolean highlightXcoord = false;
    public boolean highlightYcoord = false;
    
    private void drawCoordinate(Graphics2D g){
        if(this.drawXcoord){
            boolean draw = false;
            for(Cell c : HandlerCell.getArray()) if(c.rx == rx && c.highlightXcoord){
                draw = true;
                break;
            }
            g.setColor(draw ? Color.white : this.coordinateDefaultColor);
            g.drawString(String.valueOf(this.rx + 1), this.getX() + 15, this.getY() - this.ry * main.totalSize - 20);
        }
        if(this.drawYcoord){
            boolean draw = false;
            for(Cell c : HandlerCell.getArray()) if(c.ry == ry && c.highlightYcoord){
                draw = true;
                break;
            }
            g.setColor(draw ? Color.white : this.coordinateDefaultColor);
            g.drawString(String.valueOf(this.ry + 1), this.getX() + (SettingsCell.xCell - this.rx) * main.totalSize + 10, this.getY() + 23);
        }
    }
    
// ...........................................................................................................
    
    private void drawPortalDesign(Graphics2D g){
        g.setColor(SettingsCell.cellBoxColor);
        for(IDDirection d : IDDirection.values()){
            Cell c = this.getManagerSideCell().getSide(d);
            if(c == null) continue;
            
            if(
                c.isCellPart(TypeCellPart.portal) || c.isCellPart(TypeCellPart.specialPortal) ||
                isCellPart(TypeCellPart.portal) || isCellPart(TypeCellPart.specialPortal)
            ) switch(d){
                case U -> {
                    drawPortalDesignColor(g, d);
                    if(this.ry - c.ry >= 2) this.gFillRect(g, 13, -4, -25, -36);
                }
                case D -> {
                    drawPortalDesignColor(g, d);
                    if(c.ry - this.ry >= 2) this.gFillRect(g, 13, 40, -25, -36);
                }
                case L -> {
                    drawPortalDesignColor(g, d);
                    if(this.rx - c.rx >= 2) this.gFillRect(g, -4, 13, -36, -25);
                }
                case R -> {
                    drawPortalDesignColor(g, d);
                    if(c.rx - this.rx >= 2) this.gFillRect(g, 40, 13, -36, -25);
                }
            }
        }
    }
    
    private void drawPortalDesignColor(Graphics2D g, IDDirection d){
        g.setColor(SettingsCell.cellBoxColor);
        if(!main.isSimulating()){
            if(focused){
                g.setColor(isInvalidMove() ? SettingsCell.invalidColor : HandlerPlayers.getPlayerColor());
            }else if(sideFocused == d && sideFocusedCell != null){
                g.setColor(sideFocusedCell.isInvalidMove() ? SettingsCell.invalidColor : HandlerPlayers.getPlayerColor());
            }
        }
    }
    
// ...........................................................................................................
    
    public Color drawBorder = null;
    
    private void drawCellBorder(Graphics2D g){
        gFillRect(g, SettingsCell.cellBackground, 0, 0, 0, 0);
        
        if(!main.isSimulating() && focused){
            drawCellBorderFocused(g);
            drawBorder = isInvalidMove() ? SettingsCell.invalidColor : HandlerPlayers.getPlayerColor();
        }

        if(drawBorder != null){
            g.setColor(drawBorder);
        }else{
            g.setColor(SettingsCell.cellBoxColor);
        }
        g.setStroke(new BasicStroke(2.0F));
        gDrawRect(g, 0, 0, 0, 0);
        g.setStroke(new BasicStroke(1.0F));
        
        drawBorder = null;
    }
    
    protected void drawCellBorderFocused(Graphics2D g){
        for(Cell c : getManagerSideCell().getArray()) if(c != null){
            c.drawBorder = isInvalidMove() ? SettingsCell.invalidColor : HandlerPlayers.getPlayerColor();
        }
    }
    
    private void drawDesign(Graphics2D g){
        g.setColor(Color.lightGray);
        switch(getManagerSideCell().countSide()){
            case 1 -> {
                if(this.getManagerSideCell().haveU()){
                    drawDetailLine(g, IDDirection.D, IDDirection.L);
                    drawDetailLine(g, IDDirection.D, IDDirection.R);
                }else if(this.getManagerSideCell().haveD()){
                    drawDetailLine(g, IDDirection.U, IDDirection.L);
                    drawDetailLine(g, IDDirection.U, IDDirection.R);
                }else if(this.getManagerSideCell().haveL()){
                    drawDetailLine(g, IDDirection.U, IDDirection.R);
                    drawDetailLine(g, IDDirection.D, IDDirection.R);
                }else if(this.getManagerSideCell().haveR()){
                    drawDetailLine(g, IDDirection.U, IDDirection.L);
                    drawDetailLine(g, IDDirection.D, IDDirection.L);
                }
            }
            case 2 -> {
                if(this.getManagerSideCell().haveH()){
                    drawDetailLine(g, IDDirection.U);
                    drawDetailLine(g, IDDirection.D);
                }else if(this.getManagerSideCell().haveV()){
                    drawDetailLine(g, IDDirection.L);
                    drawDetailLine(g, IDDirection.R);
                }else if(this.getManagerSideCell().haveU()){
                    if(this.getManagerSideCell().haveL()){
                        drawDetailLine(g, IDDirection.D, IDDirection.R);
                    }else if(this.getManagerSideCell().haveR()){
                        drawDetailLine(g, IDDirection.D, IDDirection.L);
                    }
                }else if(this.getManagerSideCell().haveD()){
                    if(this.getManagerSideCell().haveL()){
                        drawDetailLine(g, IDDirection.U, IDDirection.R);
                    }else if(this.getManagerSideCell().haveR()){
                        drawDetailLine(g, IDDirection.U, IDDirection.L);
                    }
                }
            }
            case 3 -> {
                if(!this.getManagerSideCell().haveU()){
                    drawDetailLine(g, IDDirection.U);
                }else if(!this.getManagerSideCell().haveD()){
                    drawDetailLine(g, IDDirection.D);
                }else if(!this.getManagerSideCell().haveL()){
                    drawDetailLine(g, IDDirection.L);
                }else if(!this.getManagerSideCell().haveR()){
                    drawDetailLine(g, IDDirection.R);
                }
            }
        }
    }
    
    private void drawDetailLine(Graphics2D g, IDDirection d1){
        drawDetailLine(g, d1, null);
    }
    
    private void drawDetailLine(Graphics2D g, IDDirection d1, IDDirection d2){
        switch(d1){
            case U -> { 
                if(d2 != null) switch(d2){
                    case L -> {
                        g.drawLine(getX(6), getY(6), getX(6), getY(13));
                        g.drawLine(getX(6), getY(6), getX(13), getY(6));
                    }
                    case R -> {
                        g.drawLine(getXW(-6), getY(6), getXW(-6), getY(13));
                        g.drawLine(getXW(-6), getY(6), getXW(-13), getY(6));
                    }
                }else{
                    g.drawLine(getX(15), getY(6), getXW(-15), getY(6));
                }
            }
            case D -> {
                if(d2 != null) switch(d2){
                    case L -> {
                        g.drawLine(getX(6), getYH(-6), getX(6), getYH(-13));
                        g.drawLine(getX(6), getYH(-6), getX(13), getYH(-6));
                    }
                    case R -> {
                        g.drawLine(getXW(-6), getYH(-6), getXW(-6), getYH(-13));
                        g.drawLine(getXW(-6), getYH(-6), getXW(-13), getYH(-6));
                    }
                }else{
                    g.drawLine(getX(15), getYH(-6), getXW(-15), getYH(-6));
                }
            }
            case L -> {
                g.drawLine(getX(6), getY(15), getX(6), getYH(-15));
            }
            case R -> {
                g.drawLine(getXW(-6), getY(15), getXW(-6), getYH(-15));
            }
        }
    }
    
// Layer 3 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer3(Graphics2D g){
        if(isCellPart(TypeCellPart.space)) return;
        drawAtoms(g);
    }
    
// ...........................................................................................................
    
    protected int atomSize = 10;
    
    private void drawAtoms(Graphics2D g) {
        int atomCount = getManagerAtoms().atomsSize();
        if (atomCount == 0) {
            return;
        }

        int half = (20 - atomSize) + (atomSize / 2);

        if (atomCount == 1) {
            g.setColor(getManagerAtoms().getColor(0));
            if (this.explodeReady) {
                double radians = this.angle / 180.0 * Math.PI;
                double x1 = 3 * Math.cos(-radians);
                double y1 = 3 * Math.sin(-radians);
                gEllipse(g, getX((int) x1 + half), getY((int) y1 + half), atomSize);
            } else {
                gEllipse(g, getX(half), getY(half), atomSize);
            }
        } else {
            double angleIncrement = 360.0 / atomCount;
            for (int i = 0; i < atomCount; i++) {
                double radians = (this.angle + i * angleIncrement) / 180.0 * Math.PI;
                double x1 = 8 * Math.cos(-radians);
                double y1 = 8 * Math.sin(-radians);
                gEllipse(g, getManagerAtoms().getColor(i), getDoubleX(x1 + half), getDoubleY(y1 + half), atomSize);
            }
        }
    }
    
// Layer 4 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer4(Graphics2D g){
        if(isCellPart(TypeCellPart.space)) return;
    }
    
// Layer 5 ---------------------------------------------------------------------------------------------------
    
    @Override
    public void renderLayer5(Graphics2D g){
        if(isCellPart(TypeCellPart.space)) return;
        drawExplode(g);
    }
    
// ...........................................................................................................
    
    protected int drawExplodeHalf = 0;
    
    private void drawExplode(Graphics2D g){
        drawExplodeHalf = (20 - atomSize) + (int)(atomSize / 2);
        
        if(this.explodeAnimation != 0.0D){
            drawExplodeSet(g);
        }
    }
    
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
    
// </editor-fold>
    
}
