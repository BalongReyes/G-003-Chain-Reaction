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

/**
 * BotLogicClaude — AI Brain (Antigravity Edition)
 *
 * Upgrades over the original BotLogic:
 * 1. Dedicated static board evaluator (evaluate) — atom balance, critical-cell
 * threats, territory weights (corner > edge > center), chain potential,
 * and vulnerability penalties. Completely separate from move simulation.
 * 2. Zobrist transposition table — avoids re-evaluating identical board
 * positions encountered at different depths or move orderings.
 * 3. Smarter move ordering — composite priority (explosion distance +
 * position weight + atom richness) for better alpha-beta pruning.
 * 4. Opening Gambit — on the first 1–2 moves the bot targets corners/edges
 * since those cells are hardest to recapture.
 * 5. Full multi-player support — all non-self, non-Dead players are treated
 * as threats (not just a single "enemy").
 * 6. Iterative deepening with separate time budgets per difficulty:
 * Easy 200 ms / Medium 400 ms / Hard 800 ms.
 * 7. Aspiration Windows on Hard — narrows the search window using the
 * previous depth's score to converge faster.
 */
public class BotLogicClaude {

    // ─── Difficulty ────────────────────────────────────────────────────────────
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public static Difficulty currentDifficulty = Difficulty.HARD;

    // ─── Last-move indicator (for rendering) ───────────────────────────────────
    public static Cell lastBotMove = null;
    public static Player lastBotPlayer = null;

    // ═══════════════════════════════════════════════════════════════════════════
    // Public Entry Point
    // ═══════════════════════════════════════════════════════════════════════════

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

    // ─── Easy: biased-random — prefers cells 1 step from exploding ─────────────
    private static Cell calculateEasyMove(Player botPlayer, Main main) {
        VirtualBoard board = new VirtualBoard(main);
        List<Cell> moves = board.getValidMoves(botPlayer);
        if (!moves.isEmpty()) {
            List<Cell> hotMoves = new ArrayList<>();
            for (int i = 0; i < board.activeCellCount; i++) {
                if (board.indexToCell[i] != null) {
                    Player owner = board.owners[i];
                    if ((owner == null || owner == botPlayer || owner == Player.Dead)
                            && board.atoms[i] == board.maxAtomsArr[i] - 1) {
                        hotMoves.add(board.indexToCell[i]);
                    }
                }
            }
            if (!hotMoves.isEmpty()) {
                return hotMoves.get((int) (Math.random() * hotMoves.size()));
            }
            return moves.get((int) (Math.random() * moves.size()));
        }
        return null;
    }

    // ─── Medium: iterative deepening up to depth 3, 400 ms budget ──────────────
    private static Cell calculateMediumMove(Player botPlayer, Main main) {
        return runIterativeDeepening(botPlayer, main, 3, 400L, false);
    }

    // ─── Hard: iterative deepening up to depth 6, 1000 ms + aspiration ─────────
    private static Cell calculateHardMove(Player botPlayer, Main main) {
        return runIterativeDeepening(botPlayer, main, 6, 1000L, true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Search Infrastructure
    // ═══════════════════════════════════════════════════════════════════════════

    /** Thrown to abort the search when the time budget runs out. */
    private static final class TimeOutException extends Exception {
    }

    /**
     * Iterative-deepening NegaMax with Alpha-Beta pruning and a transposition
     * table.
     *
     * @param useAspiration enable aspiration windows (Hard mode only)
     */
    private static Cell runIterativeDeepening(Player botPlayer, Main main,
            int maxDepth, long timeLimitMs,
            boolean useAspiration) {
        VirtualBoard rootBoard = new VirtualBoard(main);
        Player enemyPlayer = Player.GetNextPlayer(botPlayer, false);

        // Transposition table — cleared each top-level call
        Map<Long, TranspositionEntry> ttable = new HashMap<>(1 << 14);

        long startTime = System.currentTimeMillis();
        List<Cell> bestMoves = new ArrayList<>();

        // Opening Gambit: steer toward high-value positions early on
        if (isOpeningPhase(rootBoard, botPlayer)) {
            Cell openingMove = openingGambit(rootBoard, botPlayer);
            if (openingMove != null)
                return openingMove;
        }

        double prevBestScore = 0;

        for (int depth = 1; depth <= maxDepth; depth++) {
            List<Cell> currentBestMoves = new ArrayList<>();
            double bestScore;
            double alpha, beta;

            if (useAspiration && depth > 2) {
                double window = 200;
                alpha = prevBestScore - window;
                beta = prevBestScore + window;
            } else {
                alpha = -Double.MAX_VALUE;
                beta = Double.MAX_VALUE;
            }

            try {
                bestScore = -Double.MAX_VALUE;
                int[] moves = rootBoard.getValidMoveIndices(botPlayer);

                for (int moveIdx : moves) {
                    Cell c = rootBoard.indexToCell[moveIdx];
                    VirtualBoard next = new VirtualBoard(rootBoard);
                    double delta = next.applyMoveIdx(moveIdx, botPlayer);

                    // Instant win — no need to search any further
                    if (delta >= WIN_SCORE)
                        return c;

                    double score = delta + negaMax(next, enemyPlayer, botPlayer,
                            depth - 1, -beta, -alpha,
                            startTime, timeLimitMs, ttable);

                    if (score > bestScore) {
                        bestScore = score;
                        currentBestMoves.clear();
                        currentBestMoves.add(c);
                    } else if (score == bestScore) {
                        currentBestMoves.add(c);
                    }

                    if (score > alpha)
                        alpha = score;
                    if (useAspiration && alpha >= beta)
                        break; // failed high — re-search at next depth
                }

                if (!currentBestMoves.isEmpty()) {
                    bestMoves.clear();
                    bestMoves.addAll(currentBestMoves);
                    prevBestScore = bestScore;
                }

                if (bestScore >= WIN_SCORE)
                    break; // decisive win found

            } catch (TimeOutException e) {
                break; // keep last fully-completed depth's results
            }
        }

        if (!bestMoves.isEmpty()) {
            return bestMoves.get((int) (Math.random() * bestMoves.size()));
        }

        // Absolute fallback
        int[] fallbackMoves = rootBoard.getValidMoveIndices(botPlayer);
        if (fallbackMoves.length > 0) {
            return rootBoard.indexToCell[fallbackMoves[0]];
        }
        return null;
    }

    // ─── NegaMax with Transposition Table ──────────────────────────────────────

    private static final double WIN_SCORE = 100_000;
    private static final double LOSE_SCORE = -100_000;

    private static double negaMax(VirtualBoard board, Player currentPlayer,
            Player opponentPlayer, int depth,
            double alpha, double beta,
            long startTime, long timeLimit,
            Map<Long, TranspositionEntry> ttable)
            throws TimeOutException {

        if (System.currentTimeMillis() - startTime > timeLimit)
            throw new TimeOutException();

        // Transposition table lookup
        long hash = board.computeHash(currentPlayer);
        TranspositionEntry cached = ttable.get(hash);
        if (cached != null && cached.depth >= depth) {
            if (cached.flag == TT_EXACT)
                return cached.score;
            if (cached.flag == TT_LOWER)
                alpha = Math.max(alpha, cached.score);
            if (cached.flag == TT_UPPER)
                beta = Math.min(beta, cached.score);
            if (alpha >= beta)
                return cached.score;
        }

        if (depth == 0) {
            double eval = evaluate(board, opponentPlayer, currentPlayer);
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
            VirtualBoard next = new VirtualBoard(board);
            double delta = next.applyMoveIdx(moveIdx, currentPlayer);

            if (delta >= WIN_SCORE) {
                maxScore = WIN_SCORE;
                alpha = WIN_SCORE;
                ttFlag = TT_EXACT;
                break;
            }

            double score = delta - negaMax(next, opponentPlayer, currentPlayer,
                    depth - 1, -beta, -alpha,
                    startTime, timeLimit, ttable);

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
                break;
            }
        }

        if (maxScore == -Double.MAX_VALUE)
            maxScore = 0;
        ttable.put(hash, new TranspositionEntry(maxScore, depth, ttFlag));
        return maxScore;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Static Board Evaluator
    // ═══════════════════════════════════════════════════════════════════════════
    //
    // Returns a score from myPlayer's perspective (positive = winning).
    //
    // Factors weighted:
    // • Atom balance — raw atom count differential
    // • Territory — number of cells held, weighted by position value
    // • Critical threats — my cells 1 step from exploding (toward enemy)
    // • Chain potential — cascading explosion setups
    // • Vulnerability — my cells adjacent to near-critical enemy cells
    // ───────────────────────────────────────────────────────────────────────────

    private static double evaluate(VirtualBoard board, Player myPlayer, Player enemyPlayer) {
        double myAtoms = 0, enemyAtoms = 0;
        double myTerritory = 0, enemyTerritory = 0;
        double myThreats = 0, enemyThreats = 0;
        double myVuln = 0;
        double myChain = 0;

        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            if (owner == null || owner == Player.Dead)
                continue;

            int ats = board.atoms[i];
            int maxA = board.maxAtomsArr[i];
            double posVal = board.positionWeights[i];
            int distToExplode = maxA - ats;

            if (owner == myPlayer) {
                myAtoms += ats;
                myTerritory += posVal;

                if (distToExplode <= 1) {
                    myThreats += posVal * 3;
                    // Count adjacent critical cells that would cascade
                    for (int adj : board.explosionTargetsArr[i]) {
                        if (board.owners[adj] != null && board.owners[adj] != Player.Dead
                                && board.atoms[adj] == board.maxAtomsArr[adj] - 1) {
                            myChain += 20;
                        }
                    }
                }

                // Vulnerability: adjacent enemy near-critical cells threatening us
                for (int adj : board.explosionTargetsArr[i]) {
                    Player adjOwner = board.owners[adj];
                    if (adjOwner != null && adjOwner != myPlayer && adjOwner != Player.Dead) {
                        int adjDist = board.maxAtomsArr[adj] - board.atoms[adj];
                        if (adjDist <= 1) {
                            myVuln += posVal * 2 + (distToExplode <= 1 ? posVal * 3 : 0);
                        }
                    }
                }

            } else if (owner != Player.Dead) {
                // Any non-me, non-Dead player is an enemy
                enemyAtoms += ats;
                enemyTerritory += posVal;
                if (distToExplode <= 1)
                    enemyThreats += posVal * 3;
            }
        }

        return (myAtoms - enemyAtoms) * 10.0
                + (myTerritory - enemyTerritory) * 8.0
                + (myThreats - enemyThreats) * 15.0
                - myVuln * 12.0
                + myChain * 7.0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Opening Gambit
    // ═══════════════════════════════════════════════════════════════════════════

    /** True when the bot has placed ≤ 2 atoms (early game). */
    private static boolean isOpeningPhase(VirtualBoard board, Player botPlayer) {
        int myAtoms = 0;
        for (int i = 0; i < board.activeCellCount; i++) {
            if (board.owners[i] == botPlayer)
                myAtoms += board.atoms[i];
        }
        return myAtoms <= 2;
    }

    /**
     * Picks the unoccupied (or friendly) cell with the highest position weight.
     * Corners (weight 10) are strongly preferred over edges (6) and center (3).
     */
    private static Cell openingGambit(VirtualBoard board, Player botPlayer) {
        int bestIdx = -1;
        double bestVal = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < board.activeCellCount; i++) {
            Player owner = board.owners[i];
            if (owner != null && owner != botPlayer && owner != Player.Dead)
                continue;
            double val = board.positionWeights[i];

            // Safety check: penalise cells adjacent to enemy-occupied cells
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

        // ─── Static topology (built once per bot turn from real board) ─────────
        Cell[] indexToCell;
        int[] maxAtomsArr;
        int[][] explosionTargetsArr;
        int activeCellCount;
        boolean[] isShieldArr;
        double[] positionWeights; // corner=10, edge=6, center=3

        // ─── Zobrist hashing ───────────────────────────────────────────────────
        long[][] zobristAtoms; // [cellIdx][atomsCapped * MAX_PLAYERS + playerOrd]
        long[] zobristPlayer; // per-player turn contribution

        private static final int MAX_PLAYERS = Player.values().length;

        // ─── Per-state mutable arrays ──────────────────────────────────────────
        int[] atoms;
        Player[] owners;
        int[] atomMaskArr;
        Player[] shieldOwnerArr;
        int[] shieldDamageArr;

        // ─── Build topology from the live game board ───────────────────────────
        public VirtualBoard(Main main) {
            List<Cell> activeCells = new ArrayList<>();
            for (Cell c : main.handlerCell.getArray()) {
                if (!c.isCellSpace())
                    activeCells.add(c);
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
                for (Player p : c.getManagerAtoms().getDifferentAtoms()) {
                    mask |= (1 << p.ordinal());
                }
                atomMaskArr[i] = mask;

                if (c instanceof CellBlackHole) {
                    maxAtomsArr[i] = 999_999;
                } else {
                    maxAtomsArr[i] = c.getManagerAtoms().getMaxAtoms();
                }

                if (c instanceof CellShield) {
                    isShieldArr[i] = true;
                    CellShield cs = (CellShield) c;
                    shieldOwnerArr[i] = cs.getManagerShield().getShield();
                    shieldDamageArr[i] = cs.getManagerShield().getShieldDestroy();
                }

                // Explosion target graph
                List<Cell> targets = new ArrayList<>();
                if (c instanceof CellCannon) {
                    CellCannon cc = (CellCannon) c;
                    if (cc.CannonTarget != null && !cc.CannonTarget.isCellSpace()) {
                        targets.add(cc.CannonTarget);
                    } else {
                        for (IDDirection d : IDDirection.values()) {
                            Cell adj = c.getManagerSideCell().getSide(d);
                            if (adj != null && !adj.isCellSpace())
                                targets.add(adj);
                        }
                    }
                } else {
                    for (IDDirection d : IDDirection.values()) {
                        Cell adj = c.getManagerSideCell().getSide(d);
                        if (adj != null && !adj.isCellSpace())
                            targets.add(adj);
                    }
                    if (c instanceof CellTeleport) {
                        CellTeleport ct = (CellTeleport) c;
                        if (ct.getManagerTeleport().getTeleportCell() != null
                                && !ct.getManagerTeleport().getTeleportCell().isCellSpace()) {
                            targets.add(ct.getManagerTeleport().getTeleportCell());
                        }
                    }
                }
                explosionTargetsArr[i] = new int[targets.size()];
                for (int j = 0; j < targets.size(); j++) {
                    explosionTargetsArr[i][j] = cellToIndex.get(targets.get(j));
                }

                // Position weight: lower maxAtoms = harder to recapture = more valuable
                int ma = maxAtomsArr[i];
                if (ma == 1)
                    positionWeights[i] = 10.0; // corner
                else if (ma == 2)
                    positionWeights[i] = 6.0; // edge
                else
                    positionWeights[i] = 3.0; // interior
            }

            // Build Zobrist hashing tables (deterministic seed for reproducibility)
            Random rng = new Random(0xDEADBEEF_CAFEBABEL);
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

        // ─── Fast copy constructor (minimal allocation) ────────────────────────
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
            this.owners = new Player[activeCellCount];
            this.atomMaskArr = new int[activeCellCount];
            this.shieldOwnerArr = new Player[activeCellCount];
            this.shieldDamageArr = new int[activeCellCount];

            System.arraycopy(other.atoms, 0, this.atoms, 0, activeCellCount);
            System.arraycopy(other.owners, 0, this.owners, 0, activeCellCount);
            System.arraycopy(other.atomMaskArr, 0, this.atomMaskArr, 0, activeCellCount);
            System.arraycopy(other.shieldOwnerArr, 0, this.shieldOwnerArr, 0, activeCellCount);
            System.arraycopy(other.shieldDamageArr, 0, this.shieldDamageArr, 0, activeCellCount);
        }

        // ─── Zobrist board hash ────────────────────────────────────────────────
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

        // ─── Move helpers ──────────────────────────────────────────────────────

        public List<Cell> getValidMoves(Player p) {
            List<Cell> moves = new ArrayList<>();
            for (int i = 0; i < activeCellCount; i++) {
                Player owner = owners[i];
                if (owner == null || owner == p || owner == Player.Dead)
                    moves.add(indexToCell[i]);
            }
            return moves;
        }

        /**
         * Valid moves sorted by composite priority for stronger alpha-beta pruning:
         * (10 - distToExplode) * 100 + positionWeight + currentAtoms
         *
         * Cells about to pop are explored first; ties broken by strategic value.
         */
        public int[] getValidMoveIndices(Player p) {
            int[] temp = new int[activeCellCount];
            int count = 0;

            for (int i = 0; i < activeCellCount; i++) {
                if (maxAtomsArr[i] <= 0)
                    continue;

                // Skip enemy-owned shields
                if (isShieldArr != null && isShieldArr[i]) {
                    Player sOwner = shieldOwnerArr[i];
                    if (sOwner != null && sOwner != p && sOwner != Player.Dead)
                        continue;
                }

                Player owner = owners[i];
                if (owner == null || owner == p || owner == Player.Dead) {
                    temp[count++] = i;
                }
            }

            int[] moves = Arrays.copyOf(temp, count);

            // Insertion sort (fast for small N typical of Chain Reaction boards)
            for (int i = 1; i < count; i++) {
                int key = moves[i];
                double keyPri = movePriority(key);
                int j = i - 1;
                while (j >= 0 && movePriority(moves[j]) < keyPri) {
                    moves[j + 1] = moves[j];
                    j--;
                }
                moves[j + 1] = key;
            }

            return moves;
        }

        private double movePriority(int idx) {
            int distToExplode = maxAtomsArr[idx] - atoms[idx];
            return (10 - distToExplode) * 100.0 + positionWeights[idx] + atoms[idx];
        }

        // ─── Apply a move; return immediate heuristic delta ────────────────────
        //
        // Scores captured here (not in evaluate()):
        // • Position value of the clicked cell
        // • Reward for triggering / cascade-explosion
        // • Penalty for wasting a turn on an unowned shield
        // • Win bonus when all enemies are eliminated
        //
        // Deep positional analysis (threats, vulnerability) is in evaluate() so
        // that the tree search receives clean positional deltas.
        // ──────────────────────────────────────────────────────────────────────
        public double applyMoveIdx(int moveIdx, Player p) {
            double score = 0;
            int maxA = maxAtomsArr[moveIdx];
            int curA = atoms[moveIdx];

            // ── Positional bonus for choosing low-maxAtoms cells ─────────
            score += (5 - maxA) * 2;

            // ── Shield handling ─────────────────────────────────────────────
            if (isShieldArr != null && isShieldArr[moveIdx]) {
                if (shieldOwnerArr[moveIdx] == null) {
                    shieldOwnerArr[moveIdx] = p;
                    return score - 100; // wasted move
                } else if (shieldOwnerArr[moveIdx] != p && shieldOwnerArr[moveIdx] != Player.Dead) {
                    shieldDamageArr[moveIdx]++;
                    if (shieldDamageArr[moveIdx] >= SettingsCell.maxShieldHeath) {
                        shieldOwnerArr[moveIdx] = null;
                        shieldDamageArr[moveIdx] = 0;
                    }
                    return score - 10; // direct shield attack — low reward
                }
            }

            // ── Place atom ──────────────────────────────────────────────────
            owners[moveIdx] = p;
            atoms[moveIdx] = curA + 1;
            atomMaskArr[moveIdx] |= (1 << p.ordinal());

            if (atoms[moveIdx] >= maxA) {
                score += 100; // explosion trigger
            } else if (atoms[moveIdx] == maxA - 1) {
                score += 40;  // near-critical
            }

            score += (5 - maxA) * 2;

            // ── BFS explosion simulation ────────────────────────────────────
            int[] explodeQueue = new int[activeCellCount * 4];
            int head = 0, tail = 0;

            if (atoms[moveIdx] >= maxA) {
                explodeQueue[tail++] = moveIdx;
            }

            int explosionCount = 0;
            while (head < tail) {
                if (++explosionCount > 600)
                    break;

                int current = explodeQueue[head++];
                Player popPlayer = owners[current];

                if (atoms[current] < maxAtomsArr[current])
                    continue;

                atoms[current] = 0;
                atomMaskArr[current] = 0;
                owners[current] = null;

                for (int adj : explosionTargetsArr[current]) {
                    // Shield absorption
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
                    Player adjOwner = owners[adj];
                    boolean hasMine = (atomMaskArr[adj] & (1 << popPlayer.ordinal())) != 0;

                    if (vAtoms < maxAtomsArr[adj] - 1) {
                        atoms[adj]++;
                        atomMaskArr[adj] |= (1 << popPlayer.ordinal());
                        if (owners[adj] == null) {
                            owners[adj] = popPlayer;
                        }
                        if (atoms[adj] == maxAtomsArr[adj] - 1) {
                            score += 50; // cascade created a new critical cell
                        }
                    } else if (vAtoms == maxAtomsArr[adj] - 1) {
                        atoms[adj]++;
                        if (hasMine) {
                            // popPlayer claims it all
                            if (adjOwner != null && adjOwner != popPlayer && adjOwner != Player.Dead) {
                                score += 50;           // capture bonus
                                score += vAtoms * 10;  // atom-count bonus
                            }
                            atomMaskArr[adj] = (1 << popPlayer.ordinal());
                            owners[adj] = popPlayer;
                        } else {
                            // Neutralising
                            if (adjOwner != null && adjOwner != popPlayer && adjOwner != Player.Dead) {
                                score += 30; // neutralise bonus
                            }
                            atomMaskArr[adj] = (1 << Player.Dead.ordinal());
                            owners[adj] = Player.Dead;
                        }
                        if (tail >= explodeQueue.length) {
                            explodeQueue = Arrays.copyOf(explodeQueue, explodeQueue.length * 2);
                        }
                        explodeQueue[tail++] = adj;
                        score += 20; // chain explosion bonus
                    } else {
                        // Hijack already-queued cell
                        atoms[adj]++;
                        atomMaskArr[adj] = (1 << popPlayer.ordinal());
                        owners[adj] = popPlayer;
                    }
                }
            }

            // ── Post-explosion vulnerability scan ───────────────────────────
            boolean botAlive = false;
            boolean enemyAlive = false;

            for (int c = 0; c < activeCellCount; c++) {
                if (owners[c] == p) {
                    botAlive = true;
                    // Penalise our cells that sit next to near-critical enemies
                    for (int adj : explosionTargetsArr[c]) {
                        Player adjOwner = owners[adj];
                        if (adjOwner != null && adjOwner != p && adjOwner != Player.Dead) {
                            if (atoms[adj] >= maxAtomsArr[adj] - 1) {
                                score -= 50;
                                // Extra penalty if our own cell is also near-critical (mutual threat)
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

            // ── Win detection ───────────────────────────────────────────────
            if (botAlive && !enemyAlive) {
                score += WIN_SCORE;
            }

            return score;
        }
    }
}
