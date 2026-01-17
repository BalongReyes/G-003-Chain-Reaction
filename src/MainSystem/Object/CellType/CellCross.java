
package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;

public class CellCross extends Cell{

    public CellCross(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.cross;
    }

}
