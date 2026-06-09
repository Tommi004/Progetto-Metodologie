package it.unicam.cs.mpgc.rpg126224.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the complete state of a Level Up! game session.
 *
 * <p>Maintains a history of all previously completed dungeon floors so the
 * hero can travel back to earlier levels freely after conquering them.</p>
 */
public class GameState {

    public static final int MAX_DUNGEON_LEVEL = 5;

    private final Hero         hero;
    private Dungeon            dungeon;
    private int                dungeonLevel;
    private boolean            gameOver;
    private boolean            victory;

    /**
     * Ordered list of completed dungeon floors.
     * Index 0 = floor 1, index 1 = floor 2, etc.
     * The current floor is NOT in the history — it is stored in {@code dungeon}.
     */
    private final List<Dungeon> dungeonHistory = new ArrayList<>();

    /**
     * Dungeons for floors ABOVE the current one that have already been visited.
     * When the hero retreats from floor N to N-1, floor N is pushed here.
     * When the hero re-advances, floor N is popped from here instead of regenerated.
     */
    private final List<Dungeon> futureHistory = new ArrayList<>();

    /** The highest floor ever reached in this run. */
    private int maxLevelReached = 1;

    // ------------------------------------------------------------------
    // Run statistics (persisted across save/load)
    // ------------------------------------------------------------------
    private int statEnemiesDefeated = 0;
    private int statDamageDealt     = 0;
    private int statDamageTaken     = 0;
    private String statCauseOfDeath = null;

    /**
     * Tracks which floor numbers have been cleared for the first time.
     * Using a Set prevents double-counting when the hero retreats and re-advances.
     */
    private final java.util.Set<Integer> clearedFloors = new java.util.HashSet<>();

    public GameState(Hero hero, Dungeon dungeon) {
        this.hero         = Objects.requireNonNull(hero,    "hero cannot be null");
        this.dungeon      = Objects.requireNonNull(dungeon, "dungeon cannot be null");
        this.dungeonLevel = 1;
        this.gameOver     = false;
        this.victory      = false;
    }

    public Hero    getHero()         { return hero; }
    public Dungeon getDungeon()      { return dungeon; }
    public int     getDungeonLevel() { return dungeonLevel; }
    public boolean isGameOver()      { return gameOver; }
    public boolean isVictory()       { return victory; }

    /**
     * Returns an unmodifiable view of the dungeon history.
     * Index 0 corresponds to floor 1.
     */
    public List<Dungeon> getDungeonHistory() {
        return Collections.unmodifiableList(dungeonHistory);
    }

    /**
     * Advances to the next dungeon floor.
     * The current dungeon is pushed onto the history stack.
     *
     * @param newDungeon the newly generated floor
     */
    public void advanceLevel(Dungeon newDungeon) {
        dungeonHistory.add(dungeon);
        this.dungeon = Objects.requireNonNull(newDungeon);
        this.dungeonLevel++;
        if (dungeonLevel > maxLevelReached) maxLevelReached = dungeonLevel;
        hero.setPosition(0, 0);
    }

    /**
     * Returns to the previous dungeon floor.
     * The hero is placed at (SIZE-1, SIZE-1) — the exit of the lower floor.
     *
     * @return {@code true} if successful; {@code false} if already on floor 1
     */
    public boolean retreatLevel() {
        if (dungeonHistory.isEmpty()) return false;
        futureHistory.add(0, dungeon);        // push current floor to future
        this.dungeon = dungeonHistory.remove(dungeonHistory.size() - 1);
        this.dungeonLevel--;
        hero.setPosition(Dungeon.SIZE - 1, Dungeon.SIZE - 1);
        return true;
    }

    /**
     * Returns {@code true} if the hero is currently on a floor lower than the
     * highest floor ever reached — meaning there is already a saved dungeon
     * for the next level in the history "ahead".
     */
    public boolean isOnPreviousLevel() {
        return dungeonLevel < maxLevelReached && !futureHistory.isEmpty();
    }

    /**
     * Advances to the next already-visited floor instead of generating a new one.
     * Only call when {@link #isOnPreviousLevel()} is {@code true}.
     */
    public void advanceToExistingLevel() {
        dungeonHistory.add(dungeon);
        this.dungeon = futureHistory.remove(0);
        this.dungeonLevel++;
        hero.setPosition(0, 0);
    }

    public List<Dungeon> getFutureHistory()   { return Collections.unmodifiableList(futureHistory); }
    public void setFutureHistory(List<Dungeon> list) {
        futureHistory.clear();
        futureHistory.addAll(list);
    }

    public int  getMaxLevelReached()        { return maxLevelReached; }
    public void setMaxLevelReached(int max) { this.maxLevelReached = max; }

    public int  getStatEnemiesDefeated()    { return statEnemiesDefeated; }
    public int  getStatDamageDealt()        { return statDamageDealt; }
    public int  getStatDamageTaken()        { return statDamageTaken; }
    public int  getStatDungeonsCleared()    { return clearedFloors.size(); }
    public String getStatCauseOfDeath()     { return statCauseOfDeath; }

    public void addStatEnemiesDefeated(int n) { statEnemiesDefeated += n; }
    public void addStatDamageDealt(int n)     { statDamageDealt += n; }
    public void addStatDamageTaken(int n)     { statDamageTaken += n; }
    /** Marks the given floor number as cleared (idempotent). */
    public void markFloorCleared(int floorNumber) { clearedFloors.add(floorNumber); }
    public void setStatCauseOfDeath(String s) { statCauseOfDeath = s; }

    public void setStatEnemiesDefeated(int n) { statEnemiesDefeated = n; }
    public void setStatDamageDealt(int n)     { statDamageDealt = n; }
    public void setStatDamageTaken(int n)     { statDamageTaken = n; }
    public void setStatDungeonsCleared(int n) {
        clearedFloors.clear();
        for (int i = 1; i <= n; i++) clearedFloors.add(i);
    }
    public void addStatDungeonsCleared(int n) { /* replaced by markFloorCleared */ }

    /**
     * Directly replaces the current dungeon (used by persistence layer).
     */
    public void setDungeon(Dungeon dungeon) {
        this.dungeon = Objects.requireNonNull(dungeon);
    }

    public void setDungeonLevel(int level) { this.dungeonLevel = level; }

    /**
     * Replaces the entire dungeon history (used by persistence layer).
     */
    public void setDungeonHistory(List<Dungeon> history) {
        dungeonHistory.clear();
        dungeonHistory.addAll(history);
    }

    public void setGameOver() { this.gameOver = true; this.victory = false; }
    public void setVictory()  { this.gameOver = true; this.victory = true; }

    @Override
    public String toString() {
        return "GameState{hero=" + hero.getName() +
                ", level=" + dungeonLevel +
                ", gameOver=" + gameOver +
                ", victory=" + victory + "}";
    }
}