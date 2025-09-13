
package DataSystem.State;

import DataSystem.Data.Player;

public class StatePlayer{

    private Player currentPlayer;
    private int playerMoves;
    
    public StatePlayer(Player currentPlayer, int playerMoves){
        this.currentPlayer = currentPlayer;
        this.playerMoves = playerMoves;
    }

    public Player getCurrentPlayer(){
        return currentPlayer;
    }

    public int getPlayerMoves(){
        return playerMoves;
    }

}
