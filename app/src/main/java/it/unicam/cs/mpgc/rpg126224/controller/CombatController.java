package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;

/**
 * Defines the contract for resolving a single turn of combat.
 *
 * <p>Combat in Level Up! is <em>turn-based</em>: the player chooses an action,
 * then the enemy counter-attacks. Both phases are resolved inside a single call
 * to {@link #executeTurn}, which returns an immutable {@link CombatResult}
 * describing the outcome.</p>
 *
 * <h2>Enemy attack patterns</h2>
 * <p>Each {@link EnemyType} has a distinct attack behaviour encoded in
 * {@code CombatManager}:</p>
 * <ul>
 *   <li>{@code GOBLIN}, {@code ASSASSIN}           — double physical strike</li>
 *   <li>{@code DARK_MAGE}, {@code WITCH}            — pure magic attack (bypasses DEF)</li>
 *   <li>{@code KNIGHT}, {@code LEVIATHAN}           — armoured strike (×1.25 / ×1.3 ATK)</li>
 *   <li>{@code DEMON}, {@code DEMON_LORD}           — physical + magic combo</li>
 *   <li>{@code DEMON_SOUL}                          — triple magic strike</li>
 *   <li>all others                                  — single physical attack</li>
 * </ul>
 *
 * <h2>Extension</h2>
 * <p>Alternative implementations can be injected (e.g. a {@code NetworkCombatManager}
 * for multiplayer) without modifying {@code GameController} or the view layer.</p>
 */
public interface CombatController {

    /**
     * Resolves one full combat turn: the hero acts first, then the enemy
     * counter-attacks (unless the combat is already over after the hero's action).
     *
     * <p>The {@code selectedPotionId} parameter is only consulted when
     * {@code action == CombatAction.USE_POTION}. Pass {@code null} for all
     * other actions, or when no potion has been pre-selected.</p>
     *
     * @param hero             the hero taking part in combat; must not be null
     * @param enemy            the enemy taking part in combat; must not be null
     * @param action           the action chosen by the player; must not be null
     * @param selectedPotionId the inventory id of the potion to use, or
     *                         {@code null} if no potion is selected
     * @return an immutable {@link CombatResult} describing damage dealt,
     *         damage taken, and whether combat has ended
     */
    CombatResult executeTurn(Hero hero, Enemy enemy, CombatAction action,
                             String selectedPotionId);
}