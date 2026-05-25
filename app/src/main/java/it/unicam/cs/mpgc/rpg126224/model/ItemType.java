package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Enumeration of all item types available in Dungeon Protocol.
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
    STRENGTH_POTION
}