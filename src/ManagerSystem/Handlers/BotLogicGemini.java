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

import java.util.*;

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
        return runIterativeDeepening(botPlayer, main, 7, 1200L, true); // Depth 7, 1200ms, Aspiration
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
        Map<Long, TranspositionEntry> ttable = new HashMap<>(1 << 14);

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

            try {
                int[] validMoves = rootBoard.getValidMoveIndices(botPlayer);
                
                for (int moveIdx : validMoves) {
                    Cell c = rootBoard.indexToCell[moveIdx];
                    VirtualBoard nextBoard = new VirtualBoard(rootBoard);
                    double scoreDelta = nextBoard.applyMoveIdx(moveIdx, botPlayer);
                    
                    if (scoreDelta >= WIN_SCORE) return c; // Instant win
                    
                    // NegaMax with Alpha-Beta Pruning
                    double score = scoreDelta + negaMax(nextBoard, enemyPlayer, botPlayer, depth - 1, -beta, -alpha, startTime, timeLimitMs, ttable);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        currentDepthBestMoves.clear();
                        currentDepthBestMoves.add(c);
                    } else if (score == bestScore) {
                        currentDepthBestMoves.add(c);
                    }
                    
                    if (score > alpha) alpha = score;
                    if (useAspiration && alpha >= beta) break; // Aspiration window failed high
                }
                
                // If we completed this depth without timing out, save the results!
                if (!currentDepthBestMoves.isEmpty()) {
                    bestMoves.clear();
                    bestMoves.addAll(currentDepthBestMoves);
                    prevBestScore = bestScore;
                }
                
                // If we found an instant win, no need to search deeper
                if (bestScore >= WIN_SCORE) {
                    break;
                }
                
            } catch (TimeOutException e) {
                // Time limit reached! Abort current depth and fallback to the previous depth's results
                break;
            }
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

    private static final double WIN_SCORE = 1_000_000;
    private static final double LOSE_SCORE = -1_000_000;

    private static double negaMax(VirtualBoard board, Player currentPlayer, Player opponentPlayer, int depth, double alpha, double beta, long startTime, long timeLimit, Map<Long, TranspositionEntry> ttable) throws TimeOutException {
        // Fast time check to abort long branches
        if (System.currentTimeMillis() - startTime > timeLimit) {
            throw new TimeOutException();
        }
        
        long hash = board.computeHash(currentPlayer);
        TranspositionEntry cached = ttable.get(hash);
        if (cached != null && cached.depth >= depth) {
            if (cached.flag == TT_EXACT) return cached.score;
            if (cached.flag == TT_LOWER) alpha = Math.max(alpha, cached.score);
            if (cached.flag == TT_UPPER) beta = Math.min(beta, cached.score);
            if (alpha >= beta) return cached.score;
        }

        if (depth == 0) {
            // HYPER-AGGRESSIVE LEAF NODE EVALUATOR
            double eval = evaluateAggressive(board, opponentPlayer, currentPlayer);
            ttable.put(hash, new TranspositionEntry(eval, 0, TT_EXACT));
            return eval;
        }

        int[] validMoves = board.getValidMoveIndices(currentPlayer);
        
        if (validMoves.length == 0) {
            ttable.put(hash, new TranspositionEntry(LOSE_SCORE, depth, TT_EXACT));
            return LOSE_SCORE;
        }

        double maxScore = -Double.MAX_VALUE;
        byte ttFlag = TT_UPPER;

        for (int moveIdx : validMoves) {
            VirtualBoard nextBoard = new VirtualBoard(board);
            double scoreDelta = nextBoard.applyMoveIdx(moveIdx, currentPlayer);
            
            if (scoreDelta >= WIN_SCORE) {
                maxScore = WIN_SCORE;
                alpha = WIN_SCORE;
                ttFlag = TT_EXACT;
                break;
            }

            double score = scoreDelta - negaMax(nextBoard, opponentPlayer, currentPlayer, depth - 1, -beta, -alpha, startTime, timeLimit, ttable);

            if (score > maxScore) {
                maxScore = score;
                ttFlag = TT_LOWER;
            }
            if (score > alpha) {
                alpha = score;
                ttFlag = TT_EXACT;
            }
            if (alpha >= beta) {
                ttFlag = TT_LOWER;
                break; // Alpha-Beta Cut-off (Pruning)
            }
        }

        if (maxScore == -Double.MAX_VALUE) maxScore = 0;
        ttable.put(hash, new TranspositionEntry(maxScore, depth, ttFlag));
        return maxScore;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Hyper-Aggressive Static Board Evaluator
    // ═══════════════════════════════════════════════════════════════════════════

    private static double evaluateAggressive(VirtualBoard board, Player myPlayer, Player enemyPlayer) {
        double score = 0;

        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            if (owner == null || owner == Player.Dead) continue;

            int ats = board.atoms[i];
            int maxA = board.maxAtomsArr[i];
            double posVal = board.positionWeights[i];
            int distToExplode = maxA - ats;

            if (owner == myPlayer) {
                score += ats * 15.0; // Raw atom advantage
                score += posVal * 8.0; // Territory advantage
                
                if (distToExplode <= 1) {
                    score += 50.0 + posVal * 5.0; // HUGE premium for critical cells

                    // Chain reaction potential - massive reward for having multiple adjacent criticals
                    for (int adj : board.explosionTargetsArr[i]) {
                        if (board.owners[adj] == myPlayer && board.atoms[adj] >= board.maxAtomsArr[adj] - 1) {
                            score += 40.0; 
                        } else if (board.owners[adj] != null && board.owners[adj] != myPlayer && board.owners[adj] != Player.Dead) {
                            // Aggression: rewarding being critical right next to an enemy
                            score += 30.0 + posVal * 2.0;
                        }
                    }
                }
            } else {
                score -= ats * 15.0;
                score -= posVal * 8.0;

                if (distToExplode <= 1) {
                    score -= (50.0 + posVal * 5.0);
                    
                    for (int adj : board.explosionTargetsArr[i]) {
                        if (board.owners[adj] != null && board.owners[adj] != myPlayer && board.owners[adj] != Player.Dead 
                            && board.atoms[adj] >= board.maxAtomsArr[adj] - 1) {
                            score -= 40.0;
                        } else if (board.owners[adj] == myPlayer) {
                            // Vulnerability: enemy is critical next to us
                            score -= (30.0 + posVal * 2.0);
                        }
                    }
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
        for (int i = 0; i < board.activeCellCount; i++) {
            if (board.owners[i] == botPlayer) myAtoms += board.atoms[i];
        }
        return myAtoms <= 2;
    }

    private static Cell aggressiveOpeningGambit(VirtualBoard board, Player botPlayer) {
        int bestIdx = -1;
        double bestVal = Double.NEGATIVE_INFINITY;

        boolean enemyHasPlaced = false;
        for (int i = 0; i < board.activeCellCount; i++) {
            if (board.owners[i] != null && board.owners[i] != botPlayer && board.owners[i] != Player.Dead) {
                enemyHasPlaced = true;
                break;
            }
        }

        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            if (owner != null && owner != botPlayer && owner != Player.Dead) continue;
            
            // Prefer corners normally
            double val = (board.maxAtomsArr[i] == 1) ? 10 : ((board.maxAtomsArr[i] == 2) ? 6 : 3);

            if (enemyHasPlaced) {
                // AGGRESSIVE: Seek out enemy cells!
                for (int adj : board.explosionTargetsArr[i]) {
                    Player adjOwner = board.owners[adj];
                    if (adjOwner != null && adjOwner != botPlayer && adjOwner != Player.Dead) {
                        val += 20.0; // Strong desire to fight early
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

    // ═══════════════════════════════════════════════════════════════════════════
    // Transposition Table
    // ═══════════════════════════════════════════════════════════════════════════

    private static final byte TT_EXACT = 0;
    private static final byte TT_LOWER = 1;
    private static final byte TT_UPPER = 2;

    private static final class TranspositionEntry {
        final double score;
        final int depth;
        final byte flag;

        TranspositionEntry(double score, int depth, byte flag) {
            this.score = score;
            this.depth = depth;
            this.flag = flag;
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

        long[][] zobristAtoms; 
        long[] zobristPlayer; 
        private static final int MAX_PLAYERS = Player.values().length;

        int[] atoms;
        Player[] owners;
        int[] atomMaskArr; 
        Player[] shieldOwnerArr;
        int[] shieldDamageArr;

        public VirtualBoard(Main main) {
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
                
                int ma = maxAtomsArr[i];
                if (ma == 1) positionWeights[i] = 10.0;
                else if (ma == 2) positionWeights[i] = 6.0;
                else positionWeights[i] = 3.0;
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
            
            this.atoms = new int[activeCellCount];
            System.arraycopy(other.atoms, 0, this.atoms, 0, activeCellCount);
            this.owners = new Player[activeCellCount];
            System.arraycopy(other.owners, 0, this.owners, 0, activeCellCount);
            this.atomMaskArr = new int[activeCellCount];
            System.arraycopy(other.atomMaskArr, 0, this.atomMaskArr, 0, activeCellCount);
            if (isShieldArr != null) {
                this.shieldOwnerArr = new Player[activeCellCount];
                System.arraycopy(other.shieldOwnerArr, 0, this.shieldOwnerArr, 0, activeCellCount);
                this.shieldDamageArr = new int[activeCellCount];
                System.arraycopy(other.shieldDamageArr, 0, this.shieldDamageArr, 0, activeCellCount);
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
            
            for (int i = 0; i < count - 1; i++) {
                for (int j = i + 1; j < count; j++) {
                    int aIdx = moves[i];
                    int bIdx = moves[j];
                    double aPri = (10 - (maxAtomsArr[aIdx] - atoms[aIdx])) * 100.0 + positionWeights[aIdx] + atoms[aIdx];
                    double bPri = (10 - (maxAtomsArr[bIdx] - atoms[bIdx])) * 100.0 + positionWeights[bIdx] + atoms[bIdx];
                    if (bPri > aPri) { 
                        int swap = moves[i];
                        moves[i] = moves[j];
                        moves[j] = swap;
                    }
                }
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
            
            owners[moveIdx] = p;
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
                owners[current] = null;
                
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
                            owners[adj] = popPlayer;
                        }
                        if (atoms[adj] == maxAtomsArr[adj] - 1) {
                            score += 50; 
                        }
                    } else if (vAtoms == maxAtomsArr[adj] - 1) {
                        atoms[adj]++;
                        if (hasPopPlayerAtom) {
                            atomMaskArr[adj] = (1 << popPlayer.ordinal());
                            owners[adj] = popPlayer;
                            
                            if (currentOwner != null && currentOwner != popPlayer && currentOwner != Player.Dead) {
                                score += 50; 
                                score += vAtoms * 10; 
                            }
                        } else {
                            atomMaskArr[adj] = (1 << Player.Dead.ordinal());
                            owners[adj] = Player.Dead;
                            
                            if (currentOwner != null && currentOwner != popPlayer && currentOwner != Player.Dead) {
                                score += 30; 
                            }
                        }
                        
                        if (tail >= explodeQueue.length) {
                            int[] newQ = new int[explodeQueue.length * 2];
                            System.arraycopy(explodeQueue, 0, newQ, 0, explodeQueue.length);
                            explodeQueue = newQ;
                        }
                        explodeQueue[tail++] = adj;
                        score += 20; 
                    } else {
                        atoms[adj]++;
                        atomMaskArr[adj] = (1 << popPlayer.ordinal());
                        owners[adj] = popPlayer;
                    }
                }
            }
            
            boolean botAlive = false;
            boolean enemyAlive = false;
            
            for (int c = 0; c < activeCellCount; c++) {
                if (owners[c] == p) {
                    botAlive = true;
                    for (int adj : explosionTargetsArr[c]) {
                        Player adjOwner = owners[adj];
                        if (adjOwner != null && adjOwner != p && adjOwner != Player.Dead) {
                            if (atoms[adj] >= maxAtomsArr[adj] - 1) {
                                score -= 50; 
                                if (atoms[c] >= maxAtomsArr[c] - 1) {
                                    score -= 100; 
                                }
                            }
                        }
                    }
                } else if (owners[c] != null && owners[c] != Player.Dead) {
                    enemyAlive = true;
                }
            }
            
            if (botAlive && !enemyAlive) {
                score += WIN_SCORE; 
            }

            return score;
        }
    }
}
