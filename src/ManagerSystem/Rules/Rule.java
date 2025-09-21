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
        TERRITORIAL_VALIDATE,
        SAME_PLAYER_VALIDATE,
        DIFFERENT_PLAYER_VALIDATE,
        DIFFERENT_OWNER_INVALID
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
        
        if (this.validation == Validation.DIFFERENT_OWNER_INVALID) {
            return cell.isCellPart(TypeCellPart.territory);
        }
        
        if (cell.isCellPart(TypeCellPart.territory) && this.cellPart == TypeCellPart.territory) {
            return true;
        }
        
        if (cellPart != null && !cell.isCellPart(cellPart)) {
            return false;
        }
        
        return (maxAtoms == -1 || cell.getManagerAtoms().getMaxAtoms() == maxAtoms) &&
               (currentAtoms == -1 || cell.getManagerAtoms().atomsSize() == currentAtoms) &&
               (differentAtoms == -1 || cell.getManagerAtoms().getDifferentAtoms().length == differentAtoms);
    }

    public void execute(Cell cell, Player player, boolean explodeAdd) {
        switch (action) {
            case ADD:
                cell.getManagerAtoms().add(player);
                break;
            case REPLACE:
                cell.getManagerAtoms().replaceAllThenAdd(player);
                break;
            case REPLACE_WITH_DEAD:
                cell.getManagerAtoms().replaceAllThenAdd(Player.Dead);
                break;
            case TERRITORIAL_ACTION:
                if (cell instanceof CellTerritory territoryCell) {
                    if (territoryCell.getManagerTerritory().territoryNotOwned()) {
                        territoryCell.getManagerTerritory().setTerritory(player);
                        cell.getManagerAtoms().replaceAll(player);
                        if (!explodeAdd && !territoryCell.getManagerAtoms().checkAtoms(Player.Dead)) return;
                    } else if (territoryCell.getManagerTerritory().territoryCheckOwner(player)) {
                        cell.getManagerAtoms().replaceAllThenAdd(player);
                    } else {
                        if (explodeAdd) {
                            territoryCell.getManagerTerritory().incrementTerritoryDestroy();
                            if (territoryCell.getManagerTerritory().getTerritoryDestroy() >= SettingsCell.maxTerritoryHeath) {
                                territoryCell.getManagerTerritory().reset();
                            } else {
                                territoryCell.getManagerAtoms().reset();
                            }
                        }
                    }
                }
                break;
            case POP:
                cell.setPop(player);
                break;
        }
    }

    public Validation validate(Cell cell, Player player, boolean explodeAdd) {
        switch (validation) {
            case TERRITORIAL_VALIDATE:
                if (cell instanceof CellTerritory cellTerritory) {
                    boolean isOwnerOrUnowned = cellTerritory.territoryValidate(player);
                    if (isOwnerOrUnowned) {
                        return Validation.VALID;
                    }
                    // If owned by another player
                    return explodeAdd ? Validation.VALID : Validation.INVALID;
                }
                break;
            case SAME_PLAYER_VALIDATE:
                return cell.getManagerAtoms().checkAtoms(player) ? Validation.VALID : Validation.INVALID;
            case DIFFERENT_PLAYER_VALIDATE:
                return !cell.getManagerAtoms().checkAtoms(player) ? Validation.VALID : Validation.INVALID;
            case DIFFERENT_OWNER_INVALID:
                if (cell instanceof CellTerritory territoryCell) {
                    if (territoryCell.getManagerTerritory().territoryOwned() && !territoryCell.getManagerTerritory().territoryCheckOwner(player)) {
                        return Validation.INVALID;
                    }
                }
                return Validation.VALID;
        }
        return validation;
    }

    public Action getAction(){
        return action;
    }

}