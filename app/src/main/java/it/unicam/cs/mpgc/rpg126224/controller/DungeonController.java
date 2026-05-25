package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;

/**
 * Defines the contract for generating and navigating the dungeon.
 */
public interface DungeonController {

    Dungeon generateDungeon(int level);
    boolean moveHero(Hero hero, Dungeon dungeon, int dRow, int dCol);
    Room getCurrentRoom(Hero hero, Dungeon dungeon);
}