package it.unicam.cs.mpgc.rpg126224.persistence;

import it.unicam.cs.mpgc.rpg126224.model.GameState;
import java.util.Optional;

/**
 * Defines the contract for saving and loading game state.
 */
public interface PersistenceManager {

    void saveGame(GameState state);
    Optional<GameState> loadGame();
    boolean hasSaveFile();
    void deleteSave();
}