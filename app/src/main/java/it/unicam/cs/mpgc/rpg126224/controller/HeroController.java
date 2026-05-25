package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.Hero;
import it.unicam.cs.mpgc.rpg126224.model.HeroClass;
import it.unicam.cs.mpgc.rpg126224.model.Item;

/**
 * Defines the contract for managing the player's hero.
 */
public interface HeroController {

    Hero createHero(String name, HeroClass heroClass);
    boolean useItem(Hero hero, String itemId);
    void pickUpItem(Hero hero, Item item);
}