
package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;

public class CellSpace extends Cell{

    public CellSpace(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.space;
    }

}
