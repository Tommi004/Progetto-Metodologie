package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.Hero;
import it.unicam.cs.mpgc.rpg126224.model.HeroClass;
import it.unicam.cs.mpgc.rpg126224.model.Item;

/**
 * Defines the contract for hero lifecycle and inventory management.
 *
 * <p>The single implementation is {@code HeroManager}. Keeping the contract
 * behind an interface allows alternative implementations (e.g. a networked
 * hero manager for a future multiplayer extension) to be swapped in without
 * touching the rest of the application.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Creating a fully initialised {@link Hero} from a name and class.</li>
 *   <li>Applying item effects when the hero picks up or uses an item.</li>
 * </ul>
 *
 * <h2>Item effect mapping</h2>
 * <ul>
 *   <li>{@code SWORD}, {@code SPEAR}           → {@code boostAttack}</li>
 *   <li>{@code BOW}                             → {@code boostAttack} + 30% {@code boostMagic}</li>
 *   <li>{@code CROSSBOW}                        → {@code boostAttack} + {@code boostMagic}</li>
 *   <li>{@code STAFF}, {@code AMULET}           → {@code boostMagic}</li>
 *   <li>{@code ARMOR}, {@code HELMET}           → {@code boostDefense}</li>
 *   <li>{@code HEALTH_POTION}                   → {@code heal}</li>
 *   <li>{@code MANA_POTION}                     → {@code restoreMana}</li>
 *   <li>{@code STRENGTH_POTION}                 → temporary ATK boost (cleared after combat)</li>
 * </ul>
 */
public interface HeroController {

    /**
     * Creates and returns a new {@link Hero} with stats initialised according
     * to the chosen {@link HeroClass}.
     *
     * @param name      the player-chosen hero name; must not be null or blank
     * @param heroClass the class that determines base stats and mana pool;
     *                  must not be null
     * @return a fully initialised hero at level 1, positioned at (0, 0)
     */
    Hero createHero(String name, HeroClass heroClass);

    /**
     * Uses the item identified by {@code itemId} from the hero's inventory,
     * applying its effect immediately and removing it from the inventory.
     *
     * <p>Consumable items (potions) are removed after use. Equipment items
     * (weapons, armour) are <em>not</em> removed — their effect is applied
     * once on pick-up via {@link #pickUpItem}.</p>
     *
     * @param hero   the hero using the item; must not be null
     * @param itemId the id of the item to use; must exist in the hero's inventory
     * @return {@code true} if the item was found and its effect applied;
     *         {@code false} if no item with the given id was found
     */
    boolean useItem(Hero hero, String itemId);

    /**
     * Adds the item to the hero's inventory and immediately applies any
     * permanent stat bonus (weapons, armour, amulets).
     *
     * <p>Consumable items (potions) are added to the inventory without
     * applying an effect; they are consumed later via {@link #useItem}.</p>
     *
     * @param hero the hero picking up the item; must not be null
     * @param item the item to add; must not be null
     */
    void pickUpItem(Hero hero, Item item);
}