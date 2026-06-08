package it.unicam.cs.mpgc.rpg126224.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single room in the dungeon grid.
 */
public class Room {

    private final int row;
    private final int col;
    private RoomType type;
    private final List<Enemy> enemies;
    private final List<Item> items;
    private boolean  visited;
    private boolean  cleared;
    private TrapType trap;     // null = no trap

    public Room(int row, int col, RoomType type) {
        this.row = row;
        this.col = col;
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.enemies = new ArrayList<>();
        this.items = new ArrayList<>();
        this.visited = false;
        this.cleared = false;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = Objects.requireNonNull(type); }
    public List<Enemy> getEnemies() { return List.copyOf(enemies); }
    public List<Item> getItems() { return List.copyOf(items); }
    public boolean isVisited() { return visited; }
    public boolean isCleared() { return cleared; }
    public void markVisited() { this.visited = true; }
    public void markCleared() { this.cleared = true; }

    public TrapType getTrap()          { return trap; }
    public boolean  hasTrap()          { return trap != null; }
    public void     setTrap(TrapType t){ this.trap = t; }
    public void     disarmTrap()       { this.trap = null; }

    public void addEnemy(Enemy enemy) {
        enemies.add(Objects.requireNonNull(enemy));
    }

    public boolean removeEnemy(String enemyId) {
        return enemies.removeIf(e -> e.getId().equals(enemyId));
    }

    public void addItem(Item item) {
        items.add(Objects.requireNonNull(item));
    }

    public boolean removeItem(String itemId) {
        return items.removeIf(i -> i.getId().equals(itemId));
    }

    public boolean allEnemiesDefeated() {
        return enemies.stream().noneMatch(GameEntity::isAlive);
    }

    @Override
    public String toString() {
        return "Room[" + row + "," + col + "] " + type;
    }
}