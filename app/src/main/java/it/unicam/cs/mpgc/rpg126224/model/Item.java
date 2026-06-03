package it.unicam.cs.mpgc.rpg126224.model;

import java.util.Objects;

/**
 * Represents a collectible item in Dungeon Protocol.
 * Each item has a {@link Rarity} that affects its value and display.
 */
public class Item {

    private final String   id;
    private final String   name;
    private final ItemType type;
    private final int      value;
    private final Rarity   rarity;

    public Item(String id, String name, ItemType type, int value, Rarity rarity) {
        this.id     = Objects.requireNonNull(id,     "id cannot be null");
        this.name   = Objects.requireNonNull(name,   "name cannot be null");
        this.type   = Objects.requireNonNull(type,   "type cannot be null");
        this.rarity = Objects.requireNonNull(rarity, "rarity cannot be null");
        this.value  = value;
    }

    /** Convenience constructor — defaults to COMMON rarity. */
    public Item(String id, String name, ItemType type, int value) {
        this(id, name, type, value, Rarity.COMMON);
    }

    public String   getId()     { return id; }
    public String   getName()   { return name; }
    public ItemType getType()   { return type; }
    public int      getValue()  { return value; }
    public Rarity   getRarity() { return rarity; }

    @Override
    public String toString() {
        return "[" + rarity.getDisplayName() + "] " + name + " (+" + value + ")";
    }
}