package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.Random;
import java.util.UUID;

/**
 * Implementation of DungeonController.
 */
public class DungeonManager implements DungeonController {

    private static final int ENEMY_ROOMS    = 18;
    private static final int TREASURE_ROOMS = 8;
    private final Random random = new Random();

    @Override
    public Dungeon generateDungeon(int level) {
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
            default -> EnemyType.DRAGON;
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
            default -> {
                EnemyType[] types = {EnemyType.GOBLIN, EnemyType.SKELETON, EnemyType.DARK_MAGE};
                yield types[random.nextInt(types.length)];
            }
        };
    }

    private Item createRandomItem(int level) {
        int roll = random.nextInt(6);
        int bonus = 5 + (level * 3);
        return switch (roll) {
            case 0 -> new Item(UUID.randomUUID().toString(), "Health Potion",
                    ItemType.HEALTH_POTION, 30 + level * 10);
            case 1 -> new Item(UUID.randomUUID().toString(), "Iron Sword",
                    ItemType.SWORD, bonus);
            case 2 -> new Item(UUID.randomUUID().toString(), "Elven Bow",
                    ItemType.BOW, bonus);
            case 3 -> new Item(UUID.randomUUID().toString(), "Magic Staff",
                    ItemType.STAFF, bonus);
            case 4 -> new Item(UUID.randomUUID().toString(), "Steel Armor",
                    ItemType.ARMOR, bonus);
            default -> new Item(UUID.randomUUID().toString(), "Ancient Amulet",
                    ItemType.AMULET, bonus);
        };
    }
}