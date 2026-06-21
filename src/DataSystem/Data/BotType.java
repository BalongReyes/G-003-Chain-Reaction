package DataSystem.Data;

/**
 * Identifies which bot-logic implementation a Player uses.
 * Assign this in the Player enum constants to control AI behaviour
 * without touching any other file.
 */
public enum BotType {
    /** No bot logic — used for human players or the Dead pseudo-player. */
    NONE,
    /** Original BotLogicGemini (NegaMax, depth 4, 500 ms). */
    GEMINI,
    /**
     * BotLogicClaude (NegaMax + Zobrist TT + strategic evaluator, depth 5, 800 ms).
     */
    CLAUDE
}
