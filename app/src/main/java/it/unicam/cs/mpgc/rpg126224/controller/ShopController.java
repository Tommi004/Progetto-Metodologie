package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.Hero;
import it.unicam.cs.mpgc.rpg126224.model.ShopItem;
import java.util.List;

/**
 * Defines the contract for the dungeon shop system.
 *
 * <p>The shop offers items in exchange for gold earned by defeating enemies.
 * The catalogue is generated per floor and scales in power and price.</p>
 */
public interface ShopController {

    /**
     * Generates a catalogue of items available for purchase at the given
     * dungeon level.
     *
     * @param level the current dungeon floor (1–5)
     * @return an immutable list of {@link ShopItem} entries
     */
    List<ShopItem> generateCatalogue(int level);

    /**
     * Attempts to purchase the given shop item for the hero.
     *
     * @param hero     the hero attempting the purchase; must not be null
     * @param shopItem the shop entry to purchase; must not be null
     * @return {@code true} if the purchase succeeded; {@code false} if
     *         the hero had insufficient gold
     */
    boolean buyItem(Hero hero, ShopItem shopItem);
}