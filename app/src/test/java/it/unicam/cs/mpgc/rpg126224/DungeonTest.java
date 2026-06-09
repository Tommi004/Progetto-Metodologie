package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.controller.DungeonManager;
import it.unicam.cs.mpgc.rpg126224.exception.InvalidDungeonPositionException;
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
    @DisplayName("getRoom throws InvalidDungeonPositionException for invalid coordinates")
    void getRoomThrowsOnInvalid() {
        assertThrows(InvalidDungeonPositionException.class,
                () -> dungeon.getRoom(-1, 0));
        assertThrows(InvalidDungeonPositionException.class,
                () -> dungeon.getRoom(0, Dungeon.SIZE));
    }

    @Test
    @DisplayName("Hero movement updates position")
    void heroMovement() {
        Hero hero = new Hero("h1", "Test", HeroClass.WARRIOR);
        // Remove the wall between (0,0) and (1,0) so the move is valid
        dungeon.removeWallDown(0, 0);
        boolean moved = manager.moveHero(hero, dungeon, 1, 0);
        assertTrue(moved);
        assertEquals(1, hero.getRow());
        assertEquals(0, hero.getCol());
    }

    @Test
    @DisplayName("Hero cannot move through a wall")
    void heroBlockedByWall() {
        // Use a fresh Dungeon (all walls present by default) — not the maze-generated one
        Dungeon freshDungeon = new Dungeon();
        Hero hero = new Hero("h1", "Test", HeroClass.WARRIOR);
        boolean moved = manager.moveHero(hero, freshDungeon, 1, 0);
        assertFalse(moved, "Hero should not move through a wall");
        assertEquals(0, hero.getRow());
    }

    @Test
    @DisplayName("removeWallDown allows movement after wall removal")
    void wallRemovalAllowsMovement() {
        Hero hero = new Hero("h1", "Test", HeroClass.WARRIOR);
        dungeon.removeWallDown(0, 0);
        assertTrue(manager.moveHero(hero, dungeon, 1, 0));
    }

    @Test
    @DisplayName("Generated dungeon is fully connected (START reachable from all cells via BFS)")
    void generatedDungeonIsConnected() {
        Dungeon generated = manager.generateDungeon(1);
        int size = Dungeon.SIZE;
        boolean[][] visited = new boolean[size][size];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{0, 0});
        visited[0][0] = true;
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            for (int[] d : dirs) {
                int nr = cur[0] + d[0], nc = cur[1] + d[1];
                if (generated.isValidPosition(nr, nc) && !visited[nr][nc]
                        && generated.canMove(cur[0], cur[1], nr, nc)) {
                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                assertTrue(visited[r][c],
                        "Cell [" + r + "," + c + "] not reachable from START");
    }
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