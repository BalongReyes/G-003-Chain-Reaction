// src/ManagerSystem/Rules/Rule.java

package ManagerSystem.Rules;

import DataSystem.Data.Player;
import DataSystem.Type.TypeCellPart;
import MainSystem.Object.Cell;
import MainSystem.Object.CellType.CellTerritory;
import Settings.SettingsCell;

public class Rule {

    private final TypeCellPart cellPart;
    private final int maxAtoms;
    private final int currentAtoms;
    private final int differentAtoms;
    private final Action action;
    private final Validation validation;
    private final Trigger trigger;

    public enum Action {
        ADD,
        REPLACE,
        REPLACE_WITH_DEAD,
        TERRITORIAL_ACTION,
        POP
    }

    public enum Validation {
        VALID,
        INVALID,
        DEFAULT,
        TERRITORY_VALIDATE
    }
    
    public enum Trigger {
        CLICK,
        EXPLOSION,
        ANY
    }

    public Rule(TypeCellPart cellPart, int maxAtoms, int currentAtoms, int differentAtoms, Action action, Validation validation, Trigger trigger) {
        this.cellPart = cellPart;
        this.maxAtoms = maxAtoms;
        this.currentAtoms = currentAtoms;
        this.differentAtoms = differentAtoms;
        this.action = action;
        this.validation = validation;
        this.trigger = trigger;
    }

    public boolean check(Cell cell, boolean explodeAdd) {
        Trigger currentTrigger = explodeAdd ? Trigger.EXPLOSION : Trigger.CLICK;
        if (this.trigger != Trigger.ANY && this.trigger != currentTrigger) {
            return false;
        }
        
        if (cell.isCellPart(TypeCellPart.territory) && this.cellPart == TypeCellPart.territory) {
            return true;
        }
        
        if (cellPart != null && !cell.isCellPart(cellPart)) {
            return false;
        }
        
        return cell.getManagerAtoms().getMaxAtoms() == maxAtoms &&
               cell.getManagerAtoms().atomsSize() == currentAtoms &&
               cell.getManagerAtoms().getDifferentAtoms().length == differentAtoms;
    }

    public void execute(Cell cell, Player player, boolean explodeAdd) {
        switch (action) {
            case ADD -> {
                cell.getManagerAtoms().add(player);
            }
            case REPLACE -> {
                cell.getManagerAtoms().replaceAllThenAdd(player);
            }
            case REPLACE_WITH_DEAD -> {
                cell.getManagerAtoms().replaceAllThenAdd(Player.Dead);
            }
            case TERRITORIAL_ACTION -> {
                if (cell instanceof CellTerritory territoryCell) {
                    if (territoryCell.getManagerTerritory().territoryNotOwned()) {
                        territoryCell.getManagerTerritory().setTerritory(player);
                        if (!explodeAdd && !territoryCell.getManagerAtoms().checkAtoms(Player.Dead)) return;
                    } else if (territoryCell.getManagerTerritory().territoryCheckOwner(player)) {
                        // If the player already owns the territory, just add the atom
                        cell.getManagerAtoms().add(player);
                    } else {
                        if (explodeAdd) {
                            territoryCell.getManagerTerritory().incrementTerritoryDestroy();
                            if (territoryCell.getManagerTerritory().getTerritoryDestroy() >= SettingsCell.maxTerritoryHeath) {
                                territoryCell.getManagerTerritory().reset();
                            }
                        }
                    }
                }
            }
            case POP -> {
                cell.setPop(player);
            }
        }
    }

    public Validation validate(Cell cell, Player player) {
        if (validation == Validation.TERRITORY_VALIDATE) {
            if (cell instanceof CellTerritory cellTerritory) {
                return cellTerritory.territoryValidate(player) ? Validation.VALID : Validation.INVALID;
            }
        }
        return validation;
    }

    public Action getAction(){
        return action;
    }
    
}