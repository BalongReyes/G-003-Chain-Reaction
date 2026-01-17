package DataSystem.Data;

import DataSystem.State.StatePlayerAtoms;
import MainSystem.Main.Main;
import java.awt.Color;
import java.util.Stack;

public enum Player{
    
    Dead(Color.gray),
    Player1(Color.cyan, "Sedian"),
    Player2(Color.magenta, "Jojo"),
    Player3(Color.yellow, "Vhon");
//    Player4(Color.green, "Jadey");

// ===========================================================================================================
    
    public static Main main;
    
    public Color color;
    public String name = null;
    
    public boolean hintEnabled = false;
    
// Constructor ===============================================================================================
    
    private Player(Color color, String name){
        this.color = color;
        this.name = name;
        
        if(color == Color.red){
            throw new RuntimeException("Red is not a valid player color");
        }
    }
    
    private Player(Color color){
        this(color, null);
    }
    
// Main Methods ==============================================================================================
    
    public int atoms = -1;
    
    public void incrementAtoms(){
        if(this.atoms < 0) this.atoms = 0;
        this.atoms++;
    }

    public void decrementAtoms(){
        this.atoms--;
        if(this.atoms < 0) this.atoms = 0;
    }

    public Atom createAtom(){
        return new Atom(this);
    }
    
// Static Methods ============================================================================================
    
    public static Player GetPlayer(int index){
        if(index == -1) return null;
        for(Player dP : values()) if(dP.ordinal() == index){
            return dP;
        }
        return null;
    }

    public static Player GetNextPlayer(Player currentPlayer, boolean forced){
        int index = currentPlayer.ordinal();
        int count = 0;

        Player dP;
        do{
            count++;
            index++;
            if(index >= values().length) index = 0;

            dP = values()[index];
            if(dP != Dead){
                if(count > values().length){
                    dP = currentPlayer;
                    break;
                }
                if(forced) break;
            }
        }while(dP.atoms == 0 || dP == Dead);

        return dP;
    }

    public static void Reset(){
        for(Player dP : values()) dP.atoms = -1;
    }

    public static int CountAlive(){
        int output = 0;
        for(Player dP : values()){
            if(dP == Dead) continue;
            if(dP.atoms != 0 || dP.atoms == -1) output++;
        }
        return output;
    }
    
// State =====================================================================================================
    
    private Stack<StatePlayerAtoms> playerAtoms = new Stack();
    
    public static void ResetState(){
        for(Player dP : values()){
            dP.playerAtoms.clear();
        }
    }
    
    public static void SaveState(){
        for(Player dP : values()){
            dP.playerAtoms.add(new StatePlayerAtoms(dP.atoms));
            if(dP.playerAtoms.size() > main.undoLimit){
                dP.playerAtoms.remove(0);
            }
        }
    }

    public static void UndoState(){
        for(Player dP : values()){
            if(dP.playerAtoms.empty()) continue;
            StatePlayerAtoms sA = dP.playerAtoms.pop();
            dP.atoms = sA.getState();
        }
    }

}
