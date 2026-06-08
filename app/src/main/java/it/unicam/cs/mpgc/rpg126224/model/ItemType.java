package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all item types available in Level Up!.
 */
public enum ItemType {

    /** Restores HP to the hero. */
    HEALTH_POTION,

    /** Permanently increases attack stat. */
    SWORD,

    /** Permanently increases attack stat (Archer version). */
    BOW,

    /** Permanently increases magic stat (Mage version). */
    STAFF,

    /** Permanently increases defense stat. */
    ARMOR,

    /** Permanently increases magic stat. */
    AMULET,

    /** Temporarily boosts attack. */
    STRENGTH_POTION,

    /** Restores mana to the hero. */
    MANA_POTION;

    /**
     * Returns a short human-readable description of the item's effect.
     * Used in the inventory tooltip popup.
     *
     * @return effect description string
     */
    public String getEffectDescription() {
        return switch (this) {
            case HEALTH_POTION   -> "Restores HP when used in combat.";
            case MANA_POTION     -> "Restores MP when used in combat.";
            case SWORD           -> "Permanently increases ATK.";
            case BOW             -> "Permanently increases ATK — optimised for Archers.";
            case STAFF           -> "Permanently increases MAG — optimised for Mages.";
            case ARMOR           -> "Permanently increases DEF.";
            case AMULET          -> "Permanently increases MAG.";
            case STRENGTH_POTION -> "Temporarily boosts ATK for one combat encounter.";
        };
    }
}