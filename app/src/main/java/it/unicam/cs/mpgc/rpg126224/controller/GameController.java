package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import it.unicam.cs.mpgc.rpg126224.persistence.PersistenceManager;
import java.util.List;
import java.util.Optional;

/**
 * Central application controller (facade) for Level Up!.
 *
 * <p>Besides coordinating hero, dungeon and combat sub-controllers, this class
 * accumulates run-wide statistics ({@link RunStats}) so the game-over and
 * victory dialogs can present a detailed summary to the player.</p>
 */
public class GameController {

    private final HeroController     heroController;
    private final DungeonController  dungeonController;
    private final CombatController   combatController;
    private final PersistenceManager persistenceManager;

    private GameState currentState;

    // ------------------------------------------------------------------
    // Run-statistics accumulators (reset on startNewGame / loadGame)
    // ------------------------------------------------------------------
    private int    statEnemiesDefeated  = 0;
    private int    statDamageDealt      = 0;
    private int    statDamageTaken      = 0;
    private int    statDungeonsCleared  = 0;
    private String statCauseOfDeath     = null;

    public GameController(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.heroController    = new HeroManager();
        this.dungeonController = new DungeonManager();
        this.combatController  = new CombatManager();
    }

    public PersistenceManager getPersistenceManager() { return persistenceManager; }

    // ------------------------------------------------------------------
    // Potion selection (set by the view before USE_POTION)
    // ------------------------------------------------------------------

    private String selectedPotionId = null;

    /** Called by the view before USE_POTION to specify which potion to use. */
    public void selectPotion(String itemId) { this.selectedPotionId = itemId; }

    /** Returns and clears the selected potion id. */
    public String consumeSelectedPotionId() {
        String id = selectedPotionId;
        selectedPotionId = null;
        return id;
    }

    // ------------------------------------------------------------------
    // Game lifecycle
    // ------------------------------------------------------------------

    public void startNewGame(String heroName, HeroClass heroClass) {
        dungeonController.resetUniqueItems();
        resetRunStats();
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
        resetRunStats();

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

    // ------------------------------------------------------------------
    // State accessors
    // ------------------------------------------------------------------

    public GameState getCurrentState() { return currentState; }
    public Hero      getHero()         { return currentState.getHero(); }
    public Dungeon   getDungeon()      { return currentState.getDungeon(); }
    public int       getDungeonLevel() { return currentState.getDungeonLevel(); }

    // ------------------------------------------------------------------
    // Movement
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Combat
    // ------------------------------------------------------------------

    /**
     * Executes one combat turn, updates run statistics, and marks game-over
     * if the hero is defeated.
     *
     * @param enemy  the enemy being fought
     * @param action the action chosen by the player
     * @return the result of the turn
     */
    public CombatResult executeCombatTurn(Enemy enemy, CombatAction action) {
        CombatResult result = combatController.executeTurn(
                currentState.getHero(), enemy, action, consumeSelectedPotionId());

        // Accumulate stats
        statDamageDealt += result.heroDamageDealt();
        statDamageTaken += result.enemyDamageDealt();

        if (result.heroDefeated()) {
            statCauseOfDeath = "Defeated by " + enemy.getName()
                    + " on floor " + currentState.getDungeonLevel();
            currentState.setGameOver();
        }
        return result;
    }

    public void resolveEnemyDefeat(String enemyId) {
        Room room = getCurrentRoom();
        boolean removed = room.removeEnemy(enemyId);
        if (removed) {
            statEnemiesDefeated++;
        }
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

    // ------------------------------------------------------------------
    // Trap handling
    // ------------------------------------------------------------------

    private boolean atkDebuffActive = false;
    private int     atkDebuffAmount = 0;

    /**
     * Triggers the trap in the current room if present.
     *
     * @return the trap that was triggered, or {@code null} if no trap
     */
    public TrapType triggerTrap() {
        Room room = getCurrentRoom();
        if (!room.hasTrap()) return null;
        TrapType trap = room.getTrap();
        Hero hero = currentState.getHero();

        int hpBefore = hero.getCurrentHp();

        if (trap.getHpDamage() > 0) {
            hero.setCurrentHp(Math.max(0, hero.getCurrentHp() - trap.getHpDamage()));
        }
        if (trap.getMpDamage() > 0) {
            hero.setCurrentMana(Math.max(0, hero.getCurrentMana() - trap.getMpDamage()));
        }
        if (trap.hasAtkDebuff()) {
            atkDebuffAmount = hero.getAttack() / 2;
            atkDebuffActive = true;
            hero.boostAttack(-atkDebuffAmount);
        }

        // Accumulate trap damage in run stats
        int hpLost = hpBefore - hero.getCurrentHp();
        if (hpLost > 0) statDamageTaken += hpLost;

        // Check if trap killed the hero
        if (!hero.isAlive()) {
            statCauseOfDeath = "Killed by a " + trap.getDisplayName()
                    + " on floor " + currentState.getDungeonLevel();
            currentState.setGameOver();
        }

        room.disarmTrap();
        room.markCleared();
        return trap;
    }

    public boolean isAtkDebuffActive() { return atkDebuffActive; }

    /** Called at the end of combat to restore ATK if a Hex Mark was active. */
    public void clearAtkDebuff() {
        if (atkDebuffActive) {
            currentState.getHero().boostAttack(atkDebuffAmount);
            atkDebuffActive = false;
            atkDebuffAmount = 0;
        }
    }

    // ------------------------------------------------------------------
    // Item handling
    // ------------------------------------------------------------------

    public void collectRoomItems() {
        Room room = getCurrentRoom();
        List<Item> items = room.getItems();
        Hero hero = currentState.getHero();
        for (Item item : items) {
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

    // ------------------------------------------------------------------
    // Exit / level progression
    // ------------------------------------------------------------------

    public boolean checkExitCondition() {
        Room room = getCurrentRoom();
        if (room.getType() != RoomType.EXIT) return false;
        if (!room.allEnemiesDefeated()) return false;

        if (currentState.getDungeonLevel() < GameState.MAX_DUNGEON_LEVEL) {
            advanceToNextLevel();   // statDungeonsCleared incremented inside
            return true;
        } else {
            statDungeonsCleared++;  // last floor: count here before setVictory
            currentState.setVictory();
            return true;
        }
    }

    public boolean isVictory() {
        return currentState != null && currentState.isVictory();
    }

    private void advanceToNextLevel() {
        statDungeonsCleared++;
        int nextLevel = currentState.getDungeonLevel() + 1;
        Dungeon newDungeon = dungeonController.generateDungeon(nextLevel);
        currentState.advanceLevel(newDungeon);
    }

    // ------------------------------------------------------------------
    // Run statistics
    // ------------------------------------------------------------------

    /**
     * Builds an immutable {@link RunStats} snapshot of the current run.
     * Safe to call at any point during or after the run.
     *
     * @return run statistics snapshot
     */
    public RunStats buildRunStats() {
        int level = currentState != null ? currentState.getHero().getLevel() : 1;
        return new RunStats(
                statEnemiesDefeated,
                statDamageDealt,
                statDamageTaken,
                statDungeonsCleared,
                level,
                statCauseOfDeath
        );
    }

    private void resetRunStats() {
        statEnemiesDefeated = 0;
        statDamageDealt     = 0;
        statDamageTaken     = 0;
        statDungeonsCleared = 0;
        statCauseOfDeath    = null;
    }
}