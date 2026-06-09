package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link ShopController}.
 *
 * <p>Generates a 5-item catalogue scaled to the dungeon floor.
 * Each floor offers 2 potions and 3 equipment/consumable items
 * whose power and gold cost increase with the level.</p>
 */
public class ShopManager implements ShopController {

    private final HeroController heroController;

    public ShopManager(HeroController heroController) {
        this.heroController = heroController;
    }

    @Override
    public List<ShopItem> generateCatalogue(int level) {
        List<ShopItem> catalogue = new ArrayList<>();

        // ── Potions (always available, price scales with floor) ──────────
        int hpValue  = 30 + level * 10;
        int hpCost   = 15 + level * 5;
        catalogue.add(new ShopItem(
                new Item(UUID.randomUUID().toString(),
                        "Health Potion", ItemType.HEALTH_POTION, hpValue),
                hpCost));

        int mpValue = 20 + level * 8;
        int mpCost  = 12 + level * 4;
        catalogue.add(new ShopItem(
                new Item(UUID.randomUUID().toString(),
                        "Mana Potion", ItemType.MANA_POTION, mpValue),
                mpCost));

        // ── Equipment (type and rarity scale with floor) ─────────────────
        Rarity rarity = switch (level) {
            case 1, 2 -> Rarity.COMMON;
            case 3    -> Rarity.RARE;
            case 4    -> Rarity.EPIC;
            default   -> Rarity.LEGENDARY;
        };

        int baseValue = 8 + level * 4;
        int eqCost    = 30 + level * 20;

        catalogue.add(new ShopItem(
                new Item(UUID.randomUUID().toString(),
                        rarity == Rarity.COMMON ? "Sword" : rarity.getDisplayName() + " Sword",
                        ItemType.SWORD, (int)(baseValue * rarity.getMultiplier()), rarity),
                (int)(eqCost * rarity.getMultiplier())));

        catalogue.add(new ShopItem(
                new Item(UUID.randomUUID().toString(),
                        rarity == Rarity.COMMON ? "Armor" : rarity.getDisplayName() + " Armor",
                        ItemType.ARMOR, (int)((baseValue - 2) * rarity.getMultiplier()), rarity),
                (int)(eqCost * rarity.getMultiplier())));

        catalogue.add(new ShopItem(
                new Item(UUID.randomUUID().toString(),
                        rarity == Rarity.COMMON ? "Staff" : rarity.getDisplayName() + " Staff",
                        ItemType.STAFF, (int)(baseValue * rarity.getMultiplier()), rarity),
                (int)(eqCost * rarity.getMultiplier())));

        return List.copyOf(catalogue);
    }

    @Override
    public boolean buyItem(Hero hero, ShopItem shopItem) {
        if (!hero.spendGold(shopItem.goldCost())) return false;
        heroController.pickUpItem(hero, shopItem.item());
        return true;
    }
}