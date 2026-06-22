package ManagerSystem.Handlers;

import DataSystem.Data.Player;
import DataSystem.ID.IDDirection;
import MainSystem.Main.Main;
import MainSystem.Object.Cell;
import MainSystem.Object.CellType.CellCannon;
import MainSystem.Object.CellType.CellTeleport;
import MainSystem.Object.CellType.CellBlackHole;
import MainSystem.Object.CellType.CellShield;
import Settings.SettingsCell;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BotLogicGemini {

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public static Difficulty currentDifficulty = Difficulty.HARD;

    public static Cell lastBotMove = null;
    public static Player lastBotPlayer = null;

    public static Cell calculateBestMove(Player botPlayer, Main main) {
        switch (currentDifficulty) {
            case EASY:
                return calculateEasyMove(botPlayer, main);
            case HARD:
                return calculateHardMove(botPlayer, main);
            case MEDIUM:
            default:
                return calculateMediumMove(botPlayer, main);
        }
    }

    private static Cell calculateEasyMove(Player botPlayer, Main main) {
        VirtualBoard board = new VirtualBoard(main);
        List<Cell> moves = board.getValidMoves(botPlayer);
        if (!moves.isEmpty()) {
            return moves.get((int) (Math.random() * moves.size()));
        }
        return null;
    }

    private static Cell calculateHardMove(Player botPlayer, Main main) {
        return runIterativeDeepening(botPlayer, main, 7, 1000L, true); // Depth 7, 1000ms, Enable Aspiration
    }

    private static Cell calculateMediumMove(Player botPlayer, Main main) {
        return runIterativeDeepening(botPlayer, main, 3, 400L, false); // Depth 3, 400ms
    }
    
    // Special exception to abort search when time limit is reached
    private static class TimeOutException extends Exception {}
    
    private static Cell runIterativeDeepening(Player botPlayer, Main main, int maxDepth, long timeLimitMs, boolean useAspiration) {
        VirtualBoard rootBoard = new VirtualBoard(main);
        Player enemyPlayer = Player.GetNextPlayer(botPlayer, false);

        // Transposition table
        TranspositionEntry[] ttable = new TranspositionEntry[131072];

        long startTime = System.currentTimeMillis();
        List<Cell> bestMoves = new ArrayList<>();

        // Aggressive Opening Gambit
        if (isOpeningPhase(rootBoard, botPlayer)) {
            Cell openingMove = aggressiveOpeningGambit(rootBoard, botPlayer);
            if (openingMove != null) {
                return openingMove;
            }
        }

        double prevBestScore = 0;
        long rootHash = rootBoard.computeHash(botPlayer);

        // Iterative Deepening
        for (int depth = 1; depth <= maxDepth; depth++) { 
            List<Cell> currentDepthBestMoves = new ArrayList<>();
            double bestScore = -Double.MAX_VALUE;
            double alpha, beta;
            
            if (useAspiration && depth > 2) {
                double window = 300;
                alpha = prevBestScore - window;
                beta = prevBestScore + window;
            } else {
                alpha = -Double.MAX_VALUE;
                beta = Double.MAX_VALUE;
            }

            int attempts = 0;
            boolean searchSuccess = false;
            
            while (attempts < 2) {
                currentDepthBestMoves.clear();
                bestScore = -Double.MAX_VALUE;
                int bestRootMoveIdx = -1;
                
                try {
                    int[] validMoves = rootBoard.getValidMoveIndices(botPlayer);
                    
                    // PV / Hash Move Ordering for Root
                    int rootIndex = (int)(rootHash & 131071);
                    TranspositionEntry rootCached = ttable[rootIndex];
                    int rootBestMoveIdxTemp = (rootCached != null && rootCached.hash == rootHash) ? rootCached.bestMoveIdx : -1;
                    if (rootBestMoveIdxTemp != -1) {
                        for (int i = 0; i < validMoves.length; i++) {
                            if (validMoves[i] == rootBestMoveIdxTemp) {
                                for (int j = i; j > 0; j--) {
                                    validMoves[j] = validMoves[j - 1];
                                }
                                validMoves[0] = rootBestMoveIdxTemp;
                                break;
                            }
                        }
                    }

                    for (int moveIdx : validMoves) {
                        Cell c = rootBoard.indexToCell[moveIdx];
                        VirtualBoard nextBoard = new VirtualBoard(rootBoard);
                        double scoreDelta = nextBoard.applyMoveIdx(moveIdx, botPlayer);
                        
                        if (scoreDelta >= WIN_SCORE) return c; // Instant win
                        
                        double score;
                        if (scoreDelta <= LOSE_SCORE) {
                            score = LOSE_SCORE;
                        } else {
                            // NegaMax with Alpha-Beta Pruning
                            score = scoreDelta - negaMax(nextBoard, enemyPlayer, botPlayer, depth - 1, -beta, -alpha, startTime, timeLimitMs, ttable);
                        }
                        
                        if (score > bestScore) {
                            bestScore = score;
                            currentDepthBestMoves.clear();
                            currentDepthBestMoves.add(c);
                            bestRootMoveIdx = moveIdx;
                        } else if (score == bestScore) {
                            currentDepthBestMoves.add(c);
                            if (bestRootMoveIdx == -1) {
                                bestRootMoveIdx = moveIdx;
                            }
                        }
                        
                        if (score > alpha) alpha = score;
                        if (useAspiration && alpha >= beta) break; // failed high
                    }
                    
                    if (useAspiration && depth > 2 && attempts == 0) {
                        if (bestScore <= alpha) {
                            // Failed low: expand alpha window and re-search
                            alpha = -Double.MAX_VALUE;
                            attempts++;
                            continue;
                        } else if (bestScore >= beta) {
                            // Failed high: expand beta window and re-search
                            beta = Double.MAX_VALUE;
                            attempts++;
                            continue;
                        }
                    }
                    
                    searchSuccess = true;
                    
                    // If we completed this depth without timing out, save the results!
                    if (!currentDepthBestMoves.isEmpty() && bestRootMoveIdx != -1) {
                        bestMoves.clear();
                        bestMoves.addAll(currentDepthBestMoves);
                        prevBestScore = bestScore;
                        
                        byte ttFlag = TT_EXACT;
                        int rootIndexStore = (int)(rootHash & 131071);
                        TranspositionEntry cached = ttable[rootIndexStore];
                        if (cached == null || depth >= cached.depth) {
                            ttable[rootIndexStore] = new TranspositionEntry(rootHash, bestScore, depth, ttFlag, bestRootMoveIdx);
                        }
                    }
                    break;
                    
                } catch (TimeOutException e) {
                    break;
                }
            }
            if (!searchSuccess) {
                // If search/re-search timed out, stop deepening
                break;
            }
            if (bestScore >= WIN_SCORE / 2) {
                break;
            }
        }

        if (prevBestScore <= LOSE_SCORE / 2) {
            logState(rootBoard, botPlayer, prevBestScore, "FORCED LOSS DETECTED (Score: " + prevBestScore + ")");
        } else {
            // Uncomment to log every move:
            // logState(rootBoard, botPlayer, prevBestScore, "MOVE CHOSEN (Score: " + prevBestScore + ")");
        }

        if (!bestMoves.isEmpty()) {
            return bestMoves.get((int) (Math.random() * bestMoves.size()));
        }
        
        // Fallback if absolutely nothing was found
        int[] fallbackMoves = rootBoard.getValidMoveIndices(botPlayer);
        if (fallbackMoves.length > 0) {
            return rootBoard.indexToCell[fallbackMoves[0]];
        }
        return null;
    }

    private static final double WIN_SCORE = 1_000_000_000;
    private static final double LOSE_SCORE = -1_000_000_000;

    private static double negaMax(VirtualBoard board, Player currentPlayer, Player opponentPlayer, int depth, double alpha, double beta, long startTime, long timeLimit, TranspositionEntry[] ttable) throws TimeOutException {
        // Fast time check to abort long branches
        if (System.currentTimeMillis() - startTime > timeLimit) {
            throw new TimeOutException();
        }
        
        long hash = board.computeHash(currentPlayer);
        int index = (int)(hash & 131071);
        TranspositionEntry cached = ttable[index];
        int hashMoveIdx = -1;
        if (cached != null && cached.hash == hash) {
            hashMoveIdx = cached.bestMoveIdx;
            if (cached.depth >= depth) {
                if (cached.flag == TT_EXACT) return cached.score;
                if (cached.flag == TT_LOWER) alpha = Math.max(alpha, cached.score);
                if (cached.flag == TT_UPPER) beta = Math.min(beta, cached.score);
                if (alpha >= beta) return cached.score;
            }
        }

        if (depth == 0) {
            // HYPER-AGGRESSIVE LEAF NODE EVALUATOR
            double eval = evaluateAggressive(board, currentPlayer);
            int indexLeaf = (int)(hash & 131071);
            TranspositionEntry cachedLeaf = ttable[indexLeaf];
            if (cachedLeaf == null || 0 >= cachedLeaf.depth) {
                ttable[indexLeaf] = new TranspositionEntry(hash, eval, 0, TT_EXACT, -1);
            }
            return eval;
        }

        int[] validMoves = board.getValidMoveIndices(currentPlayer);
        
        if (validMoves.length == 0) {
            int indexTerm = (int)(hash & 131071);
            TranspositionEntry cachedTerm = ttable[indexTerm];
            if (cachedTerm == null || depth >= cachedTerm.depth) {
                ttable[indexTerm] = new TranspositionEntry(hash, LOSE_SCORE, depth, TT_EXACT, -1);
            }
            return LOSE_SCORE;
        }

        // PV / Hash Move Ordering
        if (hashMoveIdx != -1) {
            for (int i = 0; i < validMoves.length; i++) {
                if (validMoves[i] == hashMoveIdx) {
                    for (int j = i; j > 0; j--) {
                        validMoves[j] = validMoves[j - 1];
                    }
                    validMoves[0] = hashMoveIdx;
                    break;
                }
            }
        }

        double maxScore = -Double.MAX_VALUE;
        double originalAlpha = alpha;
        int bestMoveIdx = -1;

        for (int moveIdx : validMoves) {
            VirtualBoard nextBoard = new VirtualBoard(board);
            double scoreDelta = nextBoard.applyMoveIdx(moveIdx, currentPlayer);
            
            if (scoreDelta >= WIN_SCORE) {
                maxScore = WIN_SCORE;
                bestMoveIdx = moveIdx;
                break;
            }

            double score;
            if (scoreDelta <= LOSE_SCORE) {
                score = LOSE_SCORE;
            } else {
                score = scoreDelta - negaMax(nextBoard, opponentPlayer, currentPlayer, depth - 1, -beta, -alpha, startTime, timeLimit, ttable);
            }

            if (score > maxScore) {
                maxScore = score;
                bestMoveIdx = moveIdx;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break; // Alpha-Beta Cut-off (Pruning)
            }
        }

        byte ttFlag;
        if (maxScore <= originalAlpha) {
            ttFlag = TT_UPPER;
        } else if (maxScore >= beta) {
            ttFlag = TT_LOWER;
        } else {
            ttFlag = TT_EXACT;
        }

        if (maxScore == -Double.MAX_VALUE) maxScore = 0;
        int indexStore = (int)(hash & 131071);
        TranspositionEntry cachedStore = ttable[indexStore];
        if (cachedStore == null || depth >= cachedStore.depth) {
            ttable[indexStore] = new TranspositionEntry(hash, maxScore, depth, ttFlag, bestMoveIdx);
        }
        return maxScore;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Hyper-Aggressive Static Board Evaluator
    // ═══════════════════════════════════════════════════════════════════════════

    private static double evaluateAggressive(VirtualBoard board, Player myPlayer) {
        double score = 0;

        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            double posVal = board.positionWeights[i];

            if (owner == null || owner == Player.Dead) {
                // Control/influence of empty cells (crucial for valuing empty corners)
                for (int adj : board.explosionTargetsArr[i]) {
                    Player adjOwner = board.owners[adj];
                    if (adjOwner == myPlayer) {
                        score += posVal * 2.0; 
                    } else if (adjOwner != null && adjOwner != Player.Dead) {
                        score -= posVal * 2.0; 
                    }
                }
                continue;
            }

            int ats = board.atoms[i];
            int maxA = board.maxAtomsArr[i];
            int distToExplode = maxA - ats;

            if (owner == myPlayer) {
                score += ats * 10.0;   // Friendly Atom advantage (balanced weight)
                score += posVal * 8.0;  // Territory advantage

                if (distToExplode <= 1) {
                    score += posVal * 45.0; // Threat premium for critical cells

                    // Chain reaction potential: reward adjacent critical cells
                    for (int adj : board.explosionTargetsArr[i]) {
                        if (board.owners[adj] == myPlayer && board.atoms[adj] >= board.maxAtomsArr[adj] - 1) {
                            score += 180.0; // Friendly chain reaction
                        } else if (board.owners[adj] != null && board.owners[adj] != myPlayer && board.owners[adj] != Player.Dead) {
                            score += 150.0 + (board.atoms[adj] * 40.0); // MASSIVE reward for exploding on enemies
                        }
                    }
                }

                // Defensive caution: penalize if we are adjacent to an enemy critical cell (predicted atom loss if we did not move/react)
                for (int adj : board.explosionTargetsArr[i]) {
                    Player adjOwner = board.owners[adj];
                    if (adjOwner != null && adjOwner != myPlayer && adjOwner != Player.Dead) {
                        int adjDist = board.maxAtomsArr[adj] - board.atoms[adj];
                        if (adjDist <= 1) {
                            double vulnPenalty = posVal * 20.0 + ats * 15.0;
                            if (distToExplode > 1) { // Only penalize heavily if we CANNOT immediately explode back
                                vulnPenalty += posVal * 25.0;
                            }
                            score -= vulnPenalty;
                        }
                    }
                }
            } else {
                score -= ats * 10.0;   // Enemy Atom advantage (balanced weight)
                score -= posVal * 8.0;  // Territory advantage

                if (distToExplode <= 1) {
                    score -= posVal * 45.0; // Enemy threat
                }
            }
        }
        return score;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Aggressive Opening Gambit
    // ═══════════════════════════════════════════════════════════════════════════

    private static boolean isOpeningPhase(VirtualBoard board, Player botPlayer) {
        int myAtoms = 0;
        int totalAtoms = 0;
        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            if (owner != null && owner != Player.Dead) {
                totalAtoms += board.atoms[i];
                if (owner == botPlayer) {
                    myAtoms += board.atoms[i];
                }
            }
        }
        return myAtoms <= 2 && totalAtoms <= 6;
    }

    private static Cell aggressiveOpeningGambit(VirtualBoard board, Player botPlayer) {
        int bestIdx = -1;
        double bestVal = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            if (owner != null && owner != botPlayer && owner != Player.Dead) continue;
            
            double val = board.positionWeights[i];

            // Safety check: penalise cells adjacent to enemy-occupied cells in opening
            for (int adj : board.explosionTargetsArr[i]) {
                Player adjOwner = board.owners[adj];
                if (adjOwner != null && adjOwner != botPlayer && adjOwner != Player.Dead) {
                    val -= 5.0; // adjacent enemy presence is risky
                    if (board.atoms[adj] >= board.maxAtomsArr[adj] - 1) {
                        val -= 15.0; // adjacent near-critical enemy is very dangerous
                    }
                }
            }

            if (val > bestVal) {
                bestVal = val;
                bestIdx = i;
            }
        }
        return (bestIdx >= 0) ? board.indexToCell[bestIdx] : null;
    }

    private static void logState(VirtualBoard board, Player botPlayer, double score, String reason) {
        try (PrintWriter out = new PrintWriter(new FileWriter("bot_gemini_log.txt", true))) {
            out.println("=====================================================");
            out.println("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            out.println("Reason: " + reason);
            out.println("Bot Player: " + botPlayer);
            out.println("Evaluation Score: " + score);
            out.println("Active Cells: " + board.activeCellCount);
            
            out.println("--- Board State ---");
            for (int i = 0; i < board.activeCellCount; i++) {
                Player owner = board.owners[i];
                if (owner != null && owner != Player.Dead) {
                    out.println(String.format("Cell %02d: %-10s | %d/%d atoms", i, owner.name(), board.atoms[i], board.maxAtomsArr[i]));
                }
            }
            out.println("=====================================================\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Transposition Table
    // ═══════════════════════════════════════════════════════════════════════════

    private static final byte TT_EXACT = 0;
    private static final byte TT_LOWER = 1;
    private static final byte TT_UPPER = 2;

    private static final class TranspositionEntry {
        final long hash;
        final double score;
        final int depth;
        final byte flag;
        final int bestMoveIdx;

        TranspositionEntry(long hash, double score, int depth, byte flag, int bestMoveIdx) {
            this.hash = hash;
            this.score = score;
            this.depth = depth;
            this.flag = flag;
            this.bestMoveIdx = bestMoveIdx;
        }
    }


    // ═══════════════════════════════════════════════════════════════════════════
    // VirtualBoard — Lightweight Directed-Graph Simulation
    // ═══════════════════════════════════════════════════════════════════════════

    private static class VirtualBoard {
        Cell[] indexToCell;
        int[] maxAtomsArr;
        int[][] explosionTargetsArr;
        int activeCellCount;
        boolean[] isShieldArr;
        double[] positionWeights;
        boolean hasShields;

        long[][] zobristAtoms; 
        long[] zobristPlayer; 
        private static final int MAX_PLAYERS = Player.values().length;

        int[] atoms;
        Player[] owners;
        int[] atomMaskArr; 
        Player[] shieldOwnerArr;
        int[] shieldDamageArr;
        int[] cellCounts;

        public VirtualBoard(Main main) {
            hasShields = false;
            List<Cell> activeCells = new ArrayList<>();
            for (Cell c : main.handlerCell.getArray()) {
                if (!c.isCellSpace()) {
                    activeCells.add(c);
                }
            }
            activeCellCount = activeCells.size();
            
            indexToCell = new Cell[activeCellCount];
            Map<Cell, Integer> cellToIndex = new HashMap<>();
            for (int i = 0; i < activeCellCount; i++) {
                indexToCell[i] = activeCells.get(i);
                cellToIndex.put(indexToCell[i], i);
            }
            
            maxAtomsArr = new int[activeCellCount];
            explosionTargetsArr = new int[activeCellCount][];
            isShieldArr = new boolean[activeCellCount];
            positionWeights = new double[activeCellCount];
            
            atoms = new int[activeCellCount];
            owners = new Player[activeCellCount];
            atomMaskArr = new int[activeCellCount];
            shieldOwnerArr = new Player[activeCellCount];
            shieldDamageArr = new int[activeCellCount];

            for (int i = 0; i < activeCellCount; i++) {
                Cell c = indexToCell[i];
                atoms[i] = c.getManagerAtoms().atomsSize();
                owners[i] = c.getManagerAtoms().getPlayer();
                
                int mask = 0;
                for (Player atomOwner : c.getManagerAtoms().getDifferentAtoms()) {
                    mask |= (1 << atomOwner.ordinal());
                }
                atomMaskArr[i] = mask;
                
                if (c instanceof CellBlackHole) {
                    maxAtomsArr[i] = 999999; 
                } else {
                    maxAtomsArr[i] = c.getManagerAtoms().getMaxAtoms();
                }
                
                if (c instanceof CellShield) {
                    isShieldArr[i] = true;
                    hasShields = true;
                    CellShield cs = (CellShield) c;
                    shieldOwnerArr[i] = cs.getManagerShield().getShield();
                    shieldDamageArr[i] = cs.getManagerShield().getShieldDestroy();
                }

                List<Cell> targets = new ArrayList<>();
                if (c instanceof CellCannon) {
                    CellCannon cc = (CellCannon) c;
                    if (cc.CannonTarget != null && !cc.CannonTarget.isCellSpace()) {
                        targets.add(cc.CannonTarget);
                    } else {
                        for (IDDirection d : IDDirection.values()) {
                            Cell adj = c.getManagerSideCell().getSide(d);
                            if (adj != null && !adj.isCellSpace()) {
                                targets.add(adj);
                            }
                        }
                    }
                } else {
                    for (IDDirection d : IDDirection.values()) {
                        Cell adj = c.getManagerSideCell().getSide(d);
                        if (adj != null && !adj.isCellSpace()) {
                            targets.add(adj);
                        }
                    }
                    if (c instanceof CellTeleport) {
                        CellTeleport ct = (CellTeleport) c;
                        if (ct.getManagerTeleport().getTeleportCell() != null && !ct.getManagerTeleport().getTeleportCell().isCellSpace()) {
                            targets.add(ct.getManagerTeleport().getTeleportCell());
                        }
                    }
                }
                
                explosionTargetsArr[i] = new int[targets.size()];
                for (int j = 0; j < targets.size(); j++) {
                    explosionTargetsArr[i][j] = cellToIndex.get(targets.get(j));
                }
                
                int numTargets = targets.size();
                if (numTargets == 2) positionWeights[i] = 10.0;
                else if (numTargets == 3) positionWeights[i] = 6.0;
                else if (numTargets == 4) positionWeights[i] = 3.0;
                else if (numTargets == 1) positionWeights[i] = 15.0;
                else positionWeights[i] = 2.0;
            }

            Random rng = new Random(0xCAFEBABE_DEADBEEFL);
            zobristAtoms = new long[activeCellCount][6 * MAX_PLAYERS];
            for (int i = 0; i < activeCellCount; i++) {
                for (int j = 0; j < 6 * MAX_PLAYERS; j++) {
                    zobristAtoms[i][j] = rng.nextLong();
                }
            }
            zobristPlayer = new long[MAX_PLAYERS];
            for (int i = 0; i < MAX_PLAYERS; i++) {
                zobristPlayer[i] = rng.nextLong();
            }

            cellCounts = new int[MAX_PLAYERS];
            for (int i = 0; i < activeCellCount; i++) {
                if (owners[i] != null) {
                    cellCounts[owners[i].ordinal()]++;
                }
            }
        }

        public VirtualBoard(VirtualBoard other) {
            this.indexToCell = other.indexToCell;
            this.maxAtomsArr = other.maxAtomsArr;
            this.explosionTargetsArr = other.explosionTargetsArr;
            this.activeCellCount = other.activeCellCount;
            this.isShieldArr = other.isShieldArr;
            this.positionWeights = other.positionWeights;
            this.zobristAtoms = other.zobristAtoms;
            this.zobristPlayer = other.zobristPlayer;
            this.hasShields = other.hasShields;
            
            this.atoms = new int[activeCellCount];
            System.arraycopy(other.atoms, 0, this.atoms, 0, activeCellCount);
            this.owners = new Player[activeCellCount];
            System.arraycopy(other.owners, 0, this.owners, 0, activeCellCount);
            this.atomMaskArr = new int[activeCellCount];
            System.arraycopy(other.atomMaskArr, 0, this.atomMaskArr, 0, activeCellCount);
            if (hasShields) {
                this.shieldOwnerArr = new Player[activeCellCount];
                System.arraycopy(other.shieldOwnerArr, 0, this.shieldOwnerArr, 0, activeCellCount);
                this.shieldDamageArr = new int[activeCellCount];
                System.arraycopy(other.shieldDamageArr, 0, this.shieldDamageArr, 0, activeCellCount);
            }
            this.cellCounts = new int[MAX_PLAYERS];
            System.arraycopy(other.cellCounts, 0, this.cellCounts, 0, MAX_PLAYERS);
        }

        private void setCellOwner(int idx, Player newOwner) {
            Player oldOwner = owners[idx];
            if (oldOwner != newOwner) {
                if (oldOwner != null) cellCounts[oldOwner.ordinal()]--;
                if (newOwner != null) cellCounts[newOwner.ordinal()]++;
                owners[idx] = newOwner;
            }
        }

        public long computeHash(Player currentPlayer) {
            long h = zobristPlayer[currentPlayer.ordinal()];
            for (int i = 0; i < activeCellCount; i++) {
                if (owners[i] != null) {
                    int ats = Math.min(atoms[i], 5);
                    int pOrd = owners[i].ordinal();
                    h ^= zobristAtoms[i][ats * MAX_PLAYERS + pOrd];
                }
            }
            return h;
        }

        public List<Cell> getValidMoves(Player p) {
            List<Cell> moves = new ArrayList<>();
            for (int i = 0; i < activeCellCount; i++) {
                Player owner = owners[i];
                if (owner == null || owner == p || owner == Player.Dead) {
                    moves.add(indexToCell[i]);
                }
            }
            return moves;
        }

        public int[] getValidMoveIndices(Player p) {
            int[] temp = new int[activeCellCount];
            int count = 0;
            for (int i = 0; i < activeCellCount; i++) {
                if (maxAtomsArr[i] <= 0) continue; 
                
                if (isShieldArr != null && isShieldArr[i]) {
                    Player sOwner = shieldOwnerArr[i];
                    if (sOwner != null && sOwner != p && sOwner != Player.Dead) {
                        continue; 
                    }
                }
                
                Player owner = owners[i];
                if (owner == null || owner == p || owner == Player.Dead) {
                    temp[count++] = i;
                }
            }
            int[] moves = new int[count];
            System.arraycopy(temp, 0, moves, 0, count);
            
            double[] priorities = new double[count];
            for (int i = 0; i < count; i++) {
                int idx = moves[i];
                priorities[i] = (10 - (maxAtomsArr[idx] - atoms[idx])) * 100.0 + positionWeights[idx] + atoms[idx];
            }
            
            for (int i = 1; i < count; i++) {
                int keyMove = moves[i];
                double keyPri = priorities[i];
                int j = i - 1;
                while (j >= 0 && priorities[j] < keyPri) {
                    moves[j + 1] = moves[j];
                    priorities[j + 1] = priorities[j];
                    j--;
                }
                moves[j + 1] = keyMove;
                priorities[j + 1] = keyPri;
            }
            
            return moves;
        }

        public double applyMoveIdx(int moveIdx, Player p) {
            double score = 0;
            
            int maxA = maxAtomsArr[moveIdx];
            int curA = atoms[moveIdx];
            
            score += (5 - maxA) * 2;
            
            int[] explodeQueue = new int[activeCellCount * 4];
            int head = 0;
            int tail = 0;
            
            if (isShieldArr != null && isShieldArr[moveIdx]) {
                if (shieldOwnerArr[moveIdx] == null) {
                    shieldOwnerArr[moveIdx] = p;
                    return score - 100; 
                } else if (shieldOwnerArr[moveIdx] != p && shieldOwnerArr[moveIdx] != Player.Dead) {
                    shieldDamageArr[moveIdx]++;
                    if (shieldDamageArr[moveIdx] >= SettingsCell.maxShieldHeath) {
                        shieldOwnerArr[moveIdx] = null;
                        shieldDamageArr[moveIdx] = 0;
                    }
                    return score - 10;
                }
            }
            
            setCellOwner(moveIdx, p);
            atoms[moveIdx] = curA + 1;
            atomMaskArr[moveIdx] |= (1 << p.ordinal());
            
            if (atoms[moveIdx] >= maxA) {
                explodeQueue[tail++] = moveIdx;
                score += 100;
            } else if (atoms[moveIdx] == maxA - 1) {
                score += 40; 
            }
            
            score += (5 - maxA) * 2;
            int explosionCount = 0;
            while (head < tail) {
                if (explosionCount++ > 600) break; 
                
                int current = explodeQueue[head++];
                Player popPlayer = owners[current]; 
                
                if (atoms[current] < maxAtomsArr[current]) continue;
                
                atoms[current] = 0;
                atomMaskArr[current] = 0;
                setCellOwner(current, null);
                
                for (int adj : explosionTargetsArr[current]) {
                    if (isShieldArr != null && isShieldArr[adj]) {
                        if (shieldOwnerArr[adj] == null) {
                            shieldOwnerArr[adj] = popPlayer;
                            continue; 
                        } else if (shieldOwnerArr[adj] != popPlayer && shieldOwnerArr[adj] != Player.Dead) {
                            shieldDamageArr[adj]++;
                            if (shieldDamageArr[adj] >= SettingsCell.maxShieldHeath) {
                                shieldOwnerArr[adj] = null;
                                shieldDamageArr[adj] = 0;
                            }
                            continue; 
                        }
                    }

                    int vAtoms = atoms[adj];
                    Player currentOwner = owners[adj];
                    boolean hasPopPlayerAtom = (atomMaskArr[adj] & (1 << popPlayer.ordinal())) != 0;
                    
                    if (vAtoms < maxAtomsArr[adj] - 1) {
                        atoms[adj]++;
                        atomMaskArr[adj] |= (1 << popPlayer.ordinal());
                        if (owners[adj] == null) {
                            setCellOwner(adj, popPlayer);
                        }
                        if (atoms[adj] == maxAtomsArr[adj] - 1) {
                            score += 50; 
                        }
                    } else if (vAtoms == maxAtomsArr[adj] - 1) {
                        atoms[adj]++;
                        if (hasPopPlayerAtom) {
                            atomMaskArr[adj] = (1 << popPlayer.ordinal());
                            setCellOwner(adj, popPlayer);
                            
                            if (currentOwner != null && currentOwner != popPlayer && currentOwner != Player.Dead) {
                                score += 300;           // increased for explosions
                                score += vAtoms * 50;  // increased
                            }
                        } else {
                            atomMaskArr[adj] = (1 << popPlayer.ordinal());
                            setCellOwner(adj, popPlayer);
                            
                            if (currentOwner != null && currentOwner != popPlayer && currentOwner != Player.Dead) {
                                score += 200; // increased
                            }
                        }
                        
                        if (tail >= explodeQueue.length) {
                            int[] newQ = new int[explodeQueue.length * 2];
                            System.arraycopy(explodeQueue, 0, newQ, 0, explodeQueue.length);
                            explodeQueue = newQ;
                        }
                        explodeQueue[tail++] = adj;
                        score += 40; // increased from 20 
                    } else {
                        atoms[adj]++;
                        atomMaskArr[adj] = (1 << popPlayer.ordinal());
                        setCellOwner(adj, popPlayer);
                    }
                }
            }
            
            boolean botAlive = cellCounts[p.ordinal()] > 0;
            boolean enemyAlive = false;
            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (i != p.ordinal() && i != Player.Dead.ordinal() && cellCounts[i] > 0) {
                    enemyAlive = true;
                    break;
                }
            }
            
            if (!botAlive) {
                return LOSE_SCORE;
            }
            if (!enemyAlive) {
                return WIN_SCORE;
            }
            return score;
        }
    }
}
