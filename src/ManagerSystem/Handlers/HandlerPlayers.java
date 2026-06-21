
package ManagerSystem.Handlers;

import DataSystem.Data.Player;
import DataSystem.State.StatePlayer;
import MainSystem.Main.Main;
import ManagerSystem.Handlers.HandlerObject.HandlerCell;
import java.awt.Color;
import java.util.Stack;

public class HandlerPlayers{

    public Player currentPlayer = Player.Player1;
    
// ===========================================================================================================
    
    public int movesPerTurn = 1;
    private int playerMoves = 0;

    public int getPlayerMoves(){
        return playerMoves;
    }
    
// ===========================================================================================================
    
    public boolean checkPlayer(Player player){
        return currentPlayer == player;
    }
    
    public Player getPlayer(){
        return currentPlayer;
    }
    
    public Color getPlayerColor(){
        return currentPlayer.color;
    }
    
// ===========================================================================================================
    
    private boolean nextPlayer;
    private boolean nextPlayerForced;
    
    public void nextPlayer(){
        nextPlayer = true;
    }
    
    public void nextPlayerForced(){
        nextPlayerForced = true;
    }
    
   // --------------------------------------------------------------------------------------------------------
    
    private int botTickDelay = 0;
    
    public void tick(Main main){
        if((nextPlayer || nextPlayerForced) && !main.isSimulating()){
            main.handlerCell.tickTurn();
            
            playerMoves++;
            if(playerMoves >= movesPerTurn || nextPlayerForced){
                currentPlayer = Player.GetNextPlayer(currentPlayer, nextPlayerForced);
                playerMoves = 0;
            }

            nextPlayer = false;
            nextPlayerForced = false;
        }

        if(!main.isSimulating() && !nextPlayer && !nextPlayerForced && currentPlayer.isBot){
            if (!isBotCalculating) {
                botTickDelay++;
                if(botTickDelay > 90){
                    botTickDelay = 0;
                    isBotCalculating = true;
                    
                    final Player botPlayer = currentPlayer;
                    new Thread(() -> {
                        long calculationStartTime = System.currentTimeMillis();
                        try {
                            MainSystem.Object.Cell bestCell;
                            switch (botPlayer.botType) {
                                case CLAUDE:
                                    bestCell = BotLogicClaude.calculateBestMove(botPlayer, main);
                                    break;
                                case GEMINI:
                                default:
                                    bestCell = BotLogicGemini.calculateBestMove(botPlayer, main);
                                    break;
                            }
                            botMoveResult = bestCell;
                        } catch (Exception e) {
                            e.printStackTrace();
                            botMoveResult = null;
                        } finally {
                            long elapsedTime = System.currentTimeMillis() - calculationStartTime;
                            long remainingDelay = 500 - elapsedTime;
                            if (remainingDelay > 0) {
                                try {
                                    Thread.sleep(remainingDelay);
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            botFinishedCalculating = true;
                        }
                    }).start();
                }
            } else if (botFinishedCalculating) {
                isBotCalculating = false;
                botFinishedCalculating = false;
                if(botMoveResult != null){
                    // Keep both classes' indicators in sync for the rendering system
                    BotLogicGemini.lastBotMove        = botMoveResult;
                    BotLogicGemini.lastBotPlayer      = currentPlayer;
                    BotLogicClaude.lastBotMove   = botMoveResult;
                    BotLogicClaude.lastBotPlayer = currentPlayer;
                    botMoveResult.clickLeftConfirmed();
                }else{
                    nextPlayerForced();
                }
                botMoveResult = null;
            }
        }else{
            botTickDelay = 0;
            isBotCalculating = false;
            botFinishedCalculating = false;
        }
    }

// ===========================================================================================================
    
    public void Reset(){
        currentPlayer = Player.Player1;
    }
    
// State =====================================================================================================
    
    private Stack<StatePlayer> playerState = new Stack<>();
    

    private boolean isBotCalculating = false;
    private boolean botFinishedCalculating = false;
    private MainSystem.Object.Cell botMoveResult = null;

// ...........................................................................................................
    
    public void ResetState(){
        Player.ResetState();
        playerState.clear();
    }
    
    public void SaveStates(int undoLimit){
        Player.SaveState(undoLimit);
        playerState.add(new StatePlayer(currentPlayer, playerMoves));
        if(playerState.size() > undoLimit){
            playerState.remove(0);
        }
    }
    
    public void UndoStates(){
        Player.UndoState();
        if(!playerState.isEmpty()){
            StatePlayer sP = playerState.pop();
            currentPlayer = sP.getCurrentPlayer();
            playerMoves = sP.getPlayerMoves();
        }
    }
    
}
