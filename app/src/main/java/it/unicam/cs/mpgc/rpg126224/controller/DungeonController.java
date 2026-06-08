package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;

/**
 * Defines the contract for generating and navigating the dungeon grid.
 *
 * <p>The dungeon is an 8×8 grid of {@link Room} objects. Each floor (1–5) is
 * independently generated with a fixed layout: 18 enemy rooms, 8 treasure rooms,
 * 4 trap rooms, a START room at (0,0) and an EXIT room at (7,7) containing the
 * floor boss.</p>
 *
 * <h2>Unique-item tracking</h2>
 * <p>Equipment items (weapons, armour) are unique per run: each type can appear
 * at most once. A higher-rarity copy of the same type <em>replaces</em> the
 * existing one. The tracker is maintained across floor transitions and must be
 * reset only when a brand-new game starts ({@link #resetUniqueItems}). After
 * loading a saved game, all existing items must be re-registered via
 * {@link #registerExistingItem} to re-align the tracker.</p>
 *
 * <h2>Extension</h2>
 * <p>Alternative dungeon generators (e.g. procedurally generated layouts,
 * infinite floors) can be introduced by implementing this interface without
 * modifying {@code GameController} or the view layer.</p>
 */
public interface DungeonController {

    /**
     * Generates a new dungeon floor for the given level.
     *
     * <p>The generated dungeon always has a {@link RoomType#START} room at (0,0)
     * (pre-visited and cleared) and a {@link RoomType#EXIT} room at (7,7)
     * containing the floor boss. Enemy pool, trap types and item rarity
     * probabilities scale with {@code level}.</p>
     *
     * @param level the dungeon floor number (1–{@code GameState.MAX_DUNGEON_LEVEL})
     * @return a freshly generated, fully populated {@link Dungeon}
     */
    Dungeon generateDungeon(int level);

    /**
     * Moves the hero one step in the given direction if the destination is valid.
     *
     * <p>The hero's position is updated and the destination room is marked as
     * visited. The move is rejected (returns {@code false}) if the destination
     * falls outside the dungeon bounds.</p>
     *
     * @param hero   the hero to move; must not be null
     * @param dungeon the dungeon the hero is navigating; must not be null
     * @param dRow   row delta: {@code -1} (up), {@code 0}, or {@code +1} (down)
     * @param dCol   column delta: {@code -1} (left), {@code 0}, or {@code +1} (right)
     * @return {@code true} if the move succeeded; {@code false} if out of bounds
     */
    boolean moveHero(Hero hero, Dungeon dungeon, int dRow, int dCol);

    /**
     * Returns the room the hero is currently occupying.
     *
     * @param hero    the hero whose position is used; must not be null
     * @param dungeon the dungeon containing the room; must not be null
     * @return the {@link Room} at the hero's current (row, col) position
     */
    Room getCurrentRoom(Hero hero, Dungeon dungeon);

    /**
     * Resets the unique-item tracker, allowing all item types to appear again.
     *
     * <p><strong>Must be called only when starting a brand-new run.</strong>
     * Calling this mid-run would allow duplicate unique items to be generated.</p>
     */
    void resetUniqueItems();

    /**
     * Registers an existing item with the unique-item tracker without generating
     * a new item.
     *
     * <p>Must be called after {@link #resetUniqueItems()} for every item found
     * in the loaded {@link it.unicam.cs.mpgc.rpg126224.model.GameState} (both in
     * the hero's inventory and in dungeon rooms) so that the tracker reflects the
     * true state of the run.</p>
     *
     * @param item the item to register; non-unique types (potions) are silently ignored
     */
    void registerExistingItem(Item item);
}