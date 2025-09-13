package DataSystem.Maps;

public abstract class AbstractMap{

    public int[][] map;

    public int[][] getMap(){
        return this.map;
    }

// -----------------------------------------------------------------------------------------------------------
    
    public int[][] teleportCells;

    public int[][] getTeleportCells(){
        return teleportCells;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public int[] mapSize;
    
    public int[] getMapSize(){
        return this.mapSize;
    }

    public int getMaxMapSize(){
        return this.mapSize[0] > this.mapSize[1] ? this.mapSize[0] : this.mapSize[1];
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public int[][] noEntryCells;

    public int[][] getNoEntryCells(){
        return noEntryCells;
    }
    
// -----------------------------------------------------------------------------------------------------------
    
    public int[][] moveable;

    public int[][] getMoveable(){
        return moveable;
    }
    
}
