package it.unicam.cs.mpgc.rpg126224.persistence;

import it.unicam.cs.mpgc.rpg126224.model.*;
import it.unicam.cs.mpgc.rpg126224.model.Rarity;
import it.unicam.cs.mpgc.rpg126224.model.TrapType;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class JsonPersistenceManager implements PersistenceManager {

    private static final String SAVE_DIR  = "save";
    private static final String SAVE_FILE = "save/dungeon_protocol.json";

    @Override
    public void saveGame(GameState state) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            Hero hero = state.getHero();
            Dungeon dungeon = state.getDungeon();
            StringBuilder sb = new StringBuilder();
            sb.append("heroId=").append(hero.getId()).append("\n");
            sb.append("heroName=").append(hero.getName()).append("\n");
            sb.append("heroClass=").append(hero.getHeroClass().name()).append("\n");
            sb.append("heroHp=").append(hero.getCurrentHp()).append("\n");
            sb.append("heroMaxHp=").append(hero.getMaxHp()).append("\n");
            sb.append("heroAttack=").append(hero.getAttack()).append("\n");
            sb.append("heroDefense=").append(hero.getDefense()).append("\n");
            sb.append("heroMagic=").append(hero.getMagic()).append("\n");
            sb.append("heroCurrentMana=").append(hero.getCurrentMana()).append("\n");
            sb.append("heroMaxMana=").append(hero.getMaxMana()).append("\n");
            sb.append("heroLevel=").append(hero.getLevel()).append("\n");
            sb.append("heroXp=").append(hero.getExperience()).append("\n");
            sb.append("heroGold=").append(hero.getGold()).append("\n");
            sb.append("heroRow=").append(hero.getRow()).append("\n");
            sb.append("heroCol=").append(hero.getCol()).append("\n");
            sb.append("dungeonLevel=").append(state.getDungeonLevel()).append("\n");
            sb.append("gameOver=").append(state.isGameOver()).append("\n");
            sb.append("victory=").append(state.isVictory()).append("\n");
            List<Item> inv = hero.getInventory();
            sb.append("inventorySize=").append(inv.size()).append("\n");
            for (int i = 0; i < inv.size(); i++) {
                Item item = inv.get(i);
                sb.append("inv_").append(i).append("_id=").append(item.getId()).append("\n");
                sb.append("inv_").append(i).append("_name=").append(item.getName()).append("\n");
                sb.append("inv_").append(i).append("_type=").append(item.getType().name()).append("\n");
                sb.append("inv_").append(i).append("_value=").append(item.getValue()).append("\n");
                sb.append("inv_").append(i).append("_rarity=").append(item.getRarity().name()).append("\n");
                sb.append("inv_").append(i).append("_quantity=").append(item.getQuantity()).append("\n");
            }
            sb.append("dungeonRows=").append(dungeon.getRows()).append("\n");
            sb.append("dungeonCols=").append(dungeon.getCols()).append("\n");

            // Save wall matrices
            for (int r = 0; r < dungeon.getRows(); r++) {
                for (int c = 0; c < dungeon.getCols(); c++) {
                    sb.append("wr_").append(r).append("_").append(c)
                      .append("=").append(dungeon.hasWallRight(r, c)).append("\n");
                    sb.append("wd_").append(r).append("_").append(c)
                      .append("=").append(dungeon.hasWallDown(r, c)).append("\n");
                }
            }

            for (int r = 0; r < dungeon.getRows(); r++) {
                for (int c = 0; c < dungeon.getCols(); c++) {
                    Room room = dungeon.getRoom(r, c);
                    String prefix = "room_" + r + "_" + c + "_";
                    sb.append(prefix).append("type=").append(room.getType().name()).append("\n");
                    sb.append(prefix).append("visited=").append(room.isVisited()).append("\n");
                    sb.append(prefix).append("cleared=").append(room.isCleared()).append("\n");
                    if (room.hasTrap()) {
                        sb.append(prefix).append("trap=").append(room.getTrap().name()).append("\n");
                    }
                    List<Enemy> enemies = room.getEnemies();
                    sb.append(prefix).append("enemyCount=").append(enemies.size()).append("\n");
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy e = enemies.get(i);
                        String ep = prefix + "enemy_" + i + "_";
                        sb.append(ep).append("id=").append(e.getId()).append("\n");
                        sb.append(ep).append("type=").append(e.getType().name()).append("\n");
                        sb.append(ep).append("hp=").append(e.getCurrentHp()).append("\n");
                    }
                    List<Item> items = room.getItems();
                    sb.append(prefix).append("itemCount=").append(items.size()).append("\n");
                    for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        String ip = prefix + "item_" + i + "_";
                        sb.append(ip).append("id=").append(item.getId()).append("\n");
                        sb.append(ip).append("name=").append(item.getName()).append("\n");
                        sb.append(ip).append("type=").append(item.getType().name()).append("\n");
                        sb.append(ip).append("value=").append(item.getValue()).append("\n");
                        sb.append(ip).append("rarity=").append(item.getRarity().name()).append("\n");
                    }
                }
            }
            Files.writeString(Paths.get(SAVE_FILE), sb.toString());
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
        }
    }

    @Override
    public Optional<GameState> loadGame() {
        Path path = Paths.get(SAVE_FILE);
        if (!Files.exists(path)) return Optional.empty();
        try {
            Map<String, String> d = new HashMap<>();
            for (String line : Files.readAllLines(path)) {
                int eq = line.indexOf('=');
                if (eq > 0) d.put(line.substring(0, eq), line.substring(eq + 1));
            }
            return Optional.of(parseGameState(d));
        } catch (Exception e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean hasSaveFile() { return Files.exists(Paths.get(SAVE_FILE)); }

    @Override
    public void deleteSave() {
        try { Files.deleteIfExists(Paths.get(SAVE_FILE)); }
        catch (IOException e) { System.err.println("Failed to delete save: " + e.getMessage()); }
    }

    private GameState parseGameState(Map<String, String> d) {
        Hero hero = new Hero(d.get("heroId"), d.get("heroName"),
                HeroClass.valueOf(d.get("heroClass")));
        int savedMaxHp = getInt(d, "heroMaxHp");
        int savedHp    = getInt(d, "heroHp");
        if (savedMaxHp > hero.getMaxHp()) {
            int diff = savedMaxHp - hero.getMaxHp();
            // Ripristina maxHp tramite i livelli guadagnati
            hero.setMaxHp(savedMaxHp);
        }
        if (savedHp > 0) hero.setCurrentHp(savedHp);
        hero.setPosition(getInt(d, "heroRow"), getInt(d, "heroCol"));
        int savedAttack  = getInt(d, "heroAttack");
        int savedDefense = getInt(d, "heroDefense");
        int savedMagic   = getInt(d, "heroMagic");
        if (savedAttack  > 0) hero.boostAttack(savedAttack - hero.getAttack());
        if (savedDefense > 0) hero.boostDefense(savedDefense - hero.getDefense());
        if (savedMagic   > 0) hero.boostMagic(savedMagic - hero.getMagic());
        int savedMaxMana     = getInt(d, "heroMaxMana");
        int savedCurrentMana = getInt(d, "heroCurrentMana");
        if (savedMaxMana > hero.getMaxMana())
            hero.setMaxMana(savedMaxMana);
        hero.setCurrentMana(savedCurrentMana);
        hero.setLevel(getInt(d, "heroLevel"));
        hero.setExperience(getInt(d, "heroXp"));
        hero.setGold(getInt(d, "heroGold"));
        int invSize = getInt(d, "inventorySize");
        for (int i = 0; i < invSize; i++) {
            String rarityStr = d.getOrDefault("inv_"+i+"_rarity", "COMMON");
            Rarity rarity = Rarity.valueOf(rarityStr);
            Item item = new Item(d.get("inv_"+i+"_id"), d.get("inv_"+i+"_name"),
                    ItemType.valueOf(d.get("inv_"+i+"_type")), getInt(d, "inv_"+i+"_value"), rarity);
            int qty = getInt(d, "inv_"+i+"_quantity");
            if (qty > 1) item.setQuantity(qty);
            hero.addItem(item);
        }
        int dRows = getInt(d, "dungeonRows");
        int dCols = getInt(d, "dungeonCols");
        if (dRows <= 0) dRows = Dungeon.SIZE;
        if (dCols <= 0) dCols = Dungeon.SIZE;
        Dungeon dungeon = new Dungeon(dRows, dCols);

        // Restore wall matrices (all walls present by default — only restore false)
        for (int r = 0; r < dRows; r++) {
            for (int c = 0; c < dCols; c++) {
                if ("false".equals(d.get("wr_" + r + "_" + c)))
                    dungeon.removeWallRight(r, c);
                if ("false".equals(d.get("wd_" + r + "_" + c)))
                    dungeon.removeWallDown(r, c);
            }
        }

        for (int r = 0; r < dRows; r++) {
            for (int c = 0; c < dCols; c++) {
                String prefix = "room_" + r + "_" + c + "_";
                String typeStr = d.get(prefix + "type");
                if (typeStr == null) continue;
                Room room = dungeon.getRoom(r, c);
                room.setType(RoomType.valueOf(typeStr));
                if (getBool(d, prefix + "visited")) room.markVisited();
                if (getBool(d, prefix + "cleared")) room.markCleared();
                String trapStr = d.get(prefix + "trap");
                if (trapStr != null) room.setTrap(TrapType.valueOf(trapStr));
                int enemyCount = getInt(d, prefix + "enemyCount");
                for (int i = 0; i < enemyCount; i++) {
                    String ep = prefix + "enemy_" + i + "_";
                    Enemy enemy = new Enemy(d.get(ep+"id"), EnemyType.valueOf(d.get(ep+"type")));
                    int eHp = getInt(d, ep+"hp");
                    if (eHp > 0) enemy.setCurrentHp(eHp);
                    room.addEnemy(enemy);
                }
                int itemCount = getInt(d, prefix + "itemCount");
                for (int i = 0; i < itemCount; i++) {
                    String ip = prefix + "item_" + i + "_";
                    room.addItem(new Item(d.get(ip+"id"), d.get(ip+"name"),
                            ItemType.valueOf(d.get(ip+"type")), getInt(d, ip+"value")));
                }
            }
        }
        GameState state = new GameState(hero, dungeon);
        int dungeonLevel = getInt(d, "dungeonLevel");
        if (dungeonLevel > 1) state.setDungeonLevel(dungeonLevel);
        if (getBool(d, "victory"))       state.setVictory();
        else if (getBool(d, "gameOver")) state.setGameOver();
        return state;
    }

    private int getInt(Map<String, String> d, String key) {
        String val = d.get(key);
        if (val == null || val.isBlank()) return 0;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private boolean getBool(Map<String, String> d, String key) {
        return "true".equals(d.get(key));
    }
}