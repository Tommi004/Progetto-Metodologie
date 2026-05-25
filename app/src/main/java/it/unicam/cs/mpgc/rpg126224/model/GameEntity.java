package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Base interface for all entities in the game (heroes and enemies).
 * Defines the common contract for combat participants.
 */
public interface GameEntity {

    String getId();
    String getName();
    int getCurrentHp();
    int getMaxHp();
    int getAttack();
    int getDefense();

    default boolean isAlive() {
        return getCurrentHp() > 0;
    }

    void takeDamage(int damage);
    void heal(int amount);
}