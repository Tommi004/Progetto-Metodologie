package it.unicam.cs.mpgc.rpg126224.persistence;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * JSON-based implementation of PersistenceManager.
 * Serializes and deserializes the full GameState without external libraries.
 */
public class JsonPersistenceManager implements PersistenceManager {

    private static final String SAVE_DIR  = "save";
    private static final String SAVE_FILE = "save/dungeon_protocol.json";

    @Override
    public void saveGame(GameState state) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");

            Hero hero = state.getHero();
            sb.append("  \"heroId\": \"").append(escape(hero.getId())).append("\",\n");
            sb.append("  \"heroName\": \"").append(escape(hero.getName())).append("\",\n");
            sb.append("  \"heroClass\": \"").append(hero.getHeroClass().name()).append("\",\n");
            sb.append("  \"heroHp\": ").append(hero.getCurrentHp()).append(",\n");
            sb.append("  \"heroMaxHp\": ").append(hero.getMaxHp()).append(",\n");
            sb.append("  \"heroAttack\": ").append(hero.getAttack()).append(",\n");
            sb.append("  \"heroDefense\": ").append(hero.getDefense()).append(",\n");
            sb.append("  \"heroMagic\": ").append(hero.getMagic()).append(",\n");
            sb.append("  \"heroLevel\": ").append(hero.getLevel()).append(",\n");
            sb.append("  \"heroXp\": ").append(hero.getExperience()).append(",\n");
            sb.append("  \"heroRow\": ").append(hero.getRow()).append(",\n");
            sb.append("  \"heroCol\": ").append(hero.getCol()).append(",\n");

            sb.append("  \"inventory\": [\n");
            List<Item> inv = hero.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                Item item = inv.get(i);
                sb.append("    {\"id\":\"").append(escape(item.getId()))
                  .append("\",\"name\":\"").append(escape(item.getName()))
                  .append("\",\"type\":\"").append(item.getType().name())
                  .append("\",\"value\":").append(item.getValue()).append("}");
                if (i < inv.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");

            Dungeon dungeon = state.getDungeon();
            sb.append("  \"dungeonRows\": ").append(dungeon.getRows()).append(",\n");
            sb.append("  \"dungeonCols\": ").append(dungeon.getCols()).append(",\n");
            sb.append("  \"rooms\": [\n");
            boolean firstRoom = true;
            for (int r = 0; r < dungeon.getRows(); r++) {
                for (int c = 0; c < dungeon.getCols(); c++) {
                    Room room = dungeon.getRoom(r, c);
                    if (!firstRoom) sb.append(",\n");
                    firstRoom = false;
                    sb.append("    {");
                    sb.append("\"row\":").append(r).append(",");
                    sb.append("\"col\":").append(c).append(",");
                    sb.append("\"type\":\"").append(room.getType().name()).append("\",");
                    sb.append("\"visited\":").append(room.isVisited()).append(",");
                    sb.append("\"cleared\":").append(room.isCleared()).append(",");

                    sb.append("\"enemies\":[");
                    List<Enemy> enemies = room.getEnemies();
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy e = enemies.get(i);
                        sb.append("{\"id\":\"").append(escape(e.getId()))
                          .append("\",\"type\":\"").append(e.getType().name())
                          .append("\",\"hp\":").append(e.getCurrentHp()).append("}");
                        if (i < enemies.size() - 1) sb.append(",");
                    }
                    sb.append("],");

                    sb.append("\"items\":[");
                    List<Item> items = room.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        Item item = items.get(i);
                        sb.append("{\"id\":\"").append(escape(item.getId()))
                          .append("\",\"name\":\"").append(escape(item.getName()))
                          .append("\",\"type\":\"").append(item.getType().name())
                          .append("\",\"value\":").append(item.getValue()).append("}");
                        if (i < items.size() - 1) sb.append(",");
                    }
                    sb.append("]}");
                }
            }
            sb.append("\n  ],\n");
            sb.append("  \"gameOver\": ").append(state.isGameOver()).append(",\n");
            sb.append("  \"victory\": ").append(state.isVictory()).append(",\n");
            sb.append("  \"dungeonLevel\": ").append(state.getDungeonLevel()).append("\n");
            sb.append("}");

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
            String json = Files.readString(path);
            return Optional.of(parseGameState(json));
        } catch (Exception e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean hasSaveFile() {
        return Files.exists(Paths.get(SAVE_FILE));
    }

    @Override
    public void deleteSave() {
        try {
            Files.deleteIfExists(Paths.get(SAVE_FILE));
        } catch (IOException e) {
            System.err.println("Failed to delete save: " + e.getMessage());
        }
    }

    private GameState parseGameState(String json) {
        String heroId       = extractString(json, "heroId");
        String heroName     = extractString(json, "heroName");
        HeroClass heroClass = HeroClass.valueOf(extractString(json, "heroClass"));

        Hero hero = new Hero(heroId, heroName, heroClass);
        hero.setCurrentHp(extractInt(json, "heroHp"));
        hero.setPosition(extractInt(json, "heroRow"), extractInt(json, "heroCol"));

        String invSection = extractArray(json, "inventory");
        for (String itemJson : splitObjects(invSection)) {
            hero.addItem(new Item(
                    extractString(itemJson, "id"),
                    extractString(itemJson, "name"),
                    ItemType.valueOf(extractString(itemJson, "type")),
                    extractInt(itemJson, "value")
            ));
        }

        int dRows = extractInt(json, "dungeonRows");
        int dCols = extractInt(json, "dungeonCols");
        if (dRows <= 0) dRows = Dungeon.SIZE;
        if (dCols <= 0) dCols = Dungeon.SIZE;
        Dungeon dungeon = new Dungeon(dRows, dCols);

        String roomsSection = extractArray(json, "rooms");
        for (String roomJson : splitObjects(roomsSection)) {
            int r = extractInt(roomJson, "row");
            int c = extractInt(roomJson, "col");
            RoomType rType = RoomType.valueOf(extractString(roomJson, "type"));
            Room room = dungeon.getRoom(r, c);
            room.setType(rType);
            if (extractBool(roomJson, "visited")) room.markVisited();
            if (extractBool(roomJson, "cleared")) room.markCleared();

            for (String eJson : splitObjects(extractArray(roomJson, "enemies"))) {
                Enemy enemy = new Enemy(
                        extractString(eJson, "id"),
                        EnemyType.valueOf(extractString(eJson, "type"))
                );
                enemy.setCurrentHp(extractInt(eJson, "hp"));
                room.addEnemy(enemy);
            }

            for (String iJson : splitObjects(extractArray(roomJson, "items"))) {
                room.addItem(new Item(
                        extractString(iJson, "id"),
                        extractString(iJson, "name"),
                        ItemType.valueOf(extractString(iJson, "type")),
                        extractInt(iJson, "value")
                ));
            }
        }

        GameState state = new GameState(hero, dungeon);
        int dungeonLevel = extractInt(json, "dungeonLevel");
        if (dungeonLevel > 1) state.setDungeonLevel(dungeonLevel);
        if (extractBool(json, "victory"))      state.setVictory();
        else if (extractBool(json, "gameOver")) state.setGameOver();

        return state;
    }

    private String extractString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return "";
        int colon = json.indexOf(':', idx);
        int start = json.indexOf('"', colon + 1) + 1;
        int end   = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private int extractInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 0;
        int colon = json.indexOf(':', idx) + 1;
        int end = colon;
        while (end < json.length() &&
               (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-'))
            end++;
        String val = json.substring(colon, end).trim();
        return val.isEmpty() ? 0 : Integer.parseInt(val);
    }

    private boolean extractBool(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return false;
        int colon = json.indexOf(':', idx) + 1;
        return json.substring(colon).trim().startsWith("true");
    }

    private String extractArray(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return "";
        int start = json.indexOf('[', idx) + 1;
        int depth = 1, i = start;
        while (i < json.length() && depth > 0) {
            char ch = json.charAt(i);
            if (ch == '[') depth++;
            else if (ch == ']') depth--;
            i++;
        }
        return json.substring(start, i - 1);
    }

    private List<String> splitObjects(String arrayContent) {
        List<String> result = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char ch = arrayContent.charAt(i);
            if (ch == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    result.add(arrayContent.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}