package DataSystem.Data;

import DataSystem.State.StatePlayerAtoms;
import MainSystem.Main.Main;
import java.awt.Color;
import java.util.Stack;

public enum Player {

    // BotType controls which AI implementation is used for this player.
    // Change a player's BotType here to switch its logic without touching any other
    // file.
    Dead(Color.gray, false, BotType.NONE),
    Player1(new Color(255, 85, 0), "Bot 1 Gem", true, BotType.GEMINI),
    Player2(new Color(255, 170, 0), "Bot 2 Cla", true, BotType.CLAUDE),
    Player3(new Color(255, 255, 0), "Bot 3 Gem", true, BotType.GEMINI),
    Player4(new Color(170, 255, 0), "Bot 4 Cla", true, BotType.CLAUDE),
    Player5(new Color(85, 255, 0), "Bot 5 Gem", true, BotType.GEMINI),
    Player6(new Color(0, 255, 85), "Bot 6 Cla", true, BotType.CLAUDE),
    Player7(new Color(0, 255, 255), "Bot 7 Gem", true, BotType.GEMINI),
    Player8(new Color(0, 85, 255), "Bot 8 Cla", true, BotType.CLAUDE),
    Player9(new Color(85, 0, 255), "Bot 9 Gem", true, BotType.GEMINI),
    Player10(new Color(255, 0, 255), "Bot 10 Cla", true, BotType.CLAUDE);

    // ===========================================================================================================

    public Color color;
    public String name = null;
    public boolean isBot = false;
    public BotType botType = BotType.NONE;

    public boolean hintEnabled = false;

    // Constructor
    // ===============================================================================================

    private Player(Color color, String name, boolean isBot, BotType botType) {
        this.color = color;
        this.name = name;
        this.isBot = isBot;
        this.botType = botType;

        if (color == Color.red) {
            throw new RuntimeException("Red is not a valid player color");
        }
    }

    private Player(Color color, boolean isBot, BotType botType) {
        this(color, null, isBot, botType);
    }

    // Main Methods
    // ==============================================================================================

    public int atoms = -1;

    public void incrementAtoms() {
        if (this.atoms < 0)
            this.atoms = 0;
        this.atoms++;
    }

    public void decrementAtoms() {
        this.atoms--;
        if (this.atoms < 0)
            this.atoms = 0;
    }

    // Static Methods
    // ============================================================================================

    public static Player GetPlayer(int index) {
        if (index == -1)
            return null;
        for (Player dP : values())
            if (dP.ordinal() == index) {
                return dP;
            }
        return null;
    }

    public static Player GetNextPlayer(Player currentPlayer, boolean forced) {
        int index = currentPlayer.ordinal();
        int count = 0;

        Player dP;
        do {
            count++;
            index++;
            if (index >= values().length)
                index = 0;

            dP = values()[index];
            if (dP != Dead) {
                if (count > values().length) {
                    dP = currentPlayer;
                    break;
                }
                if (forced)
                    break;
            }
        } while (dP.atoms == 0 || dP == Dead);

        return dP;
    }

    public static void Reset() {
        for (Player dP : values())
            dP.atoms = -1;
    }

    public static int CountAlive() {
        int output = 0;
        for (Player dP : values()) {
            if (dP == Dead)
                continue;
            if (dP.atoms != 0 || dP.atoms == -1)
                output++;
        }
        return output;
    }

    // State
    // =====================================================================================================

    private Stack<StatePlayerAtoms> playerAtoms = new Stack<>();

    public static void ResetState() {
        for (Player dP : values()) {
            dP.playerAtoms.clear();
        }
    }

    public static void SaveState(int undoLimit) {
        for (Player dP : values()) {
            dP.playerAtoms.add(new StatePlayerAtoms(dP.atoms));
            if (dP.playerAtoms.size() > undoLimit) {
                dP.playerAtoms.remove(0);
            }
        }
    }

    public static void UndoState() {
        for (Player dP : values()) {
            if (dP.playerAtoms.empty())
                continue;
            StatePlayerAtoms sA = dP.playerAtoms.pop();
            dP.atoms = sA.getState();
        }
    }

}
