
package MainSystem.Object.CellType;

import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;

public class CellNormal extends Cell{

    public CellNormal(double x, double y, int rx, int ry){
        super(x, y, rx, ry);
        cellPart = TypeCellPart.normal;
    }

}
