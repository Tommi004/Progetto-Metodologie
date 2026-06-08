package it.unicam.cs.mpgc.rpg126224.persistence;

import it.unicam.cs.mpgc.rpg126224.model.GameState;
import java.util.Optional;

/**
 * Defines the contract for persisting and restoring a {@link GameState}.
 *
 * <p>The single current implementation, {@code JsonPersistenceManager}, serialises
 * the game state to a flat key=value JSON file at {@code save/level_up.json}.
 * The interface allows alternative backends (e.g. a database, cloud storage or
 * binary format) to be introduced without changing any other layer of the
 * application — a direct application of the <em>Dependency Inversion Principle</em>.</p>
 *
 * <h2>What is persisted</h2>
 * <ul>
 *   <li>Hero stats (HP, mana, XP, level, position, attack/defense/magic)</li>
 *   <li>Full inventory including item rarity</li>
 *   <li>Complete dungeon grid: room types, visited/cleared flags,
 *       enemy state, item drops and trap state for every room</li>
 *   <li>Current dungeon floor number</li>
 * </ul>
 *
 * <h2>Save lifecycle</h2>
 * <ol>
 *   <li>Call {@link #saveGame} at any point during the run.</li>
 *   <li>Call {@link #hasSaveFile} before offering "Load Game" in the menu.</li>
 *   <li>Call {@link #loadGame} to restore a previous session.</li>
 *   <li>Call {@link #deleteSave} after a run ends (game over or victory)
 *       to prevent stale saves from being loaded.</li>
 * </ol>
 */
public interface PersistenceManager {

    /**
     * Serialises the given {@link GameState} and writes it to persistent storage.
     *
     * <p>If a save already exists it is overwritten. Implementations should
     * write atomically (e.g. write to a temp file then rename) to avoid
     * corrupting the save on crash.</p>
     *
     * @param state the complete game state to persist; must not be null
     */
    void saveGame(GameState state);

    /**
     * Deserialises and returns the previously saved {@link GameState}.
     *
     * @return an {@link Optional} containing the restored state, or
     *         {@link Optional#empty()} if no valid save file exists or
     *         if the file cannot be parsed
     */
    Optional<GameState> loadGame();

    /**
     * Returns {@code true} if a readable save file currently exists.
     *
     * <p>Used by the main menu to enable or disable the "Load Game" button.</p>
     *
     * @return {@code true} if a save file is present and accessible
     */
    boolean hasSaveFile();

    /**
     * Deletes the save file from persistent storage.
     *
     * <p>Should be called when a run concludes (game over or victory) to
     * prevent the player from reloading a finished game.
     * Silently does nothing if no save file exists.</p>
     */
    void deleteSave();
}