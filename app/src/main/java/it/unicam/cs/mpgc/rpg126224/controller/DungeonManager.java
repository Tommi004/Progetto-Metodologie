package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.*;

/**
 * Implementation of {@link DungeonController}.
 *
 * <p>After placing all room types, a recursive-backtracker (DFS) maze
 * algorithm carves passages through the wall grid, guaranteeing:
 * <ul>
 *   <li>Full connectivity — every cell is reachable from START (0,0).</li>
 *   <li>Reduced branching — ~30% of walls between non-maze-path neighbours
 *       are randomly reopened to create optional shortcuts.</li>
 * </ul>
 * The hero's movement is blocked by walls via {@link Dungeon#canMove}.</p>
 */
public class DungeonManager implements DungeonController {

    private static final int ENEMY_ROOMS    = 18;
    private static final int TREASURE_ROOMS = 8;
    private static final int TRAP_ROOMS     = 4;
    private static final int SHOP_ROOMS     = 2;

    /** Probability (0–100) that an extra passage is opened between two
     *  non-adjacent maze cells, creating shortcuts/loops. */
    private static final int EXTRA_PASSAGE_CHANCE = 30;

    private final Random random = new Random();

    private final java.util.Map<ItemType, Rarity> foundUniqueItems =
            new java.util.EnumMap<>(ItemType.class);

    private static final Set<ItemType> UNIQUE_ITEMS = EnumSet.of(
            ItemType.SWORD, ItemType.SPEAR, ItemType.BOW, ItemType.CROSSBOW,
            ItemType.STAFF, ItemType.AMULET, ItemType.ARMOR, ItemType.HELMET
    );

    @Override public void resetUniqueItems()              { foundUniqueItems.clear(); }

    @Override
    public void registerExistingItem(Item item) {
        if (UNIQUE_ITEMS.contains(item.getType())) {
            Rarity existing = foundUniqueItems.get(item.getType());
            if (existing == null || item.getRarity().isHigherThan(existing))
                foundUniqueItems.put(item.getType(), item.getRarity());
        }
    }

    // ------------------------------------------------------------------
    // Dungeon generation
    // ------------------------------------------------------------------

    @Override
    public Dungeon generateDungeon(int level) {
        Dungeon dungeon = new Dungeon();
        int size = Dungeon.SIZE;

        // Fixed anchor rooms
        dungeon.getRoom(0, 0).setType(RoomType.START);
        dungeon.getRoom(0, 0).markVisited();
        Room exitRoom = dungeon.getRoom(size - 1, size - 1);
        exitRoom.setType(RoomType.EXIT);
        exitRoom.addEnemy(new Enemy(UUID.randomUUID().toString(), getBossForLevel(level)));

        placeRooms(dungeon, size, level);
        generateMaze(dungeon, size);

        return dungeon;
    }

    /** Places ENEMY, TREASURE, TRAP and SHOP rooms on EMPTY cells. */
    private void placeRooms(Dungeon dungeon, int size, int level) {
        placeRoomType(dungeon, size, ENEMY_ROOMS, level, RoomType.ENEMY);
        placeRoomType(dungeon, size, TREASURE_ROOMS, level, RoomType.TREASURE);
        placeRoomType(dungeon, size, TRAP_ROOMS, level, RoomType.TRAP);
        placeRoomType(dungeon, size, SHOP_ROOMS, level, RoomType.SHOP);
    }

    private void placeRoomType(Dungeon dungeon, int size, int count,
                               int level, RoomType type) {
        int placed = 0;
        while (placed < count) {
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            Room room = dungeon.getRoom(r, c);
            if (room.getType() == RoomType.EMPTY) {
                room.setType(type);
                populateRoom(room, type, level);
                placed++;
            }
        }
    }

    private void populateRoom(Room room, RoomType type, int level) {
        switch (type) {
            case ENEMY    -> room.addEnemy(new Enemy(UUID.randomUUID().toString(),
                                    getRandomEnemyForLevel(level)));
            case TREASURE -> room.addItem(createRandomItem(level));
            case TRAP     -> room.setTrap(getRandomTrapForLevel(level));
            default       -> { /* SHOP, EMPTY — nothing to add */ }
        }
    }

    // ------------------------------------------------------------------
    // Maze generation — Recursive Backtracker (DFS)
    // ------------------------------------------------------------------

    /**
     * Carves a spanning tree through the grid using iterative DFS, then
     * opens a random subset of remaining walls to add loops/shortcuts.
     */
    private void generateMaze(Dungeon dungeon, int size) {
        boolean[][] visited = new boolean[size][size];
        Deque<int[]> stack  = new ArrayDeque<>();

        // Start carving from (0,0)
        visited[0][0] = true;
        stack.push(new int[]{0, 0});

        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int r = cur[0], c = cur[1];

            // Collect unvisited neighbours
            List<int[]> neighbours = new ArrayList<>();
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (dungeon.isValidPosition(nr, nc) && !visited[nr][nc])
                    neighbours.add(new int[]{nr, nc, d[0], d[1]});
            }

            if (neighbours.isEmpty()) {
                stack.pop();
            } else {
                int[] chosen = neighbours.get(random.nextInt(neighbours.size()));
                int nr = chosen[0], nc = chosen[1];
                int dr = chosen[2], dc = chosen[3];

                // Remove wall between current and chosen
                removeWall(dungeon, r, c, dr, dc);
                visited[nr][nc] = true;
                stack.push(new int[]{nr, nc});
            }
        }

        // Add extra passages (~30%) for shortcuts
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (c + 1 < size && dungeon.hasWallRight(r, c)
                        && random.nextInt(100) < EXTRA_PASSAGE_CHANCE)
                    dungeon.removeWallRight(r, c);
                if (r + 1 < size && dungeon.hasWallDown(r, c)
                        && random.nextInt(100) < EXTRA_PASSAGE_CHANCE)
                    dungeon.removeWallDown(r, c);
            }
        }
    }

    private void removeWall(Dungeon dungeon, int r, int c, int dr, int dc) {
        if (dr == 0 && dc == 1)  dungeon.removeWallRight(r, c);
        if (dr == 0 && dc == -1) dungeon.removeWallRight(r, c - 1);
        if (dr == 1 && dc == 0)  dungeon.removeWallDown(r, c);
        if (dr == -1 && dc == 0) dungeon.removeWallDown(r - 1, c);
    }

    // ------------------------------------------------------------------
    // Movement
    // ------------------------------------------------------------------

    @Override
    public boolean moveHero(Hero hero, Dungeon dungeon, int dRow, int dCol) {
        int newRow = hero.getRow() + dRow;
        int newCol = hero.getCol() + dCol;
        if (!dungeon.canMove(hero.getRow(), hero.getCol(), newRow, newCol))
            return false;
        hero.setPosition(newRow, newCol);
        dungeon.getRoom(newRow, newCol).markVisited();
        return true;
    }

    @Override
    public Room getCurrentRoom(Hero hero, Dungeon dungeon) {
        return dungeon.getRoom(hero.getRow(), hero.getCol());
    }

    // ------------------------------------------------------------------
    // Enemy / item / trap helpers
    // ------------------------------------------------------------------

    private EnemyType getBossForLevel(int level) {
        return switch (level) {
            case 1  -> EnemyType.TROLL;
            case 2  -> EnemyType.ASSASSIN;
            case 3  -> EnemyType.DRAGON;
            case 4  -> EnemyType.LEVIATHAN;
            default -> EnemyType.DEMON_LORD;
        };
    }

    private TrapType getRandomTrapForLevel(int level) {
        return switch (level) {
            case 1  -> TrapType.SPIKE_TRAP;
            case 2  -> TrapType.VENOM_POOL;
            case 3  -> TrapType.HEX_MARK;
            case 4  -> TrapType.LIFE_SIPHON;
            default -> TrapType.BRIMSTONE_PIT;
        };
    }

    private EnemyType getRandomEnemyForLevel(int level) {
        return switch (level) {
            case 1 -> pick(EnemyType.GOBLIN, EnemyType.SKELETON);
            case 2 -> pick(EnemyType.SKELETON, EnemyType.DARK_MAGE);
            case 3 -> pick(EnemyType.GOBLIN, EnemyType.SKELETON, EnemyType.DARK_MAGE);
            case 4 -> pick(EnemyType.KNIGHT, EnemyType.DARK_MAGE);
            default -> pick(EnemyType.DEMON, EnemyType.WITCH, EnemyType.KNIGHT);
        };
    }

    @SafeVarargs
    private <T> T pick(T... options) {
        return options[random.nextInt(options.length)];
    }

    private Item createRandomItem(int level) {
        int roll = random.nextInt(11);
        Rarity rarity = rollRarity(level);
        ItemType type = switch (roll) {
            case 0  -> ItemType.HEALTH_POTION;
            case 1  -> ItemType.SWORD;
            case 2  -> ItemType.SPEAR;
            case 3  -> ItemType.BOW;
            case 4  -> ItemType.CROSSBOW;
            case 5  -> ItemType.STAFF;
            case 6  -> ItemType.AMULET;
            case 7  -> ItemType.ARMOR;
            case 8  -> ItemType.HELMET;
            case 9  -> ItemType.STRENGTH_POTION;
            default -> ItemType.MANA_POTION;
        };

        // Potions are always COMMON — rarity only applies to equipment
        boolean isPotion = !UNIQUE_ITEMS.contains(type);

        if (isPotion) {
            rarity = Rarity.COMMON;
        } else {
            // Equipment: enforce unique-item rule (only upgrade if higher rarity)
            Rarity existing = foundUniqueItems.get(type);
            if (existing != null && !rarity.isHigherThan(existing)) {
                type   = random.nextBoolean() ? ItemType.HEALTH_POTION : ItemType.MANA_POTION;
                rarity = Rarity.COMMON;
            } else {
                foundUniqueItems.put(type, rarity);
            }
        }

        int baseValue = switch (type) {
            case HEALTH_POTION   -> 30 + level * 10;
            case MANA_POTION     -> 20 + level * 8;
            default              -> 5  + level * 3;
        };
        int value = (int) Math.round(baseValue * rarity.getMultiplier());

        String baseName = switch (type) {
            case SWORD           -> "Sword";
            case SPEAR           -> "Spear";
            case BOW             -> "Bow";
            case CROSSBOW        -> "Crossbow";
            case STAFF           -> "Staff";
            case AMULET          -> "Amulet";
            case ARMOR           -> "Armor";
            case HELMET          -> "Helmet";
            case HEALTH_POTION   -> "Health Potion";
            case STRENGTH_POTION -> "Strength Potion";
            case MANA_POTION     -> "Mana Potion";
        };
        String name = rarity == Rarity.COMMON ? baseName
                : rarity.getDisplayName() + " " + baseName;

        return new Item(UUID.randomUUID().toString(), name, type, value, rarity);
    }

    private Rarity rollRarity(int level) {
        int[] t = switch (level) {
            case 1  -> new int[]{ 0,  2, 15};
            case 2  -> new int[]{ 1,  8, 30};
            case 3  -> new int[]{ 3, 15, 45};
            case 4  -> new int[]{ 7, 25, 60};
            default -> new int[]{12, 40, 75};
        };
        int roll = random.nextInt(100);
        if (roll < t[0]) return Rarity.LEGENDARY;
        if (roll < t[1]) return Rarity.EPIC;
        if (roll < t[2]) return Rarity.RARE;
        return Rarity.COMMON;
    }
}