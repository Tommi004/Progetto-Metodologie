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
    private final ShopController     shopController;
    private final PersistenceManager persistenceManager;

    private GameState currentState;



    public GameController(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.heroController    = new HeroManager();
        this.dungeonController = new DungeonManager();
        this.combatController  = new CombatManager();
        this.shopController    = new ShopManager(heroController);
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
        Hero hero = heroController.createHero(heroName, heroClass);
        Dungeon dungeon = dungeonController.generateDungeon(1);
        currentState = new GameState(hero, dungeon);
        // Stats start at zero — already default in GameState
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
        // Stats are restored from the save file — do NOT reset them

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
        currentState.addStatDamageDealt(result.heroDamageDealt());
        currentState.addStatDamageTaken(result.enemyDamageDealt());

        if (result.heroDefeated()) {
            currentState.setStatCauseOfDeath("Defeated by " + enemy.getName() + " on floor " + currentState.getDungeonLevel());
            currentState.setGameOver();
        }
        return result;
    }

    public void resolveEnemyDefeat(String enemyId) {
        Room room = getCurrentRoom();
        room.getEnemies().stream()
                .filter(e -> e.getId().equals(enemyId))
                .findFirst()
                .ifPresent(e -> currentState.getHero().addGold(e.getType().getGoldReward()));
        boolean removed = room.removeEnemy(enemyId);
        if (removed) {
            currentState.addStatEnemiesDefeated(1);
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
        if (hpLost > 0) currentState.addStatDamageTaken(hpLost);

        // Check if trap killed the hero
        if (!hero.isAlive()) {
            currentState.setStatCauseOfDeath("Killed by a " + trap.getDisplayName() + " on floor " + currentState.getDungeonLevel());
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


    /**
     * Collects all items from the current room into the hero's inventory.
     *
     * <p>Returns a list of items that could NOT be automatically picked up
     * because adding them would exceed the 2-slot offensive weapon limit.
     * The view is responsible for showing the weapon-swap dialog for these.</p>
     *
     * @return items that require a swap decision from the player
     */
    public List<Item> collectRoomItems() {
        Room room = getCurrentRoom();
        List<Item> items = new java.util.ArrayList<>(room.getItems());
        Hero hero = currentState.getHero();
        List<Item> needsSwap = new java.util.ArrayList<>();

        for (Item item : items) {
            if (item.getType().isOffensiveWeapon()) {
                List<Item> current = getOffensiveWeapons();
                if (current.size() < 2) {
                    // Slot available — pick up directly
                    heroController.pickUpItem(hero, item);
                    room.removeItem(item.getId());
                } else {
                    // Check if same type already owned at lower rarity — auto-replace
                    java.util.Optional<Item> sameType = current.stream()
                            .filter(w -> w.getType() == item.getType()
                                    && item.getRarity().isHigherThan(w.getRarity()))
                            .findFirst();
                    if (sameType.isPresent()) {
                        hero.removeItem(sameType.get().getId());
                        heroController.pickUpItem(hero, item);
                        room.removeItem(item.getId());
                    } else {
                        // Player must choose which weapon to replace (or skip)
                        needsSwap.add(item);
                    }
                }
            } else if (isUniqueEquipment(item.getType())) {
                // Non-offensive unique slots: replace if same type and higher rarity
                hero.getInventory().stream()
                        .filter(i -> i.getType() == item.getType()
                                && item.getRarity().isHigherThan(i.getRarity()))
                        .findFirst()
                        .ifPresent(old -> hero.removeItem(old.getId()));
                // Only add if slot is free or was just freed
                boolean slotFree = hero.getInventory().stream()
                        .noneMatch(i -> i.getType() == item.getType());
                if (slotFree) {
                    heroController.pickUpItem(hero, item);
                    room.removeItem(item.getId());
                }
            } else {
                // Consumables — always pick up
                heroController.pickUpItem(hero, item);
                room.removeItem(item.getId());
            }
        }
        if (room.getItems().isEmpty() && !items.isEmpty()) room.markCleared();
        return needsSwap;
    }

    /** Returns all offensive weapons currently in the hero's inventory. */
    public List<Item> getOffensiveWeapons() {
        return currentState.getHero().getInventory().stream()
                .filter(i -> i.getType().isOffensiveWeapon())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Swaps an existing offensive weapon with a new one from the room.
     * Called by the view when the player chooses which weapon to replace.
     *
     * @param existingId id of the weapon to remove (null = skip pickup)
     * @param newItem    the new weapon to add
     * @param roomItem   whether to remove this item from the current room
     */
    public void swapOffensiveWeapon(String existingId, Item newItem, boolean roomItem) {
        if (existingId == null) return; // player chose to skip
        Hero hero = currentState.getHero();
        hero.removeItem(existingId);
        heroController.pickUpItem(hero, newItem);
        if (roomItem) getCurrentRoom().removeItem(newItem.getId());
        if (getCurrentRoom().getItems().isEmpty()) getCurrentRoom().markCleared();
    }

    private boolean isUniqueEquipment(ItemType type) {
        return switch (type) {
            case STAFF, AMULET, ARMOR, HELMET -> true;
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
            advanceToNextLevel();
            return true;
        } else {
            currentState.markFloorCleared(currentState.getDungeonLevel());
            currentState.setVictory();
            return true;
        }
    }

    public boolean isVictory() {
        return currentState != null && currentState.isVictory();
    }

    /**
     * Returns to the previous dungeon floor if one exists in the history.
     * The hero is placed at (SIZE-1, SIZE-1) — the exit of that floor.
     *
     * @return {@code true} if the retreat succeeded; {@code false} if on floor 1
     */
    public boolean goToPreviousLevel() {
        return currentState.retreatLevel();
    }

    /** Returns {@code true} if the hero can retreat (not on floor 1). */
    public boolean canRetreat() {
        return currentState != null
                && currentState.getDungeonLevel() > 1
                && !currentState.getDungeonHistory().isEmpty();
    }

    private void advanceToNextLevel() {
        // Mark only if this is a first-time advance (not a re-advance from retreat)
        if (!currentState.isOnPreviousLevel()) {
            currentState.markFloorCleared(currentState.getDungeonLevel());
        }
        if (currentState.isOnPreviousLevel()) {
            currentState.advanceToExistingLevel();
        } else {
            int nextLevel = currentState.getDungeonLevel() + 1;
            Dungeon newDungeon = dungeonController.generateDungeon(nextLevel);
            currentState.advanceLevel(newDungeon);
        }
    }

    /**
     * Generates the shop catalogue for the current dungeon floor.
     *
     * @return list of items available for purchase
     */
    public List<ShopItem> getShopCatalogue() {
        return shopController.generateCatalogue(currentState.getDungeonLevel());
    }

    /**
     * Attempts to purchase a shop item for the hero, applying the same
     * slot rules as collectRoomItems (offensive weapon 2-slot limit,
     * unique equipment, rarità superiore only).
     *
     * @param shopItem the shop entry to buy
     * @return {@code null} if purchase failed (insufficient gold);
     *         an empty list if purchased successfully with no swap needed;
     *         a list with the new item if a weapon-swap dialog is required
     */
    public List<Item> buyShopItem(ShopItem shopItem) {
        Hero hero = currentState.getHero();
        if (!hero.spendGold(shopItem.goldCost())) return null;

        Item item = shopItem.item();

        if (item.getType().isOffensiveWeapon()) {
            List<Item> current = getOffensiveWeapons();
            if (current.size() < 2) {
                heroController.pickUpItem(hero, item);
            } else {
                // Check same type at lower rarity — auto-replace
                java.util.Optional<Item> sameType = current.stream()
                        .filter(w -> w.getType() == item.getType()
                                && item.getRarity().isHigherThan(w.getRarity()))
                        .findFirst();
                if (sameType.isPresent()) {
                    hero.removeItem(sameType.get().getId());
                    heroController.pickUpItem(hero, item);
                } else if (current.stream().anyMatch(w -> w.getType() == item.getType())) {
                    // Same type same/lower rarity — refund and reject
                    hero.addGold(shopItem.goldCost());
                    return null;
                } else {
                    // Different types, slots full — need swap dialog
                    return java.util.List.of(item);
                }
            }
        } else if (isUniqueEquipment(item.getType())) {
            java.util.Optional<Item> existing = hero.getInventory().stream()
                    .filter(i -> i.getType() == item.getType())
                    .findFirst();
            if (existing.isPresent()) {
                if (item.getRarity().isHigherThan(existing.get().getRarity())) {
                    hero.removeItem(existing.get().getId());
                    heroController.pickUpItem(hero, item);
                } else {
                    // Same or lower rarity — refund and reject
                    hero.addGold(shopItem.goldCost());
                    return null;
                }
            } else {
                heroController.pickUpItem(hero, item);
            }
        } else {
            heroController.pickUpItem(hero, item);
        }
        return java.util.List.of();
    }

    /**
     * Builds an immutable {@link RunStats} snapshot of the current run.
     * Safe to call at any point during or after the run.
     *
     * @return run statistics snapshot
     */
    public RunStats buildRunStats() {
        int level = currentState != null ? currentState.getHero().getLevel() : 1;
        return new RunStats(
                currentState.getStatEnemiesDefeated(),
                currentState.getStatDamageDealt(),
                currentState.getStatDamageTaken(),
                currentState.getStatDungeonsCleared(),
                level,
                currentState.getStatCauseOfDeath()
        );
    }
}