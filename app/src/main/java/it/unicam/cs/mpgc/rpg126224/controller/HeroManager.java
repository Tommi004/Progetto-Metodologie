package it.unicam.cs.mpgc.rpg126224.controller;

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
                    hero.removeItem(itemId);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void pickUpItem(Hero hero, Item item) { hero.addItem(item); }

    private void applyItemEffect(Hero hero, Item item) {
        switch (item.getType()) {
            case HEALTH_POTION    -> hero.heal(item.getValue());
            case STRENGTH_POTION  -> hero.boostAttack(item.getValue());
            case MANA_POTION      -> hero.restoreMana(item.getValue());
            case SWORD, BOW       -> hero.boostAttack(item.getValue());
            case STAFF, AMULET    -> hero.boostMagic(item.getValue());
            case ARMOR            -> hero.boostDefense(item.getValue());
        }
    }
}