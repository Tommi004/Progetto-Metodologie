package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of DungeonController.
 */
public class DungeonManager implements DungeonController {

    private static final int ENEMY_ROOMS    = 18;
    private static final int TREASURE_ROOMS = 8;
    private static final int TRAP_ROOMS     = 4;
    private static final int SHOP_ROOMS     = 2;
    private final Random random = new Random();

    /**
     * Tracks the highest rarity found for each unique item type this run.
     * A unique item can only be replaced by a higher rarity copy.
     */
    private final java.util.Map<ItemType, Rarity> foundUniqueItems =
            new java.util.EnumMap<>(ItemType.class);

    /** Item types that can only appear once per rarity tier per run. */
    private static final Set<ItemType> UNIQUE_ITEMS = EnumSet.of(
            ItemType.SWORD, ItemType.BOW, ItemType.STAFF,
            ItemType.ARMOR, ItemType.AMULET, ItemType.STRENGTH_POTION
    );

    @Override
    public void resetUniqueItems() {
        foundUniqueItems.clear();
    }

    @Override
    public void registerExistingItem(Item item) {
        if (UNIQUE_ITEMS.contains(item.getType())) {
            Rarity existing = foundUniqueItems.get(item.getType());
            if (existing == null || item.getRarity().isHigherThan(existing)) {
                foundUniqueItems.put(item.getType(), item.getRarity());
            }
        }
    }

    @Override
    public Dungeon generateDungeon(int level) {
        // NOTE: foundUniqueItems is NOT cleared here.
        // Call resetUniqueItems() explicitly when starting a brand new run.
        Dungeon dungeon = new Dungeon();
        int size = Dungeon.SIZE;

        dungeon.getRoom(0, 0).setType(RoomType.START);
        dungeon.getRoom(0, 0).markVisited();

        Room exitRoom = dungeon.getRoom(size - 1, size - 1);
        exitRoom.setType(RoomType.EXIT);
        exitRoom.addEnemy(new Enemy(UUID.randomUUID().toString(), getBossForLevel(level)));

        int placed = 0;
        while (placed < ENEMY_ROOMS) {
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            Room room = dungeon.getRoom(r, c);
            if (room.getType() == RoomType.EMPTY) {
                room.setType(RoomType.ENEMY);
                room.addEnemy(new Enemy(UUID.randomUUID().toString(), getRandomEnemyForLevel(level)));
                placed++;
            }
        }

        placed = 0;
        while (placed < TREASURE_ROOMS) {
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            Room room = dungeon.getRoom(r, c);
            if (room.getType() == RoomType.EMPTY) {
                room.setType(RoomType.TREASURE);
                room.addItem(createRandomItem(level));
                placed++;
            }
        }

        placed = 0;
        while (placed < TRAP_ROOMS) {
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            Room room = dungeon.getRoom(r, c);
            if (room.getType() == RoomType.EMPTY) {
                room.setType(RoomType.TRAP);
                room.setTrap(getRandomTrapForLevel(level));
                placed++;
            }
        }

        placed = 0;
        while (placed < SHOP_ROOMS) {
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            Room room = dungeon.getRoom(r, c);
            if (room.getType() == RoomType.EMPTY) {
                room.setType(RoomType.SHOP);
                placed++;
            }
        }

        return dungeon;
    }

    @Override
    public boolean moveHero(Hero hero, Dungeon dungeon, int dRow, int dCol) {
        int newRow = hero.getRow() + dRow;
        int newCol = hero.getCol() + dCol;
        if (!dungeon.isValidPosition(newRow, newCol)) return false;
        hero.setPosition(newRow, newCol);
        dungeon.getRoom(newRow, newCol).markVisited();
        return true;
    }

    @Override
    public Room getCurrentRoom(Hero hero, Dungeon dungeon) {
        return dungeon.getRoom(hero.getRow(), hero.getCol());
    }

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
            case 1 -> {
                EnemyType[] types = {EnemyType.GOBLIN, EnemyType.SKELETON};
                yield types[random.nextInt(types.length)];
            }
            case 2 -> {
                EnemyType[] types = {EnemyType.SKELETON, EnemyType.DARK_MAGE};
                yield types[random.nextInt(types.length)];
            }
            case 3 -> {
                EnemyType[] types = {EnemyType.GOBLIN, EnemyType.SKELETON, EnemyType.DARK_MAGE};
                yield types[random.nextInt(types.length)];
            }
            case 4 -> {
                EnemyType[] types = {EnemyType.KNIGHT, EnemyType.DARK_MAGE};
                yield types[random.nextInt(types.length)];
            }
            default -> {
                EnemyType[] types = {EnemyType.DEMON, EnemyType.WITCH, EnemyType.KNIGHT};
                yield types[random.nextInt(types.length)];
            }
        };
    }

    private Item createRandomItem(int level) {
        int roll  = random.nextInt(8);
        Rarity rarity = rollRarity(level);

        ItemType type = switch (roll) {
            case 0 -> ItemType.HEALTH_POTION;
            case 1 -> ItemType.SWORD;
            case 2 -> ItemType.BOW;
            case 3 -> ItemType.STAFF;
            case 4 -> ItemType.ARMOR;
            case 5 -> ItemType.AMULET;
            case 6 -> ItemType.STRENGTH_POTION;
            default -> ItemType.MANA_POTION;
        };

        // Unique item duplicate/upgrade logic
        if (UNIQUE_ITEMS.contains(type)) {
            Rarity existing = foundUniqueItems.get(type);
            if (existing != null && !rarity.isHigherThan(existing)) {
                type = random.nextBoolean() ? ItemType.HEALTH_POTION : ItemType.MANA_POTION;
                rarity = Rarity.COMMON;
            } else {
                foundUniqueItems.put(type, rarity);
            }
        }

        int baseValue = switch (type) {
            case HEALTH_POTION   -> 30 + level * 10;
            case MANA_POTION     -> 20 + level * 8;
            default              -> 5 + (level * 3);
        };
        int value = (int) Math.round(baseValue * rarity.getMultiplier());

        String baseName = switch (type) {
            case HEALTH_POTION   -> "Health Potion";
            case SWORD           -> "Iron Sword";
            case BOW             -> "Elven Bow";
            case STAFF           -> "Magic Staff";
            case ARMOR           -> "Steel Armor";
            case AMULET          -> "Ancient Amulet";
            case STRENGTH_POTION -> "Strength Potion";
            case MANA_POTION     -> "Mana Potion";
        };

        String name = rarity == Rarity.COMMON ? baseName
                : rarity.getDisplayName() + " " + baseName;

        return new Item(UUID.randomUUID().toString(), name, type, value, rarity);
    }

    /**
     * Rolls a rarity based on dungeon level probability tables.
     * Higher levels have significantly better chances of rare/epic/legendary items.
     *
     * @param level dungeon level 1–5
     * @return the rolled Rarity
     */
    private Rarity rollRarity(int level) {
        // Thresholds: [legendary, epic, rare] cumulative from top
        int[] thresholds = switch (level) {
            case 1 -> new int[]{0,   2,  15};   // 0% leg, 2% epic, 15% rare
            case 2 -> new int[]{1,   8,  30};   // 1% leg, 7% epic, 22% rare
            case 3 -> new int[]{3,  15,  45};   // 3% leg, 12% epic, 30% rare
            case 4 -> new int[]{7,  25,  60};   // 7% leg, 18% epic, 35% rare
            default -> new int[]{12, 40,  75};  // 12% leg, 28% epic, 35% rare
        };
        int roll = random.nextInt(100);
        if (roll < thresholds[0]) return Rarity.LEGENDARY;
        if (roll < thresholds[1]) return Rarity.EPIC;
        if (roll < thresholds[2]) return Rarity.RARE;
        return Rarity.COMMON;
    }
}