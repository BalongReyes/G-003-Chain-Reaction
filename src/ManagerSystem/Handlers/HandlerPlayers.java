
package ManagerSystem.Handlers;

import DataSystem.Data.Player;
import DataSystem.State.StatePlayer;
import MainSystem.Main.Main;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import java.awt.Color;
import java.util.Stack;

public class HandlerPlayers{

    public static Main main;
    public static Player currentPlayer = Player.Player1;
    
// ===========================================================================================================
    
    public static int movesPerTurn = 1;
    private static int playerMoves = 0;

    public static int getPlayerMoves(){
        return playerMoves;
    }
    
// ===========================================================================================================
    
    public static boolean checkPlayer(Player player){
        return currentPlayer == player;
    }
    
    public static Player getPlayer(){
        return currentPlayer;
    }
    
    public static Color getPlayerColor(){
        return currentPlayer.color;
    }
    
// ===========================================================================================================
    
    private static boolean nextPlayer;
    private static boolean nextPlayerForced;
    
    public static void nextPlayer(){
        nextPlayer = true;
    }
    
    public static void nextPlayerForced(){
        nextPlayerForced = true;
    }
    
   // --------------------------------------------------------------------------------------------------------
    
    public static void tick(){
        if((nextPlayer || nextPlayerForced) && !main.isSimulating()){
            playerMoves++;
            if(playerMoves >= movesPerTurn || nextPlayerForced){
                currentPlayer = Player.GetNextPlayer(currentPlayer, nextPlayerForced);
                playerMoves = 0;
            }

            nextPlayer = false;
            nextPlayerForced = false;
            HandlerCell.afterTurn();
        }
    }

// ===========================================================================================================
    
    public static void Reset(){
        currentPlayer = Player.Player1;
    }
    
// State =====================================================================================================
    
    private static Stack<StatePlayer> playerState = new Stack();
    
    public static void ResetState(){
        Player.ResetState();
        playerState.clear();
    }
    
    public static void SaveStates(){
        Player.SaveState();
        playerState.add(new StatePlayer(currentPlayer, playerMoves));
        if(playerState.size() > main.undoLimit){
            playerState.remove(0);
        }
    }
    
    public static void UndoStates(){
        Player.UndoState();
        if(!playerState.isEmpty()){
            StatePlayer sP = playerState.pop();
            currentPlayer = sP.getCurrentPlayer();
            playerMoves = sP.getPlayerMoves();
        }
    }
    
}
