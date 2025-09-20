package ManagerSystem.Manager.ManagerCell;

import MainSystem.Object.Cell;
import java.util.ArrayList;
import java.util.List;

public class ManagerDuplicator {

    private Cell parent;
    private int duplicateCellPart;
    private List<Cell> duplicatorCells = new ArrayList<>();

    public ManagerDuplicator(Cell parent) {
        this.parent = parent;
    }

    public void setDuplicateCellPart(int duplicateCellPart) {
        this.duplicateCellPart = duplicateCellPart;
    }

    public int getDuplicatorCellPart() {
        return duplicateCellPart;
    }

    public void addDuplicatorCell(Cell duplicatorCell) {
        if (!this.duplicatorCells.contains(duplicatorCell)) {
            this.duplicatorCells.add(duplicatorCell);
        }
    }

    public List<Cell> getDuplicatorCells() {
        return duplicatorCells;
    }
}