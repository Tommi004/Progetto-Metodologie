package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.controller.DungeonManager;
import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Dungeon and DungeonManager classes.
 */
@DisplayName("Dungeon Tests")
class DungeonTest {

    private Dungeon dungeon;
    private DungeonManager manager;

    @BeforeEach
    void setUp() {
        manager = new DungeonManager();
        dungeon = manager.generateDungeon(1);
    }

    @Test
    @DisplayName("Dungeon has correct default size")
    void dungeonDefaultSize() {
        assertEquals(Dungeon.SIZE, dungeon.getRows());
        assertEquals(Dungeon.SIZE, dungeon.getCols());
    }

    @Test
    @DisplayName("Start room is at position (0,0) and is visited")
    void startRoomCorrect() {
        Room start = dungeon.getRoom(0, 0);
        assertEquals(RoomType.START, start.getType());
        assertTrue(start.isVisited());
    }

    @Test
    @DisplayName("Level 1 exit room contains Troll mini-boss")
    void exitRoomLevel1ContainsTroll() {
        Room exit = dungeon.getRoom(Dungeon.SIZE - 1, Dungeon.SIZE - 1);
        assertEquals(RoomType.EXIT, exit.getType());
        assertFalse(exit.getEnemies().isEmpty());
        assertEquals(EnemyType.TROLL, exit.getEnemies().get(0).getType());
    }

    @Test
    @DisplayName("Level 3 exit room contains Dragon final boss")
    void exitRoomLevel3ContainsDragon() {
        Dungeon level3 = manager.generateDungeon(3);
        Room exit = level3.getRoom(Dungeon.SIZE - 1, Dungeon.SIZE - 1);
        assertEquals(EnemyType.DRAGON, exit.getEnemies().get(0).getType());
    }

    @Test
    @DisplayName("isValidPosition returns false for out-of-bounds coordinates")
    void invalidPositionCheck() {
        assertFalse(dungeon.isValidPosition(-1, 0));
        assertFalse(dungeon.isValidPosition(0, Dungeon.SIZE));
        assertTrue(dungeon.isValidPosition(0, 0));
        assertTrue(dungeon.isValidPosition(Dungeon.SIZE - 1, Dungeon.SIZE - 1));
    }

    @Test
    @DisplayName("getRoom throws for invalid coordinates")
    void getRoomThrowsOnInvalid() {
        assertThrows(IndexOutOfBoundsException.class, () -> dungeon.getRoom(-1, 0));
    }

    @Test
    @DisplayName("Hero movement updates position")
    void heroMovement() {
        Hero hero = new Hero("h1", "Test", HeroClass.WARRIOR);
        boolean moved = manager.moveHero(hero, dungeon, 1, 0);
        assertTrue(moved);
        assertEquals(1, hero.getRow());
        assertEquals(0, hero.getCol());
    }

    @Test
    @DisplayName("Hero cannot move outside dungeon bounds")
    void heroMovementOutOfBounds() {
        Hero hero = new Hero("h1", "Test", HeroClass.WARRIOR);
        boolean moved = manager.moveHero(hero, dungeon, -1, 0);
        assertFalse(moved);
        assertEquals(0, hero.getRow());
    }

    @Test
    @DisplayName("Room is marked visited after hero movement")
    void roomMarkedVisited() {
        Hero hero = new Hero("h1", "Test", HeroClass.WARRIOR);
        manager.moveHero(hero, dungeon, 1, 0);
        assertTrue(dungeon.getRoom(1, 0).isVisited());
    }
}
