package it.unicam.cs.mpgc.rpg126224.model;

import java.util.Objects;

/**
 * Represents the complete state of a Level Up! game session.
 */
public class GameState {

    public static final int MAX_DUNGEON_LEVEL = 5;

    private final Hero hero;
    private Dungeon dungeon;
    private int dungeonLevel;
    private boolean gameOver;
    private boolean victory;

    public GameState(Hero hero, Dungeon dungeon) {
        this.hero = Objects.requireNonNull(hero, "hero cannot be null");
        this.dungeon = Objects.requireNonNull(dungeon, "dungeon cannot be null");
        this.dungeonLevel = 1;
        this.gameOver = false;
        this.victory = false;
    }

    public Hero getHero() { return hero; }
    public Dungeon getDungeon() { return dungeon; }
    public int getDungeonLevel() { return dungeonLevel; }
    public boolean isGameOver() { return gameOver; }
    public boolean isVictory() { return victory; }

    public void advanceLevel(Dungeon newDungeon) {
        this.dungeon = Objects.requireNonNull(newDungeon);
        this.dungeonLevel++;
        hero.setPosition(0, 0);
    }

    public void setDungeonLevel(int level) { this.dungeonLevel = level; }
    public void setDungeon(Dungeon dungeon) {
        this.dungeon = Objects.requireNonNull(dungeon);
    }

    public void setGameOver() {
        this.gameOver = true;
        this.victory = false;
    }

    public void setVictory() {
        this.gameOver = true;
        this.victory = true;
    }

    @Override
    public String toString() {
        return "GameState{hero=" + hero.getName() +
                ", level=" + dungeonLevel +
                ", gameOver=" + gameOver +
                ", victory=" + victory + "}";
    }
}