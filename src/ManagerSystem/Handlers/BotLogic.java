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

public class BotLogic {

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
        return calculateMinimaxMove(botPlayer, main, 4); // Depth 4 for Hard
    }

    private static Cell calculateMediumMove(Player botPlayer, Main main) {
        return calculateMinimaxMove(botPlayer, main, 2); // Depth 2 for Medium
    }
    
    // Special exception to abort search when time limit is reached
    private static class TimeOutException extends Exception {}
    
    private static Cell calculateMinimaxMove(Player botPlayer, Main main, int maxDepth) {
        VirtualBoard currentBoard = new VirtualBoard(main);
        
        List<Cell> absoluteBestMoves = new ArrayList<>();
        Player enemyPlayer = Player.GetNextPlayer(botPlayer, false);

        long startTime = System.currentTimeMillis();
        long timeLimit = 500; // 500ms max

        // Iterative Deepening
        for (int depth = 1; depth <= maxDepth; depth++) { 
            List<Cell> currentDepthBestMoves = new ArrayList<>();
            double bestScore = -Double.MAX_VALUE;
            double alpha = -Double.MAX_VALUE;
            double beta = Double.MAX_VALUE;
            
            try {
                int[] validMoves = currentBoard.getValidMoveIndices(botPlayer);
                
                for (int moveIdx : validMoves) {
                    Cell c = VirtualBoard.indexToCell[moveIdx];
                    VirtualBoard nextBoard = new VirtualBoard(currentBoard);
                    double scoreDelta = nextBoard.applyMoveIdx(moveIdx, botPlayer);
                    
                    // NegaMax with Alpha-Beta Pruning
                    double score = scoreDelta - negaMax(nextBoard, enemyPlayer, botPlayer, depth - 1, -beta, -alpha, startTime, timeLimit);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        currentDepthBestMoves.clear();
                        currentDepthBestMoves.add(c);
                    } else if (score == bestScore) {
                        currentDepthBestMoves.add(c);
                    }
                    
                    if (score > alpha) {
                        alpha = score;
                    }
                }
                
                // If we completed this depth without timing out, save the results!
                if (!currentDepthBestMoves.isEmpty()) {
                    absoluteBestMoves.clear();
                    absoluteBestMoves.addAll(currentDepthBestMoves);
                }
                
                // If we found an instant win, no need to search deeper
                if (bestScore >= 1000000) {
                    break;
                }
                
            } catch (TimeOutException e) {
                // Time limit reached! Abort current depth and fallback to the previous depth's results
                break;
            }
        }

        if (!absoluteBestMoves.isEmpty()) {
            return absoluteBestMoves.get((int) (Math.random() * absoluteBestMoves.size()));
        }
        
        // Fallback if absolutely nothing was found
        int[] fallbackMoves = currentBoard.getValidMoveIndices(botPlayer);
        if (fallbackMoves.length > 0) {
            return VirtualBoard.indexToCell[fallbackMoves[0]];
        }
        return null;
    }

    private static double negaMax(VirtualBoard board, Player currentPlayer, Player opponentPlayer, int depth, double alpha, double beta, long startTime, long timeLimit) throws TimeOutException {
        // Fast time check to abort long branches
        if (System.currentTimeMillis() - startTime > timeLimit) {
            throw new TimeOutException();
        }
        
        if (depth == 0) {
            return 0; // Reached depth limit
        }

        double maxScore = -Double.MAX_VALUE;

        int[] validMoves = board.getValidMoveIndices(currentPlayer);

        for (int moveIdx : validMoves) {
            VirtualBoard nextBoard = new VirtualBoard(board);
            double scoreDelta = nextBoard.applyMoveIdx(moveIdx, currentPlayer);
            
            double score = scoreDelta - negaMax(nextBoard, opponentPlayer, currentPlayer, depth - 1, -beta, -alpha, startTime, timeLimit);

            if (score > maxScore) {
                maxScore = score;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break; // Alpha-Beta Cut-off (Pruning)
            }
        }

        if (maxScore == -Double.MAX_VALUE) return 0;
        return maxScore;
    }

    // A lightweight directed-graph representation of the board state that enables instant BFS simulation
    private static class VirtualBoard {
        static Cell[] indexToCell = null;
        static int[] maxAtomsArr = null;
        static int[][] explosionTargetsArr = null;
        static int activeCellCount = 0;
        static boolean[] isShieldArr = null;

        int[] atoms;
        Player[] owners;
        int[] atomMaskArr; // Bitmask of players who have atoms in the cell
        Player[] shieldOwnerArr;
        int[] shieldDamageArr;

        // Initialize static graph mapping (only runs once per turn)
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
                    maxAtomsArr[i] = 999999; // Black holes never explode
                } else {
                    maxAtomsArr[i] = c.getManagerAtoms().getMaxAtoms();
                }
                
                if (c instanceof CellShield) {
                    isShieldArr[i] = true;
                    CellShield cs = (CellShield) c;
                    shieldOwnerArr[i] = cs.getManagerShield().getShield();
                    shieldDamageArr[i] = cs.getManagerShield().getShieldDestroy();
                }

                // Map how atoms will spread
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
                
                // Convert targets to index array
                explosionTargetsArr[i] = new int[targets.size()];
                for (int j = 0; j < targets.size(); j++) {
                    explosionTargetsArr[i][j] = cellToIndex.get(targets.get(j));
                }
            }
        }

        // Extremely fast primitive copy constructor (0 object allocation besides the 2 arrays)
        public VirtualBoard(VirtualBoard other) {
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

        public boolean isValidMove(Cell c, Player p) {
            // Find index manually (only called at top level depth)
            for (int i = 0; i < activeCellCount; i++) {
                if (indexToCell[i] == c) {
                    Player owner = owners[i];
                    return owner == null || owner == p || owner == Player.Dead;
                }
            }
            return false;
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

        // Returns primitive array of valid moves sorted by heuristic (Move Ordering)
        public int[] getValidMoveIndices(Player p) {
            int[] temp = new int[activeCellCount];
            int count = 0;
            for (int i = 0; i < activeCellCount; i++) {
                if (maxAtomsArr[i] <= 0) continue; 
                
                // If it's a shield owned by the enemy, we cannot click it directly!
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
            
            // Primitive Selection Sort for Move Ordering
            // We want cells closest to exploding (maxAtoms - atoms) to be evaluated FIRST
            // This massively increases Alpha-Beta Pruning efficiency
            for (int i = 0; i < count - 1; i++) {
                for (int j = i + 1; j < count; j++) {
                    int aDist = maxAtomsArr[moves[i]] - atoms[moves[i]];
                    int bDist = maxAtomsArr[moves[j]] - atoms[moves[j]];
                    if (bDist < aDist) { 
                        int swap = moves[i];
                        moves[i] = moves[j];
                        moves[j] = swap;
                    }
                }
            }
            
            return moves;
        }

        public double applyMove(Cell move, Player p) {
            int moveIdx = -1;
            for (int i = 0; i < activeCellCount; i++) {
                if (indexToCell[i] == move) {
                    moveIdx = i;
                    break;
                }
            }
            return applyMoveIdx(moveIdx, p);
        }

        private double applyMoveIdx(int moveIdx, Player p) {
            double score = 0;
            
            int maxA = maxAtomsArr[moveIdx];
            int curA = atoms[moveIdx];
            
            score += (5 - maxA) * 2;
            
            // Fast primitive Queue with dynamic resizing
            int[] explodeQueue = new int[activeCellCount * 4];
            int head = 0;
            int tail = 0;
            
            if (isShieldArr != null && isShieldArr[moveIdx]) {
                if (shieldOwnerArr[moveIdx] == null) {
                    shieldOwnerArr[moveIdx] = p;
                    // Extreme penalty to forbid the bot from using unowned shields as generic stalling tactics.
                    // An unowned shield already absorbs an explosion, so claiming it defensively is a wasted turn.
                    return score - 100; 
                } else if (shieldOwnerArr[moveIdx] != p && shieldOwnerArr[moveIdx] != Player.Dead) {
                    shieldDamageArr[moveIdx]++;
                    if (shieldDamageArr[moveIdx] >= SettingsCell.maxShieldHeath) {
                        shieldOwnerArr[moveIdx] = null;
                        shieldDamageArr[moveIdx] = 0;
                    }
                    // Penalize attacking enemy shields directly unless it's part of an explosion
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
                // Highly reward making a cell critical (1 atom away from exploding)
                score += 40; 
            }
            
            score += (5 - maxA) * 2;
            int explosionCount = 0;
            while (head < tail) {
                if (explosionCount++ > 500) break; // Hard cap to prevent infinite game loops
                
                int current = explodeQueue[head++];
                Player popPlayer = owners[current]; // The player whose atoms are spreading
                
                // If the cell didn't retain enough atoms, skip
                if (atoms[current] < maxAtomsArr[current]) continue;
                
                // Popping clears the cell
                atoms[current] = 0;
                atomMaskArr[current] = 0;
                owners[current] = null;
                
                for (int adj : explosionTargetsArr[current]) {
                    if (isShieldArr != null && isShieldArr[adj]) {
                        if (shieldOwnerArr[adj] == null) {
                            shieldOwnerArr[adj] = popPlayer;
                            continue; // Atom absorbed
                        } else if (shieldOwnerArr[adj] != popPlayer && shieldOwnerArr[adj] != Player.Dead) {
                            shieldDamageArr[adj]++;
                            if (shieldDamageArr[adj] >= SettingsCell.maxShieldHeath) {
                                shieldOwnerArr[adj] = null;
                                shieldDamageArr[adj] = 0;
                            }
                            continue; // Atom absorbed
                        }
                    }

                    int vAtoms = atoms[adj];
                    Player currentOwner = owners[adj];
                    boolean hasPopPlayerAtom = (atomMaskArr[adj] & (1 << popPlayer.ordinal())) != 0;
                    
                    if (vAtoms < maxAtomsArr[adj] - 1) {
                        // Shared atom added
                        atoms[adj]++;
                        atomMaskArr[adj] |= (1 << popPlayer.ordinal());
                        if (owners[adj] == null) {
                            owners[adj] = popPlayer;
                        }
                        if (atoms[adj] == maxAtomsArr[adj] - 1) {
                            score += 50; // Highly reward explosion spread that creates new critical cells
                        }
                    } else if (vAtoms == maxAtomsArr[adj] - 1) {
                        // Threshold hit!
                        atoms[adj]++;
                        if (hasPopPlayerAtom) {
                            // popPlayer claims it all
                            atomMaskArr[adj] = (1 << popPlayer.ordinal());
                            owners[adj] = popPlayer;
                            
                            // Score reward for capturing
                            if (currentOwner != null && currentOwner != popPlayer && currentOwner != Player.Dead) {
                                score += 50; 
                                score += vAtoms * 10; 
                            }
                        } else {
                            // Neutralizing!
                            atomMaskArr[adj] = (1 << Player.Dead.ordinal());
                            owners[adj] = Player.Dead;
                            
                            // Score reward for neutralizing an enemy
                            if (currentOwner != null && currentOwner != popPlayer && currentOwner != Player.Dead) {
                                score += 30; 
                            }
                        }
                        
                        // Queue the explosion
                        if (tail >= explodeQueue.length) {
                            int[] newQ = new int[explodeQueue.length * 2];
                            System.arraycopy(explodeQueue, 0, newQ, 0, explodeQueue.length);
                            explodeQueue = newQ;
                        }
                        explodeQueue[tail++] = adj;
                        score += 20; 
                    } else {
                        // Already in queue. Hijacks the explosion!
                        atoms[adj]++;
                        atomMaskArr[adj] = (1 << popPlayer.ordinal());
                        owners[adj] = popPlayer;
                    }
                }
            }
            
            // Strategic Vulnerability Check
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
            
            // Win Condition Detection!
            // If the bot has cells, but no enemy has any cells left, the bot has achieved total dominance.
            if (botAlive && !enemyAlive) {
                score += 100000; // Massive reward for wiping out the enemy!
            }

            return score;
        }
    }
}
