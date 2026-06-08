package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Immutable snapshot of the statistics accumulated during a single run.
 *
 * <p>Collected by {@code GameController} and passed to the game-over / victory
 * dialog so the player can review their performance.</p>
 *
 * @param enemiesDefeated  total number of enemies killed
 * @param totalDamageDealt cumulative damage dealt to enemies
 * @param totalDamageTaken cumulative damage taken by the hero
 * @param dungeonsCleared  number of dungeon floors fully cleared (boss defeated)
 * @param finalLevel       hero level at the end of the run
 * @param causeOfDeath     human-readable description of what killed the hero,
 *                         or {@code null} if the run ended in victory
 */
public record RunStats(
        int    enemiesDefeated,
        int    totalDamageDealt,
        int    totalDamageTaken,
        int    dungeonsCleared,
        int    finalLevel,
        String causeOfDeath
) {}