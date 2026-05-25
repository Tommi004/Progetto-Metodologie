package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;

/**
 * Defines the contract for managing turn-based combat.
 */
public interface CombatController {

    CombatResult executeTurn(Hero hero, Enemy enemy, CombatAction action);
}