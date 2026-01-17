package DataSystem.Type;

import MainSystem.Object.Cell;
import MainSystem.Object.CellType.CellCannon;
import MainSystem.Object.CellType.CellCross;
import MainSystem.Object.CellType.CellDuplicator;
import MainSystem.Object.CellType.CellTeleport;
import MainSystem.Object.CellType.CellMoveable;
import MainSystem.Object.CellType.CellNoEntry;
import MainSystem.Object.CellType.CellNormal;
import MainSystem.Object.CellType.CellPortal;
import MainSystem.Object.CellType.CellSidePortal;
import MainSystem.Object.CellType.CellSpace;
import MainSystem.Object.CellType.CellSpecial;
import MainSystem.Object.CellType.CellSpecialPortal;
import MainSystem.Object.CellType.CellTerritory;

public enum TypeCellPart{

    space(),            // 0
    normal(),           // 1
    portal(),           // 2
    special(),          // 3
    specialPortal(),    // 4
    sidePortal(),       // 5
    teleport(),         // 6
    noEntry(),          // 7
    moveable(),         // 8
    territory(),        // 9
    duplicator(),       // 10
    cannon(),           // 11
    cross();            // 12

// ===========================================================================================================
    
    public static TypeCellPart getTypeCellPart(int mapbit){
        for(TypeCellPart tC : values()) if(tC.ordinal() == mapbit){
            return tC;
        }
        return space;
    }
    
    public static Cell createCell(TypeCellPart mapBit, double x, double y, int rx, int ry){
        switch(mapBit){
            case space -> {
                return new CellSpace(x, y, rx, ry);
            }
            case normal -> {
                return new CellNormal(x, y, rx, ry);
            }
            case portal -> {
                return new CellPortal(x, y, rx, ry);
            }
            case special -> {
                return new CellSpecial(x, y, rx, ry);
            }
            case specialPortal -> {
                return new CellSpecialPortal(x, y, rx, ry);
            }
            case sidePortal -> {
                return new CellSidePortal(x, y, rx, ry);
            }
            case teleport -> {
                return new CellTeleport(x, y, rx, ry);
            }
            case noEntry -> {
                return new CellNoEntry(x, y, rx, ry);
            }
            case moveable -> {
                return new CellMoveable(x, y, rx, ry);
            }
            case territory -> {
                return new CellTerritory(x, y, rx, ry);
            }
            case duplicator -> {
                return new CellDuplicator(x, y, rx, ry);
            }
            case cannon -> {
                return new CellCannon(x, y, rx, ry);
            }
            case cross -> {
                return new CellCross(x, y, rx, ry);
            }
        }
        return null;
    }

}
