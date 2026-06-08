package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Base contract for all combat-capable entities in Level Up!.
 *
 * <p>Both {@link Hero} and {@link Enemy} implement this interface, allowing the
 * combat system to operate on either participant without knowing the concrete
 * type. This follows the <em>Dependency Inversion Principle</em>: high-level
 * modules (e.g. {@code CombatManager}) depend on this abstraction rather than
 * on concrete classes.</p>
 *
 * <h2>Damage model</h2>
 * <p>Implementations must apply defense reduction inside {@link #takeDamage}:
 * <pre>
 *     effectiveDamage = Math.max(1, rawDamage - defense / 2)
 * </pre>
 * Trap damage bypasses this reduction and is applied directly via
 * {@code setCurrentHp}.</p>
 *
 * <h2>Extending the entity hierarchy</h2>
 * <p>New entity types (e.g. NPCs, summons) can be introduced by implementing
 * this interface without modifying existing combat logic.</p>
 */
public interface GameEntity {

    /**
     * Returns the unique identifier of this entity.
     *
     * @return non-null, non-empty id string
     */
    String getId();

    /**
     * Returns the display name of this entity (shown in the combat log and UI).
     *
     * @return non-null display name
     */
    String getName();

    /**
     * Returns the entity's current hit points.
     *
     * <p>The value is always in the range {@code [0, getMaxHp()]}.</p>
     *
     * @return current HP, never negative
     */
    int getCurrentHp();

    /**
     * Returns the entity's maximum hit points.
     *
     * @return max HP, always positive
     */
    int getMaxHp();

    /**
     * Returns the entity's physical attack power.
     *
     * <p>Used as the base value for normal attacks and as input to the
     * damage calculation in {@link #takeDamage}.</p>
     *
     * @return attack value, always positive
     */
    int getAttack();

    /**
     * Returns the entity's physical defense.
     *
     * <p>Applied as {@code defense / 2} reduction in {@link #takeDamage}.</p>
     *
     * @return defense value, non-negative
     */
    int getDefense();

    /**
     * Returns {@code true} if the entity has at least 1 HP remaining.
     *
     * <p>Default implementation delegates to {@link #getCurrentHp()}.
     * Concrete classes may override this for special resurrection mechanics.</p>
     *
     * @return {@code true} if alive, {@code false} if defeated
     */
    default boolean isAlive() {
        return getCurrentHp() > 0;
    }

    /**
     * Applies damage to the entity, reducing current HP.
     *
     * <p>Implementations must apply defense reduction:
     * {@code effective = Math.max(1, damage - defense / 2)}.
     * Current HP must never drop below zero.</p>
     *
     * @param damage raw damage amount before defense reduction; must be positive
     */
    void takeDamage(int damage);

    /**
     * Restores HP to the entity.
     *
     * <p>Implementations must cap the result at {@link #getMaxHp()}:
     * healing beyond max HP is silently discarded.</p>
     *
     * @param amount amount of HP to restore; must be positive
     */
    void heal(int amount);
}