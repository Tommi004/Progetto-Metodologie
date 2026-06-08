package it.unicam.cs.mpgc.rpg126224.model;

import java.util.Objects;

/**
 * Represents an enemy in Level Up!.
 */
public class Enemy implements GameEntity {

    private final String id;
    private final EnemyType type;
    private int currentHp;

    public Enemy(String id, EnemyType type) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.currentHp = type.getBaseHp();
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return type.getDisplayName(); }
    @Override public int getCurrentHp() { return currentHp; }
    @Override public int getMaxHp() { return type.getBaseHp(); }
    @Override public int getAttack() { return type.getBaseAttack(); }
    @Override public int getDefense() { return type.getBaseDefense(); }

    public int getMagic() { return type.getBaseMagic(); }
    public EnemyType getType() { return type; }
    public int getXpReward() { return type.getXpReward(); }

    public void setCurrentHp(int hp) {
        this.currentHp = Math.max(0, Math.min(type.getBaseHp(), hp));
    }

    @Override
    public void takeDamage(int damage) {
        int effective = Math.max(1, damage - type.getBaseDefense() / 2);
        currentHp = Math.max(0, currentHp - effective);
    }

    @Override
    public void heal(int amount) {
        currentHp = Math.min(type.getBaseHp(), currentHp + amount);
    }

    @Override
    public String toString() {
        return type.getDisplayName() + " HP:" + currentHp + "/" + type.getBaseHp();
    }
}