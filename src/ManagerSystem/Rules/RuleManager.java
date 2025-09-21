package ManagerSystem.Rules;

import DataSystem.Data.Player;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import java.util.ArrayList;
import java.util.List;

public class RuleManager {

    private static final List<Rule> rules = new ArrayList<>();

    static {
        // Rules for cells with maxAtoms = 2
        rules.add(new Rule(null, 2, 1, 1, Rule.Action.REPLACE_WITH_DEAD, Rule.Validation.VALID, Rule.Trigger.ANY));

        // Rules for cells with maxAtoms = 3
        rules.add(new Rule(null, 3, 2, 1, Rule.Action.ADD, Rule.Validation.VALID, Rule.Trigger.ANY));
        rules.add(new Rule(null, 3, 2, 2, Rule.Action.REPLACE_WITH_DEAD, Rule.Validation.VALID, Rule.Trigger.ANY));

        // Rules for cells with maxAtoms = 4
        rules.add(new Rule(null, 4, 3, 1, Rule.Action.ADD, Rule.Validation.VALID, Rule.Trigger.ANY));
        rules.add(new Rule(null, 4, 3, 2, Rule.Action.REPLACE_WITH_DEAD, Rule.Validation.VALID, Rule.Trigger.ANY));
        rules.add(new Rule(null, 4, 3, 3, Rule.Action.REPLACE_WITH_DEAD, Rule.Validation.VALID, Rule.Trigger.ANY));

        // Rule for territory cells
        rules.add(new Rule(TypeCellPart.territory, 0, 0, 0, Rule.Action.TERRITORIAL_ACTION, Rule.Validation.TERRITORY_VALIDATE, Rule.Trigger.ANY));
        
        // Rule for popping cells
        rules.add(new Rule(null, 0, 0, 0, Rule.Action.POP, Rule.Validation.DEFAULT, Rule.Trigger.ANY));
    }

    public static void applyRule(Cell cell, Player player, boolean explodeAdd) {
        if (cell.getManagerAtoms().isMaxAtoms()) {
            cell.setPop(player);
            return;
        }

        for (Rule rule : rules) {
            if (rule.check(cell, explodeAdd)) {
                rule.execute(cell, player, explodeAdd);
                return;
            }
        }

        // Default action if no other rule matches
        cell.getManagerAtoms().add(player);
    }

    public static boolean isValid(Cell cell, Player player) {
        for (Rule rule : rules) {
            if (rule.check(cell, false)) { // Corrected this line
                switch (rule.validate(cell, player)) {
                    case VALID -> {
                        return true;
                    }
                    case INVALID -> {
                        return false;
                    }
                    case DEFAULT -> {
                    }
                }
                // Continue to the next rule or default logic
                            }
        }

        // Default validation logic if no specific rule matches
        if (cell.getManagerAtoms().checkAtoms(Player.Dead) || cell.getManagerAtoms().isEmpty()) {
            return true;
        }
        if (cell.getManagerAtoms().atomsSize() >= cell.getManagerAtoms().getMaxAtoms()) {
            return cell.getManagerAtoms().checkAtoms(player);
        }
        return true;
    }
}