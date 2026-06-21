package ManagerSystem.Handlers;

import DataSystem.Data.Player;
import DataSystem.ID.IDDirection;
import MainSystem.Main.Main;
import MainSystem.Object.Cell;

import java.util.ArrayList;
import java.util.List;

public class BotLogic {

    public static Cell calculateBestMove(Player botPlayer, Main main) {
        Cell bestMove = null;
        double bestScore = -Double.MAX_VALUE;
        List<Cell> bestMoves = new ArrayList<>();

        for (Cell c : main.handlerCell.getArray()) {
            if (!c.isCellSpace() && c.validateClickAddAtom(botPlayer)) {
                double score = evaluateMove(c, botPlayer, main);
                if (score > bestScore) {
                    bestScore = score;
                    bestMoves.clear();
                    bestMoves.add(c);
                } else if (score == bestScore) {
                    bestMoves.add(c);
                }
            }
        }

        if (!bestMoves.isEmpty()) {
            return bestMoves.get((int) (Math.random() * bestMoves.size()));
        }

        return null;
    }

    private static double evaluateMove(Cell move, Player botPlayer, Main main) {
        double score = 0;
        int maxAtoms = move.getManagerAtoms().getMaxAtoms();
        int currentAtoms = move.getManagerAtoms().atomsSize();

        boolean willExplode = (currentAtoms + 1 >= maxAtoms);

        if (willExplode) {
            // Highly value explosions
            score += 1000;
        } else {
            boolean nearEnemyCritical = false;
            for (IDDirection d : IDDirection.values()) {
                Cell adj = move.getManagerSideCell().getSide(d);
                if (adj != null && !adj.isCellSpace()) {
                    Player adjPlayer = adj.getManagerAtoms().getPlayer();
                    if (adjPlayer != null && adjPlayer != botPlayer && adjPlayer != Player.Dead) {
                        if (adj.getManagerAtoms().atomsSize() >= adj.getManagerAtoms().getMaxAtoms() - 1) {
                            nearEnemyCritical = true;
                            break;
                        }
                    }
                }
            }

            if (nearEnemyCritical) {
                // Penalize heavily if placing an atom here feeds a critical enemy cell
                score -= 500;
            } else {
                // Prefer corners and edges
                score += (5 - maxAtoms) * 10;
            }

            // Becoming critical is good because it threatens enemies
            if (currentAtoms + 1 == maxAtoms - 1) {
                score += 50;
            }
        }

        // Add a slight randomization to make the bot less predictable
        score += Math.random() * 5;
        
        return score;
    }
}
