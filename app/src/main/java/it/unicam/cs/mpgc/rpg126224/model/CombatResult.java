package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Immutable record representing the outcome of a single combat turn.
 */
public record CombatResult(
        int heroDamageDealt,
        int enemyDamageDealt,
        boolean heroFled,
        boolean enemyDefeated,
        boolean heroDefeated,
        String message
) {
    public boolean isCombatOver() {
        return heroFled || enemyDefeated || heroDefeated;
    }
}