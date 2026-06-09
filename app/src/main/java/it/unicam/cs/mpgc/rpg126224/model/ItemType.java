package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all item types available in Level Up!.
 *
 * <p>Offensive weapons (Sword, Spear, Bow, Crossbow) share a pool of
 * two slots — the hero can carry at most two offensive weapons at once.
 * Defensive and magic equipment each occupy their own dedicated slot.</p>
 */
public enum ItemType {

    // ── Offensive weapons (shared 2-slot pool) ───────────────────────
    /** Increases ATK. */
    SWORD,
    /** Increases ATK. */
    SPEAR,
    /** Increases ATK; also grants a small MAG bonus (30% of ATK value). */
    BOW,
    /** Increases ATK and MAG equally. */
    CROSSBOW,

    // ── Magic equipment (dedicated slots) ────────────────────────────
    /** Increases MAG. */
    STAFF,
    /** Increases MAG. */
    AMULET,

    // ── Defensive equipment (dedicated slots) ────────────────────────
    /** Increases DEF. */
    ARMOR,
    /** Increases DEF. */
    HELMET,

    // ── Consumables (stackable, no slot limit) ────────────────────────
    /** Restores HP when used. */
    HEALTH_POTION,
    /** Temporarily boosts ATK for one combat encounter. */
    STRENGTH_POTION,
    /** Restores MP when used. */
    MANA_POTION;

    /**
     * Returns {@code true} if this item type is an offensive weapon
     * (contributes to the shared 2-slot offensive weapon pool).
     */
    public boolean isOffensiveWeapon() {
        return switch (this) {
            case SWORD, SPEAR, BOW, CROSSBOW -> true;
            default -> false;
        };
    }

    /**
     * Returns {@code true} if this item type can be stacked in inventory
     * (i.e. it is a consumable potion).
     */
    public boolean isStackable() {
        return switch (this) {
            case HEALTH_POTION, MANA_POTION, STRENGTH_POTION -> true;
            default -> false;
        };
    }

    /**
     * Returns a short human-readable description of the item's effect.
     * Used in the inventory tooltip popup.
     */
    public String getEffectDescription() {
        return switch (this) {
            case SWORD           -> "Increases ATK permanently.";
            case SPEAR           -> "Increases ATK permanently.";
            case BOW             -> "Increases ATK; also grants 30% of ATK value as MAG bonus.";
            case CROSSBOW        -> "Increases both ATK and MAG permanently.";
            case STAFF           -> "Increases MAG permanently.";
            case AMULET          -> "Increases MAG permanently.";
            case ARMOR           -> "Increases DEF permanently.";
            case HELMET          -> "Increases DEF permanently.";
            case HEALTH_POTION   -> "Restores HP when used in combat or from inventory.";
            case MANA_POTION     -> "Restores MP when used in combat or from inventory.";
            case STRENGTH_POTION -> "Temporarily boosts ATK for one combat encounter.";
        };
    }
}