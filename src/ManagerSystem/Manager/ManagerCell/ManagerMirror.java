package ManagerSystem.Manager.ManagerCell;

import MainSystem.Object.Cell;
import java.util.ArrayList;
import java.util.List;

public class ManagerMirror {

    private Cell parent;
    private int mirrorCellPart;
    private List<Cell> mirrorCells = new ArrayList<>();

    public ManagerMirror(Cell parent) {
        this.parent = parent;
    }

    public void setMirrorCellPart(int duplicateCellPart) {
        this.mirrorCellPart = duplicateCellPart;
    }

    public int getMirrorCellPart() {
        return mirrorCellPart;
    }

    public void addMirrorCell(Cell mirrorCell) {
        if (!this.mirrorCells.contains(mirrorCell)) {
            this.mirrorCells.add(mirrorCell);
        }
    }

    public List<Cell> getMirrorCells() {
        return mirrorCells;
    }
}