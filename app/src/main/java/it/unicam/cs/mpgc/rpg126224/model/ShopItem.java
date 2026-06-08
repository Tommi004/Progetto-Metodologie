package it.unicam.cs.mpgc.rpg126224.model;

/**
 * Represents a single entry in a dungeon shop.
 *
 * <p>Each shop item pairs a purchasable {@link Item} with a gold cost.
 * The player spends gold earned by defeating enemies to buy items.</p>
 *
 * @param item      the item available for purchase
 * @param goldCost  the gold required to buy this item
 */
public record ShopItem(Item item, int goldCost) {}