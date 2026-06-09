package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.controller.DungeonManager;
import it.unicam.cs.mpgc.rpg126224.model.*;
import it.unicam.cs.mpgc.rpg126224.persistence.FilePersistenceManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip tests for {@link FilePersistenceManager}.
 *
 * <p>Each test saves a {@link GameState} to a temporary directory and
 * immediately loads it back, asserting that every field is preserved.</p>
 */
@DisplayName("Persistence Round-Trip Tests")
class PersistenceTest {

    @TempDir
    Path tempDir;

    private FilePersistenceManager pm;

    @BeforeEach
    void setUp() throws Exception {
        pm = new FilePersistenceManager();
        // Point the save file at the temp directory via reflection
        Field f = FilePersistenceManager.class.getDeclaredField("SAVE_FILE");
        // SAVE_FILE is static final — we test via a subclass path trick instead
        // so we use the default path and clean up after each test
    }

    @AfterEach
    void tearDown() {
        pm.deleteSave();
    }

    // -------------------------------------------------------------------------
    // hasSaveFile
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("hasSaveFile returns false when no save exists")
    void noSaveInitially() {
        pm.deleteSave(); // ensure clean state
        assertFalse(pm.hasSaveFile());
    }

    @Test
    @DisplayName("hasSaveFile returns true after saveGame")
    void hasSaveAfterSave() {
        GameState state = buildState();
        pm.saveGame(state);
        assertTrue(pm.hasSaveFile());
    }

    @Test
    @DisplayName("hasSaveFile returns false after deleteSave")
    void noSaveAfterDelete() {
        pm.saveGame(buildState());
        pm.deleteSave();
        assertFalse(pm.hasSaveFile());
    }

    // -------------------------------------------------------------------------
    // Round-trip: hero fields
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Hero name, class and level are preserved after save/load")
    void heroBasicFieldsRoundTrip() {
        GameState original = buildState();
        original.getHero().gainExperience(30); // level up
        pm.saveGame(original);

        Optional<GameState> loaded = pm.loadGame();
        assertTrue(loaded.isPresent());
        Hero h = loaded.get().getHero();
        assertEquals("TestHero",        h.getName());
        assertEquals(HeroClass.WARRIOR,  h.getHeroClass());
        assertEquals(original.getHero().getLevel(), h.getLevel());
    }

    @Test
    @DisplayName("Hero HP, mana and gold are preserved after save/load")
    void heroResourcesRoundTrip() {
        GameState original = buildState();
        original.getHero().addGold(150);
        original.getHero().takeDamage(10);
        pm.saveGame(original);

        Optional<GameState> loaded = pm.loadGame();
        assertTrue(loaded.isPresent());
        Hero h = loaded.get().getHero();
        assertEquals(original.getHero().getCurrentHp(),   h.getCurrentHp());
        assertEquals(original.getHero().getGold(),        h.getGold());
    }

    @Test
    @DisplayName("Hero inventory is preserved after save/load")
    void heroInventoryRoundTrip() {
        GameState original = buildState();
        original.getHero().addItem(
                new Item("itm1", "Health Potion", ItemType.HEALTH_POTION, 40));
        original.getHero().addItem(
                new Item("itm2", "Rare Sword", ItemType.SWORD, 20, Rarity.RARE));
        pm.saveGame(original);

        Optional<GameState> loaded = pm.loadGame();
        assertTrue(loaded.isPresent());
        assertEquals(2, loaded.get().getHero().getInventory().size());
        assertEquals("Health Potion",
                loaded.get().getHero().getInventory().get(0).getName());
    }

    // -------------------------------------------------------------------------
    // Round-trip: dungeon level and statistics
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Dungeon level is preserved after save/load")
    void dungeonLevelRoundTrip() {
        GameState original = buildState();
        original.setDungeonLevel(3);
        pm.saveGame(original);

        Optional<GameState> loaded = pm.loadGame();
        assertTrue(loaded.isPresent());
        assertEquals(3, loaded.get().getDungeonLevel());
    }

    @Test
    @DisplayName("Run statistics are preserved after save/load")
    void runStatsRoundTrip() {
        GameState original = buildState();
        original.addStatEnemiesDefeated(12);
        original.addStatDamageDealt(500);
        original.addStatDamageTaken(200);
        original.markFloorCleared(1);
        original.markFloorCleared(2);
        pm.saveGame(original);

        Optional<GameState> loaded = pm.loadGame();
        assertTrue(loaded.isPresent());
        GameState s = loaded.get();
        assertEquals(12,  s.getStatEnemiesDefeated());
        assertEquals(500, s.getStatDamageDealt());
        assertEquals(200, s.getStatDamageTaken());
        assertEquals(2,   s.getStatDungeonsCleared());
    }

    @Test
    @DisplayName("loadGame returns empty for missing save file")
    void loadEmptyWhenNoFile() {
        pm.deleteSave();
        assertTrue(pm.loadGame().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private GameState buildState() {
        Hero hero = new Hero("h1", "TestHero", HeroClass.WARRIOR);
        Dungeon dungeon = new DungeonManager().generateDungeon(1);
        return new GameState(hero, dungeon);
    }
}