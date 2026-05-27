package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import it.unicam.cs.mpgc.rpg126224.persistence.PersistenceManager;
import java.util.List;
import java.util.Optional;

/**
 * Central application controller (facade) for Dungeon Protocol.
 */
public class GameController {

    private final HeroController heroController;
    private final DungeonController dungeonController;
    private final CombatController combatController;
    private final PersistenceManager persistenceManager;

    private GameState currentState;

    public GameController(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.heroController = new HeroManager();
        this.dungeonController = new DungeonManager();
        this.combatController = new CombatManager();
    }

    public PersistenceManager getPersistenceManager() { return persistenceManager; }

    public void startNewGame(String heroName, HeroClass heroClass, String gender) {
    Hero hero = heroController.createHero(heroName, heroClass, gender);
    }

    public boolean saveGame() {
        if (currentState == null) return false;
        persistenceManager.saveGame(currentState);
        return true;
    }

    public boolean loadGame() {
        Optional<GameState> loaded = persistenceManager.loadGame();
        loaded.ifPresent(state -> currentState = state);
        return loaded.isPresent();
    }

    public GameState getCurrentState() { return currentState; }
    public Hero getHero() { return currentState.getHero(); }
    public Dungeon getDungeon() { return currentState.getDungeon(); }
    public int getDungeonLevel() { return currentState.getDungeonLevel(); }

    public boolean moveHero(int dRow, int dCol) {
        return dungeonController.moveHero(
                currentState.getHero(),
                currentState.getDungeon(),
                dRow, dCol);
    }

    public Room getCurrentRoom() {
        return dungeonController.getCurrentRoom(
                currentState.getHero(),
                currentState.getDungeon());
    }

    public CombatResult executeCombatTurn(Enemy enemy, CombatAction action) {
        CombatResult result = combatController.executeTurn(
                currentState.getHero(), enemy, action);
        if (result.heroDefeated()) {
            currentState.setGameOver();
        }
        return result;
    }

    public void resolveEnemyDefeat(String enemyId) {
        Room room = getCurrentRoom();
        room.removeEnemy(enemyId);
        if (room.allEnemiesDefeated()) {
            room.markCleared();
        }
    }

    public void collectRoomItems() {
        Room room = getCurrentRoom();
        List<Item> items = room.getItems();
        for (Item item : items) {
            heroController.pickUpItem(currentState.getHero(), item);
            room.removeItem(item.getId());
        }
        if (!items.isEmpty()) room.markCleared();
    }

    public boolean useItem(String itemId) {
        return heroController.useItem(currentState.getHero(), itemId);
    }

    public boolean checkExitCondition() {
        Room room = getCurrentRoom();
        if (room.getType() != RoomType.EXIT) return false;
        if (!room.allEnemiesDefeated()) return false;

        if (currentState.getDungeonLevel() < GameState.MAX_DUNGEON_LEVEL) {
            advanceToNextLevel();
            return true;
        } else {
            currentState.setVictory();
            return true;
        }
    }

    public boolean isVictory() {
        return currentState != null && currentState.isVictory();
    }

    private void advanceToNextLevel() {
        int nextLevel = currentState.getDungeonLevel() + 1;
        Dungeon newDungeon = dungeonController.generateDungeon(nextLevel);
        currentState.advanceLevel(newDungeon);
    }
}