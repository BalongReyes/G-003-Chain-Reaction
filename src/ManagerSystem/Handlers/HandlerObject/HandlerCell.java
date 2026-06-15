package ManagerSystem.Handlers.HandlerObject;

import DataSystem.Data.Position;
import DataSystem.ID.IDDirection;
import DataSystem.Type.TypeCellPart;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Main;
import MainSystem.Object.Cell;
import MainSystem.Object.CellType.CellMirror;
import MainSystem.Object.CellType.CellMoveable;
import MainSystem.Object.CellType.CellTeleport;
import MainSystem.Object.CellType.CellNoEntry;
import MainSystem.Object.CellType.CellSidePortal;
import MainSystem.Object.CellType.CellSpecialPortal;
import ManagerSystem.Manager.ManagerCell.ManagerSideCell;
import Settings.SettingsCell;
import java.util.ArrayList;
import java.util.List;

public class HandlerCell{

    private final java.util.List<Cell> cells = new ArrayList<>();
    private Cell[] cellsCache = new Cell[0];
    private boolean cellsDirty = false;

    private Cell[][] grid = new Cell[SettingsCell.xCell][SettingsCell.yCell];

    public void updateGridSize() {
        if (grid == null || grid.length != SettingsCell.xCell || grid[0].length != SettingsCell.yCell) {
            grid = new Cell[SettingsCell.xCell][SettingsCell.yCell];
        }
    }

    public void add(AbstractObject o){
        if(o instanceof Cell c){
            cells.add(c);
            cellsDirty = true;
            if (c.rx >= 0 && c.rx < SettingsCell.xCell && c.ry >= 0 && c.ry < SettingsCell.yCell) {
                grid[c.rx][c.ry] = c;
            }
        }
        if(o instanceof CellMoveable cm){
            moveableCells.add(cm);
            moveableCellsDirty = true;
        }
    }

    public void remove(AbstractObject o){
        if(o instanceof Cell c){
            cells.remove(c);
            cellsDirty = true;
            if (c.rx >= 0 && c.rx < SettingsCell.xCell && c.ry >= 0 && c.ry < SettingsCell.yCell) {
                if(grid[c.rx][c.ry] == c) grid[c.rx][c.ry] = null;
            }
        }
        if(o instanceof CellMoveable cm){
            moveableCells.remove(cm);
            moveableCellsDirty = true;
        }
    }

    public void swap(Cell sC, Cell tC){
        Position sDP = sC.getPosition();
        Position tDP = tC.getPosition();
        sC.setNewPosition(tDP);
        tC.setNewPosition(sDP);
        
        if (sDP.rx >= 0 && sDP.rx < SettingsCell.xCell && sDP.ry >= 0 && sDP.ry < SettingsCell.yCell) {
            grid[sDP.rx][sDP.ry] = tC;
        }
        if (tDP.rx >= 0 && tDP.rx < SettingsCell.xCell && tDP.ry >= 0 && tDP.ry < SettingsCell.yCell) {
            grid[tDP.rx][tDP.ry] = sC;
        }
    }
    
    public void swapPosition(Cell sC, Cell tC, Position sCp, Position tCp){
        sC.setNewPosition(tCp);
        tC.setNewPosition(sCp);
        
        if (sCp.rx >= 0 && sCp.rx < SettingsCell.xCell && sCp.ry >= 0 && sCp.ry < SettingsCell.yCell) {
            grid[sCp.rx][sCp.ry] = tC;
        }
        if (tCp.rx >= 0 && tCp.rx < SettingsCell.xCell && tCp.ry >= 0 && tCp.ry < SettingsCell.yCell) {
            grid[tCp.rx][tCp.ry] = sC;
        }
    }
    
    public boolean check(AbstractObject o){
        return o instanceof Cell;
    }

    public Cell[] getArray(){
        if(cellsDirty){
            cellsCache = cells.toArray(Cell[]::new);
            cellsDirty = false;
        }
        return cellsCache;
    }

    public void Reset(){
        for(Cell c : getArray()){
            c.reset();
        }
    }

// -----------------------------------------------------------------------------------------------------------
    
    private final java.util.List<CellMoveable> moveableCells = new ArrayList<>();
    private CellMoveable[] moveableCellsCache = new CellMoveable[0];
    private boolean moveableCellsDirty = false;
    
    public CellMoveable[] getCellMoveableArray(){
        if(moveableCellsDirty){
            moveableCellsCache = moveableCells.toArray(CellMoveable[]::new);
            moveableCellsDirty = false;
        }
        return moveableCellsCache;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public void ResetState(){
        for(Cell c : getArray()){
            c.resetState();
        }
    }
    
    public void SaveState(){
        for(Cell c : getArray()){
            c.saveState();
        }
    }

    public void UndoState(){
        for(Cell c : getArray()){
            c.undoState();
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public Cell[] getCellTeleport(int teleportCellPart){
        Cell[] output = new Cell[]{null, null};
        int i = 0;
        for(Cell c : getArray()){
            if(c instanceof CellTeleport cm){
                if(cm.isTeleportPart(teleportCellPart)){
                    output[i] = c;
                    i++;
                }
            }
            if(i == 2) break;
        }
        
        if(output[0] == null || output[1] == null){
            return null;
        }else{
            return output;
        }
    }
    
    public List<Cell> getCellMirror(int mirrorCellPart) {
        List<Cell> output = new ArrayList<>();
        for (Cell c : getArray()) {
            if (c instanceof CellMirror cm) {
                if (cm.isMirrorPart(mirrorCellPart)) {
                    output.add(c);
                }
            }
        }
        return output;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public Cell getCell(Cell c, IDDirection d){
        return getCell(c, d, 1);
    }

    public Cell getCell(Cell c, IDDirection d, int distance){
        return switch(d){
            case U -> getCell(c.rx, c.ry - distance);
            case D -> getCell(c.rx, c.ry + distance);
            case L -> getCell(c.rx - distance, c.ry);
            case R -> getCell(c.rx + distance, c.ry);
            case UL -> getCell(c.rx - distance, c.ry - distance);
            case UR -> getCell(c.rx + distance, c.ry - distance);
            case DL -> getCell(c.rx - distance, c.ry + distance);
            case DR -> getCell(c.rx + distance, c.ry + distance);
            default -> null;
        };
    }

    public Cell getCell(int rx, int ry){
        if(rx >= 0 && rx < SettingsCell.xCell && ry >= 0 && ry < SettingsCell.yCell){
            Cell c = grid[rx][ry];
            if (c != null && !c.isCellSpace()) {
                return c;
            }
        }
        return null;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public Cell getAllCell(Cell c, IDDirection d, int distance){
        return switch(d){
            case U -> getAllCell(c.rx, c.ry - distance);
            case D -> getAllCell(c.rx, c.ry + distance);
            case L -> getAllCell(c.rx - distance, c.ry);
            case R -> getAllCell(c.rx + distance, c.ry);
            case UL -> getAllCell(c.rx - distance, c.ry - distance);
            case UR -> getAllCell(c.rx + distance, c.ry - distance);
            case DL -> getAllCell(c.rx - distance, c.ry + distance);
            case DR -> getAllCell(c.rx + distance, c.ry + distance);
            default -> null;
        };
    }

    public Cell getAllCell(int rx, int ry){
        if(rx >= 0 && rx < SettingsCell.xCell && ry >= 0 && ry < SettingsCell.yCell){
            return grid[rx][ry];
        }
        return null;
    }

// -----------------------------------------------------------------------------------------------------------

    public void removeAllSideCells(Cell c){
        ManagerSideCell managerSideCell = c.getManagerSideCell();
        Cell[] sideCells = managerSideCell.sideCells;
        for(IDDirection d : IDDirection.values()){
            Cell tC = sideCells[d.index];
            if(tC == null) continue;
            tC.getManagerSideCell().setSide(null, d.getInverted());
            managerSideCell.setSide(null, d);
        }
    }
    
    public void updateCells(Main main){
        
        for(Cell sC : getArray()){
            sC.resetManagerSideCell();
            switch(sC.getCellPart()){
                case specialPortal -> {
                    ((CellSpecialPortal)sC).resetSpecialPortalUnderground();
                }
                case sidePortal -> {
                    ((CellSidePortal)sC).resetHighlightSidePortal();
                }
            }
        }
        
        for(Cell sC : getArray()){
            for(IDDirection d : IDDirection.values()){
                if (!sC.supportDiagonal() && d.isDiagonal()) continue;
                
                Cell tC = getCell(sC, d);
                if(sC.getCellPart() == TypeCellPart.noEntry){
                    if(((CellNoEntry)sC).getNoEntry() == d){
                        continue;
                    }
                }
                sC.getManagerSideCell().setSide(tC, d);
            }
        }
        
        for(Cell sC : getArray()){
            if(sC.isCellPart(TypeCellPart.specialPortal)) for(IDDirection d : IDDirection.values()){
                if (!sC.supportDiagonal() && d.isDiagonal()) continue;
                if(sC.getManagerSideCell().haveSide(d)) continue;
                
                boolean specialPortalUnderground = false;
                for(int distance = 2; distance <= main.map.getMaxMapSize(); distance++){
                    Cell tC = getCell(sC, d, distance);

                    if(tC != null && !tC.getManagerSideCell().haveSide(d.getInverted())){
                        if(tC.isCellPart(TypeCellPart.specialPortal)){
                            
                            
                            sC.getManagerSideCell().setSide(tC, d);
                            tC.getManagerSideCell().setSide(sC, d.getInverted());

                            ((CellSpecialPortal)sC).specialPortalUnderground[d.index] = specialPortalUnderground;
                            ((CellSpecialPortal)tC).specialPortalUnderground[d.getInverted().index] = specialPortalUnderground;
                            break;
                        }
                        specialPortalUnderground = true;
                    }
                }
            }
        }
        
        for(Cell sC : getArray()){
            if(sC.isCellPart(TypeCellPart.portal)) for(IDDirection d : IDDirection.values()){
                if (!sC.supportDiagonal() && d.isDiagonal()) continue;
                if(sC.getManagerSideCell().haveSide(d)) continue;
                
                for(int distance = 2; distance <= main.map.getMaxMapSize(); ++distance){
                    Cell tC = getCell(sC, d, distance);

                    if(tC != null){
                        if(!tC.isCellPart(TypeCellPart.specialPortal) || !tC.getManagerSideCell().haveSide(d.getInverted())){
                            sC.getManagerSideCell().setSide(tC, d);
                            tC.getManagerSideCell().setSide(sC, d.getInverted());
                        }
                        break;
                    }
                }
            }
        }
        
        for(Cell sC : getArray()){
            if(sC.isCellPart(TypeCellPart.sidePortal) && (sC.rx == 0 || sC.ry == 0 || sC.rx == SettingsCell.xCell - 1 || sC.ry == SettingsCell.yCell - 1)){
                
                for(IDDirection d : IDDirection.values()){
                    if (!sC.supportDiagonal() && d.isDiagonal()) continue;
                    if(sC.getManagerSideCell().haveSide(d)) continue;
                    
                    if(sC.rx == 0){
                        Cell tC = getCell(SettingsCell.xCell - 1, sC.ry);
                        if(tC == null){
                            continue;
                        }

                        sC.getManagerSideCell().setSide(tC, IDDirection.L);
                        tC.getManagerSideCell().setSide(sC, IDDirection.R);
                        ((CellSidePortal)sC).highlightSidePortal[IDDirection.L.index] = true;
                        if(tC.isCellPart(TypeCellPart.sidePortal)){
                            ((CellSidePortal)tC).highlightSidePortal[IDDirection.R.index] = true;
                        }
                    }

                    if(sC.rx == SettingsCell.xCell - 1){
                        Cell tC = getCell(0, sC.ry);
                        if(tC == null){
                            continue;
                        }

                        sC.getManagerSideCell().setSide(tC, IDDirection.R);
                        tC.getManagerSideCell().setSide(sC, IDDirection.L);
                        ((CellSidePortal)sC).highlightSidePortal[IDDirection.R.index] = true;
                        if(tC.isCellPart(TypeCellPart.sidePortal)){
                            ((CellSidePortal)tC).highlightSidePortal[IDDirection.L.index] = true;
                        }
                    }

                    if(sC.ry == 0){
                        Cell tC = getCell(sC.rx, SettingsCell.yCell - 1);
                        if(tC == null){
                            continue;
                        }

                        sC.getManagerSideCell().setSide(tC, IDDirection.U);
                        tC.getManagerSideCell().setSide(sC, IDDirection.D);
                        ((CellSidePortal)sC).highlightSidePortal[IDDirection.U.index] = true;
                        if(tC.isCellPart(TypeCellPart.sidePortal)){
                            ((CellSidePortal)tC).highlightSidePortal[IDDirection.D.index] = true;
                        }
                    }

                    if(sC.ry == SettingsCell.yCell - 1){
                        Cell tC = getCell(sC.rx, 0);
                        if(tC != null){
                            sC.getManagerSideCell().setSide(tC, IDDirection.D);
                            tC.getManagerSideCell().setSide(sC, IDDirection.U);
                            ((CellSidePortal)sC).highlightSidePortal[IDDirection.D.index] = true;
                            if(tC.isCellPart(TypeCellPart.sidePortal)){
                                ((CellSidePortal)tC).highlightSidePortal[IDDirection.U.index] = true;
                            }
                        }
                    }
                }
            }
        }

        for(Cell sC : getArray()){
            sC.updateMaxAtoms();
            switch(sC){
                case CellTeleport ct -> ct.updateTeleport();
                case CellMirror cm -> cm.updateMirror();
                default -> {
                }
            }
        }
        
        resetFocused();
    }
    
    public void resetFocused(){
        for(Cell c : getArray()){
            c.resetFocused();
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public void tickTurn(){
        for(Cell c : getArray()){
            c.tickTurn();
        }
    }
    
}