package ManagerSystem.Manager.ManagerCell;

import DataSystem.ID.IDDirection;
import MainSystem.Object.Cell;
import java.util.Arrays;

public class ManagerSideCell{

    public Cell parent;
    public Cell[] sideCells = new Cell[]{null, null, null, null};

    public ManagerSideCell(Cell parent){
        this.parent = parent;
    }

    public int countSide(){
        int output = 0;
        
        for(IDDirection d : IDDirection.values()) if(this.sideCells[d.index] != null){
            output++;
        }

        return output;
    }

    public Cell[] getArray(){
        return this.sideCells;
    }

    public Cell getSide(IDDirection d){
        return this.sideCells[d.index];
    }

    public void setSide(Cell c, IDDirection d){
        this.sideCells[d.index] = c;
    }

    public void reset(){
        Arrays.fill(this.sideCells, null);
    }

    public boolean haveSide(IDDirection d){
        return this.sideCells[d.index] != null;
    }

    public int getDistance(Cell c, IDDirection d){
        int output;
        switch(d){
            case U ->
                output = this.parent.ry - c.ry;
            case D ->
                output = c.ry - this.parent.ry;
            case L ->
                output = this.parent.rx - c.rx;
            case R ->
                output = c.rx - this.parent.rx;
            default ->
                throw new IncompatibleClassChangeError();
        }

        return output;
    }

// ===========================================================================================================
    
    public boolean haveU(){
        return this.sideCells[0] != null;
    }

    public boolean haveD(){
        return this.sideCells[1] != null;
    }

    public boolean haveL(){
        return this.sideCells[2] != null;
    }

    public boolean haveR(){
        return this.sideCells[3] != null;
    }

    public boolean haveUL(){
        return this.haveU() && this.haveL();
    }

    public boolean haveUR(){
        return this.haveU() && this.haveR();
    }

    public boolean haveDL(){
        return this.haveD() && this.haveL();
    }

    public boolean haveDR(){
        return this.haveD() && this.haveR();
    }

    public boolean haveH(){
        return this.haveL() && this.haveR();
    }

    public boolean haveV(){
        return this.haveU() && this.haveD();
    }
}
