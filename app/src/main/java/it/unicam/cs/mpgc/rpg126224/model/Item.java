package it.unicam.cs.mpgc.rpg126224.model;

import java.util.Objects;

/**
 * Represents a collectible item in Level Up!.
 *
 * <p>Each item has a {@link Rarity} that affects its value and display.
 * Consumable items (potions) support stacking: multiple copies of the same
 * consumable occupy a single inventory slot and are tracked via
 * {@link #getQuantity()}. Equipment items (weapons, armour) always have
 * quantity = 1 and are never stacked.</p>
 */
public class Item {

    private final String   id;
    private final String   name;
    private final ItemType type;
    private final int      value;
    private final Rarity   rarity;
    private int            quantity;

    public Item(String id, String name, ItemType type, int value, Rarity rarity) {
        this.id       = Objects.requireNonNull(id,     "id cannot be null");
        this.name     = Objects.requireNonNull(name,   "name cannot be null");
        this.type     = Objects.requireNonNull(type,   "type cannot be null");
        this.rarity   = Objects.requireNonNull(rarity, "rarity cannot be null");
        this.value    = value;
        this.quantity = 1;
    }

    /** Convenience constructor — defaults to COMMON rarity, quantity 1. */
    public Item(String id, String name, ItemType type, int value) {
        this(id, name, type, value, Rarity.COMMON);
    }

    public String   getId()       { return id; }
    public String   getName()     { return name; }
    public ItemType getType()     { return type; }
    public int      getValue()    { return value; }
    public Rarity   getRarity()   { return rarity; }
    public int      getQuantity() { return quantity; }

    /**
     * Returns {@code true} if this item type can be stacked (i.e. it is
     * a consumable: potion or strength potion).
     *
     * @return {@code true} for stackable consumables
     */
    public boolean isStackable() { return type.isStackable(); }

    /**
     * Increments the stack count by one.
     * Should only be called on stackable items.
     */
    public void incrementQuantity() { quantity++; }

    /**
     * Decrements the stack count by one, down to a minimum of 0.
     *
     * @return {@code true} if the stack is now empty (quantity reached 0)
     */
    public boolean decrementQuantity() {
        if (quantity > 0) quantity--;
        return quantity == 0;
    }

    /**
     * Sets the quantity directly (used when loading from a save file).
     *
     * @param quantity the quantity to set; must be ≥ 1
     */
    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    @Override
    public String toString() {
        String q = quantity > 1 ? " ×" + quantity : "";
        return "[" + rarity.getDisplayName() + "] " + name + q + " (+" + value + ")";
    }
}