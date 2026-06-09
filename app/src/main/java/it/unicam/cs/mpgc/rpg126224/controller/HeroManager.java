package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.exception.ItemNotFoundException;
import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.UUID;

public class HeroManager implements HeroController {

    @Override
    public Hero createHero(String name, HeroClass heroClass) {
        return new Hero(UUID.randomUUID().toString(), name, heroClass);
    }

    @Override
    public boolean useItem(Hero hero, String itemId) {
        return hero.getInventory().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .map(item -> {
                    applyItemEffect(hero, item);
                    if (item.isStackable()) {
                        // Decrement stack; remove slot only when empty
                        if (item.decrementQuantity()) {
                            hero.removeItem(itemId);
                        }
                    } else {
                        hero.removeItem(itemId);
                    }
                    return true;
                })
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    @Override
    public void pickUpItem(Hero hero, Item item) { hero.addItem(item); }

    private void applyItemEffect(Hero hero, Item item) {
        switch (item.getType()) {
            case HEALTH_POTION   -> hero.heal(item.getValue());
            case STRENGTH_POTION -> hero.boostAttack(item.getValue());
            case MANA_POTION     -> hero.restoreMana(item.getValue());
            case SWORD, SPEAR    -> hero.boostAttack(item.getValue());
            case BOW             -> {
                hero.boostAttack(item.getValue());
                hero.boostMagic((int) Math.round(item.getValue() * 0.3));
            }
            case CROSSBOW        -> {
                hero.boostAttack(item.getValue());
                hero.boostMagic(item.getValue());
            }
            case STAFF, AMULET   -> hero.boostMagic(item.getValue());
            case ARMOR, HELMET   -> hero.boostDefense(item.getValue());
        }
    }
}