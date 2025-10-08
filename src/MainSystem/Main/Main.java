
package MainSystem.Main;

import DataSystem.Data.Player;
import DataSystem.Maps.AbstractMap;
import DataSystem.Maps.*;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import MainSystem.Object.CellType.CellDuplicator;
import MainSystem.Object.CellType.CellTeleport;
import MainSystem.Object.CellType.CellMoveable;
import MainSystem.Object.CellType.CellNoEntry;
import MainSystem.Object.PlayerIndicate;
import MainSystem.Threads.ThreadProcess;
import ManagerSystem.Handlers.HandlerObjects;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import ManagerSystem.Handlers.HandlerPlayers;
import ManagerSystem.Handlers.HandlerSystem.HandlerRender;
import ManagerSystem.Handlers.HandlerSystem.HandlerTick;
import ManagerSystem.Listeners.KeyListener;
import Settings.SettingsCell;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class Main{

    public Window window;
    
// Settings ==================================================================================================
    
    public int undoLimit = 10;
    public int explodeBuffer = 200;
    public int moveBuffer = 50;
    
    public int gapSize = 10;
    public int borderSize = 50;
    public int totalSize;
    public Dimension canvasSize;
    
    public AbstractMap map = new Map14();
    
// Constructor ===============================================================================================
    
    public static void main(String[] args){
        Main main = new Main();
        
        Player.main = main;
        
        KeyListener.main = main;
        Console.main = main;
        PlayerIndicate.main = main;
        Window.main = main;
        
        HandlerRender.main = main;
        HandlerTick.main = main;
        HandlerCell.main = main;
        HandlerPlayers.main = main;
        
        ThreadProcess.main = main;
        
        main.init();
    }
    
    private void init(){
        setDefaults();
        setWindow();
        startThreads();
    }

// Constructor's Methods =====================================================================================

    private void setWindow(){
        window = new Window(canvasSize, 1);
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private void setDefaults(){
        Console.out("Setting Up Defaults", "\u001b[0;32m");
        
        if(map != null){
            int[] mapSize = map.getMapSize();
            SettingsCell.xCell = mapSize[0];
            SettingsCell.yCell = mapSize[1];
        }
        
        totalSize = 40 + gapSize;
        canvasSize = new Dimension(SettingsCell.xCell * this.totalSize + this.gapSize + this.borderSize * 2 + 150, SettingsCell.yCell * this.totalSize + this.gapSize + this.borderSize * 2 + 50);
        
        this.setMap();
        HandlerCell.updateCells();
        
        int y = 50;
        for(Player dP : Player.values()) if(dP != Player.Dead){
            HandlerObjects.add(new PlayerIndicate((double)(canvasSize.width - 110), (double)y, dP));
            y += 50;
        }
    }
    
// Main Methods ==============================================================================================
    
    public boolean cursorHand = false;
    
    public void tick(){
        
        for(Cell c : HandlerCell.getArray()){
            cellFocused = false;
            if(c.focused){
                cellFocused = true;
                break;
            }
        }
        
        tickSimulateReaction();
        HandlerPlayers.tick();
        
        if(cellFocused){
            if(!cursorHand){
                window.setCursor(12);
                cursorHand = true;
            }
        }else if(cursorHand){
            window.setCursor(0);
            cursorHand = false;
        }
    }
    
// -----------------------------------------------------------------------------------------------------------

    public boolean cellFocused;
    
    public boolean isCellFocused(){
        return cellFocused;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private boolean simulateReaction = false;
    private int simulatePhase = 1;
    
    public boolean isSimulating(){
        return simulateReaction;
    }
    
    public void tickSimulateReaction(){
        if(!simulateReaction){
            for(Cell c : HandlerCell.getArray()) if(c.pop){
                simulateReaction = true;
                break;
            }
            if(!simulateReaction) return;
        }
        
        switch(simulatePhase){
            case 1 -> {
                // Pop first
                boolean repeatSimulate = false;
                for(Cell c : HandlerCell.getArray()){
                    if(c.pop){
                        c.setSimulatePop();
                        repeatSimulate = true;
                        simulatePhase = 2;
                    }
                }
                if(!repeatSimulate){
                    simulateReaction = false;
                }
            }
            case 2 -> {
                // Add atoms
                for(Cell c : HandlerCell.getArray()){
                    if(c.isSimulatingPop()) return;
                }
                for(Cell c : HandlerCell.getArray()){
                    c.processFutureAtoms();
                }
                simulatePhase = 3;
            }
            case 3 -> {
                // Move moveable
                boolean repeat = false;
                do{
                    repeat = false;
                    for(Cell c : HandlerCell.getArray()){
                        if(!repeat){
                            repeat = c.processFutureMove();
                        }else{
                            c.processFutureMove();
                        }
                    }
                }while(repeat);
                simulatePhase = 4;
            }
            case 4 -> {
                // Move ready
                for(CellMoveable c : HandlerCell.getCellMoveableArray()){
                    if(c.isSimualteMove()) return;
                }
                for(Cell c : HandlerCell.getArray()){
                    c.processFutureMoveReady();
                }
                simulatePhase = 1;
            }
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    private int[] turnHintBorder = new int[]{0, 0, 0, 0};
    
    public void preRender(Graphics2D g){
        g.setColor(HandlerPlayers.getPlayerColor());
        g.setStroke(new BasicStroke(2.0F));
        g.drawRect(turnHintBorder[0], turnHintBorder[1], turnHintBorder[2], turnHintBorder[3]);
        g.setStroke(new BasicStroke(1.0F));
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public void setMap(){
        
        int[][] mapData = null;
        int[][] mapMirrorData = null;
        int[][] mapNoEntryData = null;
        int[][] mapMoveable = null;
        int[][] mapDuplicator = null;
        
        if(this.map != null){
            mapData = map.getMap();
            mapMirrorData = map.getTeleportCells();
            mapNoEntryData = map.getNoEntryCells();
            mapMoveable = map.getMoveable();
            mapDuplicator = map.getDuplicatorCells();
            if(mapData != null){
                for(int x = 0; x < SettingsCell.xCell; x++){
                    for(int y = 0; y < SettingsCell.yCell; y++){
                        int mapBit = mapData[y][x];
                        
                        int mapBitMirror = -1;
                        int mapBitNoEntry = -1;
                        int mapBitMoveable = -1;
                        int mapBitDuplicator = -1;
                        
                        if(mapMirrorData != null){
                            mapBitMirror = mapMirrorData[y][x];
                        }
                        if(mapNoEntryData != null){
                            mapBitNoEntry = mapNoEntryData[y][x];
                        }
                        if(mapMoveable != null){
                            mapBitMoveable = mapMoveable[y][x];
                        }
                        if(mapDuplicator != null){
                            mapBitDuplicator = mapDuplicator[y][x];
                        }
                        
                        TypeCellPart typeCell = TypeCellPart.getTypeCellPart(mapBit);
                        
                        Cell c = TypeCellPart.createCell(typeCell, this.borderSize + this.gapSize + x * this.totalSize, this.borderSize + this.gapSize + y * this.totalSize + 10, x, y);
                        
                        switch(typeCell){
                            case teleport -> {
                                ((CellTeleport)c).setTeleport(mapBitMirror);
                            }
                            case noEntry -> {
                                ((CellNoEntry)c).setNoEntry(mapBitNoEntry);
                            }
                            case moveable -> {
                                ((CellMoveable)c).setMoveable(mapBitMoveable);
                            }
                            case duplicator -> {
                                ((CellDuplicator)c).setDuplicator(mapBitDuplicator);
                            }
                        }
                        
                        if(x == SettingsCell.xCell - 1){
                            c.drawYcoord = true;
                        }
                        if(y == 0){
                            c.drawXcoord = true;
                        }
                        HandlerObjects.add(c);
                    }
                }
            }
            
            turnHintBorder = new int[]{
                borderSize + this.gapSize - 12,
                borderSize + this.gapSize + 10 - 12,
                (SettingsCell.xCell - 1) * this.totalSize + 40 + 24,
                (SettingsCell.yCell - 1) * this.totalSize + 40 + 24
            };
        }
    }
    
    public void reset(){
        HandlerCell.Reset();
        HandlerCell.updateCells();
        HandlerPlayers.Reset();
        
        Player.Reset();
        
        resetState();
    }
    
// Threads ===================================================================================================
    
    public boolean running = false;
    public boolean exiting = false;
    public ThreadProcess tProcess;
    
    private void startThreads(){
        Console.out("\nStarting Threads", "\u001b[0;32m");
        if(!this.running){
            this.running = true;
            this.tProcess = new ThreadProcess();
            this.tProcess.start();
        }else{
            Console.out("Threads Already Started", "\u001b[0;31m");
        }
    }

    public void stopThreads(){
        Console.gap();
        Console.out("Closing Threads", "\u001b[0;31m");
        this.exiting = true;
        this.tProcess.stop();
        this.running = false;
    }
    
// State =====================================================================================================
    
    public void resetState(){
        HandlerCell.ResetState();
        HandlerPlayers.ResetState();
    }
    
    public void saveStates(){
        HandlerCell.SaveState();
        HandlerPlayers.SaveStates();
    }
    
    public void undoStates(){
        if(HandlerTick.pause || isSimulating()) return;
        
        HandlerCell.UndoState();
        HandlerPlayers.UndoStates();
    }
    
}
