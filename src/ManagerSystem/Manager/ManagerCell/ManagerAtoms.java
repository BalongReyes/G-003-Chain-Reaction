package ManagerSystem.Manager.ManagerCell;

import DataSystem.State.StateCell;
import DataSystem.Data.Player;
import DataSystem.Type.TypeCellPart;
import static DataSystem.Type.TypeCellPart.special;
import MainSystem.Main.Console;
import MainSystem.Object.Cell;
import java.awt.Color;
import java.util.ArrayList;

public class ManagerAtoms{

    private ArrayList<Player> differentAtoms = new ArrayList();
    public Cell parent;
    
// Constructor ===============================================================================================

    public ManagerAtoms(Cell parent){
        this.parent = parent;
    }
    
// ===========================================================================================================

    public int differentAtomsSize(){
        return differentAtoms.size();
    }
    
    public boolean checkAtoms(Player player){
        try{
            for(Player a : getArray()){
                if(a == player) return true;
            }
        }catch(Exception e){
            Console.out("\nError: " + e.getMessage(), true);
        }
        return false;
    }

    public Player[] getDifferentAtoms(){
        return differentAtoms.toArray(Player[]::new);
    }

    public Color getColor(int i){
        try{
            return atoms.get(i).color;
        }catch(Exception e){
            return null;
        }
    }
    
// ===========================================================================================================
    
    public void reset(){
        for(Player oldAtoms : getArray()){
            remove(oldAtoms);
        }
    }
    
// ===========================================================================================================

    private int maxAtoms = 0;
    
    public void setMaxAtoms(int maxAtoms){
        this.maxAtoms = maxAtoms;
    }

    public int getMaxAtoms(){
        return this.maxAtoms;
    }

    public boolean isMaxAtoms(){
        return this.atomsSize() >= this.maxAtoms;
    }

    public void updateMaxAtoms(TypeCellPart cellPart, ManagerSideCell managerSideCell){
        this.maxAtoms = 0;
        if(cellPart != TypeCellPart.space) switch(cellPart){
            case special -> {
                setMaxAtoms(1);
            }
            case mirror -> {
                setMaxAtoms(4);
            }
            default -> {
                switch(managerSideCell.countSide()){
                    case 2 -> {
                        if(!managerSideCell.haveV() && !managerSideCell.haveH()){
                            setMaxAtoms(1);
                        }else{
                            setMaxAtoms(2);
                        }
                    }
                    case 3 -> {
                        setMaxAtoms(2);
                    }
                    case 4 -> {
                        setMaxAtoms(3);
                    }
                    default ->{
                        setMaxAtoms(1);
                    }
                }
            }
        }
    }
    
// State -----------------------------------------------------------------------------------------------------
    
    public void setStateCell(StateCell sC){
        reset();
        add(sC.getDataPlayerArray());
    }
    
// Atoms =====================================================================================================
    
    private ArrayList<Player> atoms = new ArrayList();
    
    public boolean isEmpty(){
        return atoms.isEmpty();
    }
    
    public boolean isEmptyOrDead(){
        return atoms.isEmpty() || checkAtoms(Player.Dead);
    }

    public int atomsSize(){
        return atoms.size();
    }
    
    public Player[] getArray(){
        return atoms.toArray(Player[]::new);
    }
    
// ===========================================================================================================
    
    public void replaceAllThenAdd(Player player){
        replaceAll(player);
        add(player);
    }

    public void replaceAll(Player player){
        for(Player oldAtoms : getArray()){
            remove(oldAtoms);
            add(player);
        }
    }
    
    private void remove(Player player){
        if(checkAtoms(player)){
            atoms.remove(player);
            player.decrementAtoms();
            
            if(!checkAtoms(player)){
                differentAtoms.remove(player);
            }
        }
    }

    public void add(Player[] players){
        for(Player a : players) add(a);
    }

    public void add(Player player){
        atoms.add(player);
        player.incrementAtoms();
        
        parent.atomsDistance = 0;
        parent.setSimulateAdd(true);
        
        boolean exist = false;
        for(Player a : getDifferentAtoms()){
            if(a == player){
                exist = true;
                break;
            }
        } 
        if(!exist){
            differentAtoms.add(player);
        }
    }
    
}
