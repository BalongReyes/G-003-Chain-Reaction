
package DataSystem.State;

public class StatePlayerAtoms{

    private int atomCount = 0;
    
    public StatePlayerAtoms(int atomCount){
        this.atomCount = atomCount;
    }

    public int getState(){
        return atomCount;
    }

}
