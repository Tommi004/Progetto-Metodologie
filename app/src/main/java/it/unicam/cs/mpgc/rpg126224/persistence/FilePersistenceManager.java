package it.unicam.cs.mpgc.rpg126224.persistence;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Implementazione di {@link PersistenceManager} che salva lo stato del gioco
 * in un file JSON testuale ({@value #SAVE_FILE}).
 *
 * <p>Il JSON viene costruito e letto manualmente tramite i metodi privati
 * {@code buildJson} e {@code parseJson}, senza dipendenze esterne.
 * Il formato scelto è:</p>
 * <pre>
 * {
 *   "heroName": "Aldric",
 *   "heroClass": "WARRIOR",
 *   "inventory": [
 *     { "id": "...", "name": "Sword", "type": "SWORD", "value": 12, "rarity": "RARE", "quantity": 1 }
 *   ],
 *   ...
 * }
 * </pre>
 */
public class FilePersistenceManager implements PersistenceManager {

    private static final String SAVE_DIR  = "save";
    private static final String SAVE_FILE = "save/level_up.json";

    // -------------------------------------------------------------------------
    // PersistenceManager implementation
    // -------------------------------------------------------------------------

    @Override
    public void saveGame(GameState state) {
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
            String json = serialize(state);
            Files.writeString(Paths.get(SAVE_FILE), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save game: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<GameState> loadGame() {
        try {
            if (!hasSaveFile()) return Optional.empty();
            String json = Files.readString(Paths.get(SAVE_FILE), StandardCharsets.UTF_8);
            return Optional.of(deserialize(json));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read save file: " + e.getMessage(), e);
        } catch (Exception e) {
            // Save file exists but is corrupted — return empty
            return Optional.empty();
        }
    }

    @Override
    public boolean hasSaveFile() {
        return Files.exists(Paths.get(SAVE_FILE));
    }

    @Override
    public void deleteSave() {
        try { Files.deleteIfExists(Paths.get(SAVE_FILE)); }
        catch (IOException e) { /* best-effort — ignore if already gone */ }
    }

    // =========================================================================
    // Serialization
    // =========================================================================

    private String serialize(GameState state) {
        JsonObject root = new JsonObject();
        Hero hero = state.getHero();

        // Hero base fields
        root.put("heroId",      hero.getId());
        root.put("heroName",    hero.getName());
        root.put("heroClass",   hero.getHeroClass().name());
        root.put("heroHp",      hero.getCurrentHp());
        root.put("heroMaxHp",   hero.getMaxHp());
        root.put("heroMana",    hero.getCurrentMana());
        root.put("heroMaxMana", hero.getMaxMana());
        root.put("heroAtk",     hero.getAttack());
        root.put("heroDef",     hero.getDefense());
        root.put("heroMag",     hero.getMagic());
        root.put("heroLevel",   hero.getLevel());
        root.put("heroXp",      hero.getExperience());
        root.put("heroRow",     hero.getRow());
        root.put("heroCol",     hero.getCol());
        root.put("heroGold",    hero.getGold());

        // Inventory
        JsonArray inventory = new JsonArray();
        for (Item item : hero.getInventory()) {
            JsonObject obj = new JsonObject();
            obj.put("id",       item.getId());
            obj.put("name",     item.getName());
            obj.put("type",     item.getType().name());
            obj.put("value",    item.getValue());
            obj.put("rarity",   item.getRarity().name());
            obj.put("quantity", item.getQuantity());
            inventory.add(obj);
        }
        root.put("inventory", inventory);

        // Session state
        root.put("dungeonLevel",    state.getDungeonLevel());
        root.put("maxLevelReached", state.getMaxLevelReached());
        root.put("gameOver",        state.isGameOver());
        root.put("victory",         state.isVictory());

        // Run statistics
        root.put("statEnemiesDefeated", state.getStatEnemiesDefeated());
        root.put("statDamageDealt",     state.getStatDamageDealt());
        root.put("statDamageTaken",     state.getStatDamageTaken());
        root.put("statDungeonsCleared", state.getStatDungeonsCleared());

        // Current dungeon
        root.put("dungeon", serializeDungeon(state.getDungeon()));

        // Dungeon history (floors below current)
        JsonArray history = new JsonArray();
        for (Dungeon d : state.getDungeonHistory()) history.add(serializeDungeon(d));
        root.put("dungeonHistory", history);

        // Future history (floors above current, already visited)
        JsonArray future = new JsonArray();
        for (Dungeon d : state.getFutureHistory()) future.add(serializeDungeon(d));
        root.put("futureHistory", future);

        return root.toJson(0);
    }

    private JsonObject serializeDungeon(Dungeon dungeon) {
        JsonObject obj = new JsonObject();
        obj.put("rows", dungeon.getRows());
        obj.put("cols", dungeon.getCols());

        // Walls — stored as flat arrays for compactness
        JsonArray wallRight = new JsonArray();
        JsonArray wallDown  = new JsonArray();
        for (int r = 0; r < dungeon.getRows(); r++) {
            for (int c = 0; c < dungeon.getCols(); c++) {
                wallRight.add(dungeon.hasWallRight(r, c));
                wallDown.add(dungeon.hasWallDown(r, c));
            }
        }
        obj.put("wallRight", wallRight);
        obj.put("wallDown",  wallDown);

        // Rooms
        JsonArray rooms = new JsonArray();
        for (int r = 0; r < dungeon.getRows(); r++) {
            for (int c = 0; c < dungeon.getCols(); c++) {
                rooms.add(serializeRoom(dungeon.getRoom(r, c)));
            }
        }
        obj.put("rooms", rooms);
        return obj;
    }

    private JsonObject serializeRoom(Room room) {
        JsonObject obj = new JsonObject();
        obj.put("type",    room.getType().name());
        obj.put("visited", room.isVisited());
        obj.put("cleared", room.isCleared());
        if (room.hasTrap()) obj.put("trap", room.getTrap().name());

        JsonArray enemies = new JsonArray();
        for (Enemy e : room.getEnemies()) {
            JsonObject eo = new JsonObject();
            eo.put("id",   e.getId());
            eo.put("type", e.getType().name());
            eo.put("hp",   e.getCurrentHp());
            enemies.add(eo);
        }
        obj.put("enemies", enemies);

        JsonArray items = new JsonArray();
        for (Item i : room.getItems()) {
            JsonObject io = new JsonObject();
            io.put("id",     i.getId());
            io.put("name",   i.getName());
            io.put("type",   i.getType().name());
            io.put("value",  i.getValue());
            io.put("rarity", i.getRarity().name());
            items.add(io);
        }
        obj.put("items", items);
        return obj;
    }

    // =========================================================================
    // Deserialization
    // =========================================================================

    private GameState deserialize(String json) {
        JsonObject root = JsonObject.parse(json);

        // Hero
        String   heroId    = root.getString("heroId");
        String   heroName  = root.getString("heroName");
        HeroClass heroClass = HeroClass.valueOf(root.getString("heroClass"));
        Hero hero = new Hero(heroId, heroName, heroClass);
        hero.setCurrentHp(root.getInt("heroHp"));
        hero.setMaxHp(root.getInt("heroMaxHp"));
        hero.setCurrentMana(root.getInt("heroMana"));
        hero.setMaxMana(root.getInt("heroMaxMana"));
        // Restore stats by boosting from the hero's base class values to the saved values
        hero.boostAttack(root.getInt("heroAtk")  - hero.getAttack());
        hero.boostDefense(root.getInt("heroDef") - hero.getDefense());
        hero.boostMagic(root.getInt("heroMag")   - hero.getMagic());
        hero.setLevel(root.getInt("heroLevel"));
        hero.setExperience(root.getInt("heroXp"));
        hero.setPosition(root.getInt("heroRow"), root.getInt("heroCol"));
        hero.setGold(root.getInt("heroGold"));

        // Inventory
        for (JsonObject io : root.getArray("inventory").objects()) {
            Item item = new Item(
                    io.getString("id"),
                    io.getString("name"),
                    ItemType.valueOf(io.getString("type")),
                    io.getInt("value"),
                    Rarity.valueOf(io.getString("rarity")));
            int qty = io.getInt("quantity");
            if (qty > 1) item.setQuantity(qty);
            hero.addItem(item);
        }

        // Dungeon
        Dungeon dungeon = parseDungeon(root.getObject("dungeon"));
        GameState state = new GameState(hero, dungeon);

        state.setDungeonLevel(root.getInt("dungeonLevel"));
        state.setMaxLevelReached(root.getInt("maxLevelReached"));
        if (root.getBool("victory"))       state.setVictory();
        else if (root.getBool("gameOver")) state.setGameOver();

        state.setStatEnemiesDefeated(root.getInt("statEnemiesDefeated"));
        state.setStatDamageDealt(root.getInt("statDamageDealt"));
        state.setStatDamageTaken(root.getInt("statDamageTaken"));
        state.setStatDungeonsCleared(root.getInt("statDungeonsCleared"));

        // Dungeon history
        List<Dungeon> history = new ArrayList<>();
        for (JsonObject d : root.getArray("dungeonHistory").objects()) history.add(parseDungeon(d));
        state.setDungeonHistory(history);

        List<Dungeon> future = new ArrayList<>();
        for (JsonObject d : root.getArray("futureHistory").objects()) future.add(parseDungeon(d));
        state.setFutureHistory(future);

        return state;
    }

    private Dungeon parseDungeon(JsonObject obj) {
        int rows = obj.getInt("rows");
        int cols = obj.getInt("cols");
        Dungeon dungeon = new Dungeon(rows, cols);

        // Restore walls
        List<Boolean> wr = obj.getArray("wallRight").booleans();
        List<Boolean> wd = obj.getArray("wallDown").booleans();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                if (!wr.get(idx)) dungeon.removeWallRight(r, c);
                if (!wd.get(idx)) dungeon.removeWallDown(r, c);
            }
        }

        // Restore rooms
        List<JsonObject> roomList = obj.getArray("rooms").objects();
        int i = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                parseRoom(dungeon.getRoom(r, c), roomList.get(i++));
            }
        }
        return dungeon;
    }

    private void parseRoom(Room room, JsonObject obj) {
        room.setType(RoomType.valueOf(obj.getString("type")));
        if (obj.getBool("visited")) room.markVisited();
        if (obj.getBool("cleared")) room.markCleared();
        if (obj.has("trap")) room.setTrap(TrapType.valueOf(obj.getString("trap")));

        for (JsonObject eo : obj.getArray("enemies").objects()) {
            Enemy enemy = new Enemy(eo.getString("id"), EnemyType.valueOf(eo.getString("type")));
            int hp = eo.getInt("hp");
            if (hp > 0) enemy.setCurrentHp(hp);
            room.addEnemy(enemy);
        }
        for (JsonObject io : obj.getArray("items").objects()) {
            room.addItem(new Item(
                    io.getString("id"),
                    io.getString("name"),
                    ItemType.valueOf(io.getString("type")),
                    io.getInt("value"),
                    Rarity.valueOf(io.getString("rarity"))));
        }
    }

    // =========================================================================
    // Minimal JSON builder — no external dependencies
    // =========================================================================

    /** Minimal mutable JSON object (key → value map, preserves insertion order). */
    private static class JsonObject {
        private final Map<String, Object> map = new LinkedHashMap<>();

        void put(String key, Object value)   { map.put(key, value); }
        String  getString(String key)        { return (String) map.get(key); }
        int     getInt(String key)           { Object v = map.get(key); return v instanceof Number n ? n.intValue() : 0; }
        boolean getBool(String key)          { Object v = map.get(key); return Boolean.TRUE.equals(v); }
        boolean has(String key)              { return map.containsKey(key); }
        JsonObject getObject(String key)     { return (JsonObject) map.get(key); }
        JsonArray  getArray(String key)      { return (JsonArray)  map.get(key); }

        String toJson(int indent) {
            String pad  = "  ".repeat(indent);
            String pad2 = "  ".repeat(indent + 1);
            StringBuilder sb = new StringBuilder("{\n");
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> e = it.next();
                sb.append(pad2).append('"').append(e.getKey()).append("\": ");
                sb.append(valueToJson(e.getValue(), indent + 1));
                if (it.hasNext()) sb.append(',');
                sb.append('\n');
            }
            sb.append(pad).append('}');
            return sb.toString();
        }

        /** Parses a JSON object string into a JsonObject. */
        static JsonObject parse(String json) {
            return (JsonObject) parseValue(json.trim(), new int[]{0});
        }
    }

    /** Minimal mutable JSON array. */
    private static class JsonArray {
        private final List<Object> list = new ArrayList<>();

        void add(Object value)              { list.add(value); }
        List<JsonObject> objects()          { return list.stream().map(o -> (JsonObject) o).toList(); }
        List<Boolean>    booleans()         { return list.stream().map(o -> Boolean.TRUE.equals(o)).toList(); }

        String toJson(int indent) {
            if (list.isEmpty()) return "[]";
            String pad  = "  ".repeat(indent);
            String pad2 = "  ".repeat(indent + 1);
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append(pad2).append(valueToJson(list.get(i), indent + 1));
                if (i < list.size() - 1) sb.append(',');
                sb.append('\n');
            }
            sb.append(pad).append(']');
            return sb.toString();
        }
    }

    private static String valueToJson(Object value, int indent) {
        if (value == null)               return "null";
        if (value instanceof String s)   return '"' + s.replace("\\","\\\\").replace("\"","\\\"") + '"';
        if (value instanceof Boolean b)  return b.toString();
        if (value instanceof Number n)   return n.toString();
        if (value instanceof JsonObject jo) return jo.toJson(indent);
        if (value instanceof JsonArray  ja) return ja.toJson(indent);
        return "\"" + value + "\"";
    }

    // -------------------------------------------------------------------------
    // Minimal JSON parser
    // -------------------------------------------------------------------------

    private static Object parseValue(String json, int[] pos) {
        skipWhitespace(json, pos);
        char ch = json.charAt(pos[0]);
        if (ch == '{')    return parseObject(json, pos);
        if (ch == '[')    return parseArray(json, pos);
        if (ch == '"')    return parseString(json, pos);
        if (ch == 't' || ch == 'f') return parseBoolean(json, pos);
        if (ch == 'n')  { pos[0] += 4; return null; }
        return parseNumber(json, pos);
    }

    private static JsonObject parseObject(String json, int[] pos) {
        JsonObject obj = new JsonObject();
        pos[0]++; // skip '{'
        skipWhitespace(json, pos);
        while (json.charAt(pos[0]) != '}') {
            skipWhitespace(json, pos);
            String key = parseString(json, pos);
            skipWhitespace(json, pos);
            pos[0]++; // skip ':'
            Object value = parseValue(json, pos);
            obj.put(key, value);
            skipWhitespace(json, pos);
            if (json.charAt(pos[0]) == ',') pos[0]++;
            skipWhitespace(json, pos);
        }
        pos[0]++; // skip '}'
        return obj;
    }

    private static JsonArray parseArray(String json, int[] pos) {
        JsonArray arr = new JsonArray();
        pos[0]++; // skip '['
        skipWhitespace(json, pos);
        while (json.charAt(pos[0]) != ']') {
            arr.add(parseValue(json, pos));
            skipWhitespace(json, pos);
            if (json.charAt(pos[0]) == ',') pos[0]++;
            skipWhitespace(json, pos);
        }
        pos[0]++; // skip ']'
        return arr;
    }

    private static String parseString(String json, int[] pos) {
        pos[0]++; // skip '"'
        StringBuilder sb = new StringBuilder();
        while (json.charAt(pos[0]) != '"') {
            char c = json.charAt(pos[0]++);
            if (c == '\\') {
                char esc = json.charAt(pos[0]++);
                sb.append(esc == 'n' ? '\n' : esc == 't' ? '\t' : esc);
            } else {
                sb.append(c);
            }
        }
        pos[0]++; // skip closing '"'
        return sb.toString();
    }

    private static boolean parseBoolean(String json, int[] pos) {
        if (json.startsWith("true",  pos[0])) { pos[0] += 4; return true; }
        pos[0] += 5; return false;
    }

    private static Number parseNumber(String json, int[] pos) {
        int start = pos[0];
        while (pos[0] < json.length() && "0123456789-+.eE".indexOf(json.charAt(pos[0])) >= 0) pos[0]++;
        String num = json.substring(start, pos[0]);
        return num.contains(".") ? Double.parseDouble(num) : Long.parseLong(num);
    }

    private static void skipWhitespace(String json, int[] pos) {
        while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) pos[0]++;
    }
}