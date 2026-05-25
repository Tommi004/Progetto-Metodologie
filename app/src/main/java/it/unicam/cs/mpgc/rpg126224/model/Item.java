package it.unicam.cs.mpgc.rpg126224.model;

import java.util.Objects;

/**
 * Represents a collectible item in Dungeon Protocol.
 */
public class Item {

    private final String id;
    private final String name;
    private final ItemType type;
    private final int value;

    public Item(String id, String name, ItemType type, int value) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.value = value;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public int getValue() { return value; }

    @Override
    public String toString() {
        return name + " (" + type + ", +" + value + ")";
    }
}