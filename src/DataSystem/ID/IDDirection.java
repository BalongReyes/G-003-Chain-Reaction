
package DataSystem.ID;

import MainSystem.Methods.MethodsNumber;
import java.util.Random;

public enum IDDirection{
    
    U(),
    D(),
    L(),
    R(),
    UL(),
    UR(),
    DL(),
    DR();

    public int index;
    
// Constructor ===============================================================================================

    private IDDirection(){
        index = ordinal();
    }
    
// Main Methods ==============================================================================================

    public static IDDirection getValue(int index){
        if(index >= values().length) return null;
        return values()[index];
    }
    
    public IDDirection getInverted(){
        return switch(ordinal()){
            case 0 -> D;
            case 1 -> U;
            case 2 -> R;
            case 3 -> L;
            case 4 -> DR;
            case 5 -> DL;
            case 6 -> UR;
            case 7 -> UL;
            default -> null;
        };
    }
    
    public boolean isDiagonal() {
        return this == UL || this == UR || this == DL || this == DR;
    }
    
// Static Methods ============================================================================================
    
    public static IDDirection[] getOrthogonals() {
        return new IDDirection[]{U, D, L, R};
    }

    public static IDDirection getRandom(){
        return values()[MethodsNumber.getRandomNumber(0, 3)];
    }
    
    public static IDDirection[] getRandomArray(){
        IDDirection randomSides[] = new IDDirection[]{null, null, null, null, null, null, null, null};
        
        int[] n = {0, 1, 2, 3, 4, 5, 6, 7};
        Random r = new Random();
        for(int i : n){
            int rndI = r.nextInt(n.length);
            int temp = n[rndI];
            n[rndI] = n[i];
            n[i] = temp;
        }
        for(int i = 0; i < n.length; i++){
            randomSides[i] = IDDirection.getValue(n[i]);
        }
        return randomSides;
    }
}
