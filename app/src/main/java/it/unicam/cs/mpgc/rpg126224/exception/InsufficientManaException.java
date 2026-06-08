package it.unicam.cs.mpgc.rpg126224.exception;

/**
 * Thrown when a hero attempts to use a mana-costing ability (Special attack)
 * but does not have enough mana in the current pool.
 *
 * <p>This is an unchecked exception. The view disables the Special button when
 * mana is insufficient, so this exception acts as a safety net for programmatic
 * calls (e.g. from tests or future AI controllers) that bypass the UI check.</p>
 *
 * <h2>Example usage</h2>
 * <pre>{@code
 * if (!hero.useMana(manaCost)) {
 *     throw new InsufficientManaException(hero.getCurrentMana(), manaCost);
 * }
 * }</pre>
 */
public class InsufficientManaException extends RuntimeException {

    /** The hero's current mana at the time of the failed attempt. */
    private final int currentMana;

    /** The mana cost that could not be met. */
    private final int requiredMana;

    /**
     * Constructs a new exception with current and required mana values.
     *
     * @param currentMana  the hero's current mana pool
     * @param requiredMana the mana cost that could not be satisfied
     */
    public InsufficientManaException(int currentMana, int requiredMana) {
        super("Insufficient mana: has " + currentMana + ", needs " + requiredMana + ".");
        this.currentMana  = currentMana;
        this.requiredMana = requiredMana;
    }

    /**
     * Returns the hero's mana at the time the exception was thrown.
     *
     * @return current mana value
     */
    public int getCurrentMana()  { return currentMana; }

    /**
     * Returns the mana cost that triggered the exception.
     *
     * @return required mana value
     */
    public int getRequiredMana() { return requiredMana; }
}