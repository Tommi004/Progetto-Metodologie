package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.controller.DungeonManager;
import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameState}.
 *
 * <p>Covers: initial state, game-over and victory transitions,
 * dungeon-level advancement and the MAX_DUNGEON_LEVEL constant.</p>
 */
@DisplayName("GameState Tests")
class GameStateTest {

    private GameState state;
    private Hero      hero;

    @BeforeEach
    void setUp() {
        DungeonManager dm = new DungeonManager();
        hero  = new Hero("h1", "Aldric", HeroClass.WARRIOR);
        state = new GameState(hero, dm.generateDungeon(1));
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("New GameState starts at dungeon level 1")
    void initialDungeonLevel() {
        assertEquals(1, state.getDungeonLevel());
    }

    @Test
    @DisplayName("New GameState is neither game-over nor victory")
    void initialStateIsActive() {
        assertFalse(state.isGameOver());
        assertFalse(state.isVictory());
    }

    @Test
    @DisplayName("Hero reference is stored correctly")
    void heroReference() {
        assertSame(hero, state.getHero());
    }

    @Test
    @DisplayName("Dungeon reference is non-null")
    void dungeonNonNull() {
        assertNotNull(state.getDungeon());
    }

    // -------------------------------------------------------------------------
    // Game-over transition
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setGameOver marks the state as game-over")
    void setGameOverWorks() {
        state.setGameOver();
        assertTrue(state.isGameOver());
    }

    @Test
    @DisplayName("Game-over state is not victory")
    void gameOverIsNotVictory() {
        state.setGameOver();
        assertFalse(state.isVictory());
    }

    // -------------------------------------------------------------------------
    // Victory transition
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setVictory marks the state as victory")
    void setVictoryWorks() {
        state.setVictory();
        assertTrue(state.isVictory());
    }

    @Test
    @DisplayName("Victory state also sets gameOver (game is ended)")
    void victoryAlsoSetsGameOver() {
        state.setVictory();
        assertTrue(state.isGameOver(),
                "isGameOver() should be true on victory — the game session is over");
    }

    // -------------------------------------------------------------------------
    // Level advancement
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("advanceLevel increments dungeon level by one")
    void advanceLevelIncrementsLevel() {
        DungeonManager dm = new DungeonManager();
        Dungeon next = dm.generateDungeon(2);
        state.advanceLevel(next);
        assertEquals(2, state.getDungeonLevel());
    }

    @Test
    @DisplayName("advanceLevel replaces the dungeon reference")
    void advanceLevelReplacesDungeon() {
        DungeonManager dm = new DungeonManager();
        Dungeon next = dm.generateDungeon(2);
        state.advanceLevel(next);
        assertSame(next, state.getDungeon());
    }

    @Test
    @DisplayName("MAX_DUNGEON_LEVEL is 5")
    void maxDungeonLevelIs5() {
        assertEquals(5, GameState.MAX_DUNGEON_LEVEL);
    }
}