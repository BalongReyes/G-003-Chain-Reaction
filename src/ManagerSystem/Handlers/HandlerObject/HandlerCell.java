package ManagerSystem.Handlers.HandlerObject;

import DataSystem.Data.Position;
import DataSystem.ID.IDDirection;
import DataSystem.Type.TypeCellPart;
import MainSystem.Abstract.AbstractObject;
import MainSystem.Main.Main;
import MainSystem.Object.Cell;
import MainSystem.Object.CellType.CellDuplicator;
import MainSystem.Object.CellType.CellTeleport;
import MainSystem.Object.CellType.CellNoEntry;
import MainSystem.Object.CellType.CellSidePortal;
import MainSystem.Object.CellType.CellSpecialPortal;
import Settings.SettingsCell;
import java.util.ArrayList;

public class HandlerCell{

    public static Main main;
    
    public static ArrayList<Cell> cells = new ArrayList();

    public static void add(AbstractObject o){
        if(o instanceof Cell c){
            cells.add(c);
        }
    }

    public static void remove(AbstractObject o){
        if(o instanceof Cell c){
            cells.remove(c);
        }
    }

    public static void set(AbstractObject oldO, AbstractObject newO){
        if(oldO instanceof Cell oldC){
            if(newO instanceof Cell newC){
                cells.set(cells.indexOf(oldC), newC);
            }
        }
    }

    public static void swap(Cell sC, Cell tC){
        Position sDP = sC.getPosition();
        
        sC.setNewPosition(tC.getPosition());
        tC.setNewPosition(sDP);
    }
    
    public static boolean check(AbstractObject o){
        return o instanceof Cell;
    }

    public static Cell[] getArray(){
        return cells.toArray(Cell[]::new);
    }

    public static void Reset(){
        cells.forEach((c) -> {
            c.reset();
        });
    }

// -----------------------------------------------------------------------------------------------------------
    
    public static void ResetState(){
        for(Cell c : getArray()){
            c.resetState();
        }
    }
    
    public static void SaveState(){
        for(Cell c : getArray()){
            c.saveState();
        }
    }

    public static void UndoState(){
        for(Cell c : getArray()){
            c.undoState();
        }
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public static Cell[] getCellTeleport(int teleportCellPart){
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
    
    public static Cell[] getCellDuplicator(int duplicatorCellPart){
        Cell[] output = new Cell[]{null, null};
        int i = 0;
        for(Cell c : getArray()){
            if(c instanceof CellDuplicator cm){
                if(cm.isDuplicatorPart(duplicatorCellPart)){
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
    
// -----------------------------------------------------------------------------------------------------------
    
    public static Cell getCell(Cell c, IDDirection d){
        return getCell(c, d, 1);
    }

    public static Cell getCell(Cell c, IDDirection d, int distance){
        return switch(d){
            case U ->
                getCell(c.rx, c.ry - distance);
            case D ->
                getCell(c.rx, c.ry + distance);
            case L ->
                getCell(c.rx - distance, c.ry);
            case R ->
                getCell(c.rx + distance, c.ry);
            default ->
                null;
        };
    }

    public static Cell getCell(int rx, int ry){
        if(rx != -1 && rx != SettingsCell.xCell && ry != -1 && ry != SettingsCell.yCell){
            for(Cell c : getArray()){
                if(c.rx == rx && c.ry == ry && !c.isCellPart(TypeCellPart.space)){
                    return c;
                }
            }
        }

        return null;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public static Cell getAllCell(Cell c, IDDirection d, int distance){
        return switch(d){
            case U ->
                getAllCell(c.rx, c.ry - distance);
            case D ->
                getAllCell(c.rx, c.ry + distance);
            case L ->
                getAllCell(c.rx - distance, c.ry);
            case R ->
                getAllCell(c.rx + distance, c.ry);
            default ->
                null;
        };
    }

    public static Cell getAllCell(int rx, int ry){
        if(rx != -1 && rx != SettingsCell.xCell && ry != -1 && ry != SettingsCell.yCell){
            for(Cell c : getArray()){
                if(c.rx == rx && c.ry == ry){
                    return c;
                }
            }
        }

        return null;
    }

// -----------------------------------------------------------------------------------------------------------

    public static void updateCells(){
        
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
                    if(sC.getManagerSideCell().haveSide(d)) continue;
                    
                    if(sC.rx == 0){
                        Cell tC = getCell(SettingsCell.xCell - 1, sC.ry);
                        if(tC == null){
                            continue;
                        }

                        sC.getManagerSideCell().setSide(tC, IDDirection.L);
                        tC.getManagerSideCell().setSide(sC, IDDirection.R);
                        ((CellSidePortal)sC).highlightSidePortal[IDDirection.L.index] = true;
                        ((CellSidePortal)tC).highlightSidePortal[IDDirection.R.index] = true;
                    }

                    if(sC.rx == SettingsCell.xCell - 1){
                        Cell tC = getCell(0, sC.ry);
                        if(tC == null){
                            continue;
                        }

                        sC.getManagerSideCell().setSide(tC, IDDirection.R);
                        tC.getManagerSideCell().setSide(sC, IDDirection.L);
                        ((CellSidePortal)sC).highlightSidePortal[IDDirection.R.index] = true;
                        ((CellSidePortal)tC).highlightSidePortal[IDDirection.L.index] = true;
                    }

                    if(sC.ry == 0){
                        Cell tC = getCell(sC.rx, SettingsCell.yCell - 1);
                        if(tC == null){
                            continue;
                        }

                        sC.getManagerSideCell().setSide(tC, IDDirection.U);
                        tC.getManagerSideCell().setSide(sC, IDDirection.D);
                        ((CellSidePortal)sC).highlightSidePortal[IDDirection.U.index] = true;
                        ((CellSidePortal)tC).highlightSidePortal[IDDirection.D.index] = true;
                    }

                    if(sC.ry == SettingsCell.yCell - 1){
                        Cell tC = getCell(sC.rx, 0);
                        if(tC != null){
                            sC.getManagerSideCell().setSide(tC, IDDirection.D);
                            tC.getManagerSideCell().setSide(sC, IDDirection.U);
                            ((CellSidePortal)sC).highlightSidePortal[IDDirection.D.index] = true;
                            ((CellSidePortal)tC).highlightSidePortal[IDDirection.U.index] = true;
                        }
                    }
                }
            }
        }

        for(Cell sC : getArray()){
            sC.updateMaxAtoms();
            if(sC instanceof CellTeleport cm){
                cm.updateTeleport();
            }
            if(sC instanceof CellDuplicator cm){
                cm.updateDuplicator();
            }
        }
        
        resetFocused();
    }
    
    public static void resetFocused(){
        cells.forEach((c) -> {
            c.resetFocused();
        });
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public static void afterTurn(){
        cells.forEach((c) -> {
            c.tickTurn();
        });
    }
    
}
