package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import it.unicam.cs.mpgc.rpg126224.persistence.PersistenceManager;
import java.util.List;
import java.util.Optional;

/**
 * Central application controller (facade) for Level Up!.
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

    private String selectedPotionId = null;

    /** Called by the view before USE_POTION to specify which potion to use. */
    public void selectPotion(String itemId) {
        this.selectedPotionId = itemId;
    }

    /** Returns and clears the selected potion id. */
    public String consumeSelectedPotionId() {
        String id = selectedPotionId;
        selectedPotionId = null;
        return id;
    }

    public void startNewGame(String heroName, HeroClass heroClass) {
        dungeonController.resetUniqueItems();
        Hero hero = heroController.createHero(heroName, heroClass);
        Dungeon dungeon = dungeonController.generateDungeon(1);
        currentState = new GameState(hero, dungeon);
    }

    public boolean saveGame() {
        if (currentState == null) return false;
        persistenceManager.saveGame(currentState);
        return true;
    }

    public boolean loadGame() {
        Optional<GameState> loaded = persistenceManager.loadGame();
        if (loaded.isEmpty()) return false;
        currentState = loaded.get();

        // Re-register all existing items so the unique-item tracker is in sync
        dungeonController.resetUniqueItems();
        currentState.getHero().getInventory()
                .forEach(dungeonController::registerExistingItem);
        Dungeon d = currentState.getDungeon();
        for (int r = 0; r < d.getRows(); r++)
            for (int c = 0; c < d.getCols(); c++)
                d.getRoom(r, c).getItems()
                        .forEach(dungeonController::registerExistingItem);

        return true;
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
                currentState.getHero(), enemy, action, consumeSelectedPotionId());
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

    /**
     * Replaces the defeated Demon Lord with a Demon Soul in the current room.
     * Called by the view when the phase-2 transformation is triggered.
     *
     * @return the new Demon Soul enemy
     */
    public Enemy transformToDemonSoul() {
        Room room = getCurrentRoom();
        Enemy soul = new Enemy(java.util.UUID.randomUUID().toString(), EnemyType.DEMON_SOUL);
        room.addEnemy(soul);
        return soul;
    }

    public void collectRoomItems() {
        Room room = getCurrentRoom();
        List<Item> items = room.getItems();
        Hero hero = currentState.getHero();
        for (Item item : items) {
            // If a unique item of the same type exists in inventory, remove it first
            // (upgrade logic: higher rarity replaces lower rarity)
            if (isUniqueItemType(item.getType())) {
                hero.getInventory().stream()
                        .filter(i -> i.getType() == item.getType())
                        .findFirst()
                        .ifPresent(old -> hero.removeItem(old.getId()));
            }
            heroController.pickUpItem(hero, item);
            room.removeItem(item.getId());
        }
        if (!items.isEmpty()) room.markCleared();
    }

    private boolean isUniqueItemType(ItemType type) {
        return switch (type) {
            case SWORD, BOW, STAFF, ARMOR, AMULET, STRENGTH_POTION -> true;
            default -> false;
        };
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