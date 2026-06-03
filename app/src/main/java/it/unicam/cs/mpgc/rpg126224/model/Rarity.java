package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Item rarity tier.
 * Each tier has a display name, a stat multiplier applied to the base item value,
 * and a hex color used in the TreasureView popup.
 */
public enum Rarity {

    COMMON(    "Common",     1.0,  "#a0a0a0"),
    RARE(      "Rare",       2.0,  "#4488ff"),
    EPIC(      "Epic",       3.5,  "#aa44ff"),
    LEGENDARY( "Legendary",  5.5,  "#ffaa00");

    private final String displayName;
    private final double multiplier;
    private final String color;

    Rarity(String displayName, double multiplier, String color) {
        this.displayName = displayName;
        this.multiplier  = multiplier;
        this.color       = color;
    }

    public String getDisplayName() { return displayName; }
    public double getMultiplier()  { return multiplier; }
    public String getColor()       { return color; }

    /**
     * Returns true if this rarity is strictly higher than {@code other}.
     * Used to enforce the upgrade-only rule for unique items.
     */
    public boolean isHigherThan(Rarity other) {
        return this.ordinal() > other.ordinal();
    }
}