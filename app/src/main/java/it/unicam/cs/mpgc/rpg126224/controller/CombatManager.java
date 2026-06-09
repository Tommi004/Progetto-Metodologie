package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.Random;

/**
 * Implementation of CombatController.
 */
public class CombatManager implements CombatController {

    private static final double FLEE_SUCCESS_RATE = 0.45;
    private final Random random = new Random();

    @Override
    public CombatResult executeTurn(Hero hero, Enemy enemy, CombatAction action,
                                    String selectedPotionId) {
        int heroDmg = 0;
        int enemyDmg = 0;
        boolean fled = false;
        StringBuilder msg = new StringBuilder();

        switch (action) {
            case ATTACK -> {
                int raw = calcHeroAttack(hero);
                int hpBefore = enemy.getCurrentHp();
                enemy.takeDamage(raw);
                heroDmg = hpBefore - enemy.getCurrentHp();
                msg.append(hero.getName()).append(" attacks for ").append(heroDmg).append(" dmg. ");
            }
            case SPECIAL -> {
                int manaCost = hero.getHeroClass().getSpecialManaCost();
                if (!hero.useMana(manaCost)) {
                    msg.append("Not enough mana! (need ").append(manaCost)
                       .append(", have ").append(hero.getCurrentMana()).append(") ");
                } else {
                    int raw = calcHeroSpecial(hero);
                    int hpBefore = enemy.getCurrentHp();
                    enemy.takeDamage(raw);
                    heroDmg = hpBefore - enemy.getCurrentHp();
                    msg.append(hero.getName()).append(" uses special for ")
                       .append(heroDmg).append(" dmg. (-").append(manaCost).append(" MP) ");
                }
            }
            case USE_POTION -> {
                boolean used = usePotion(hero, selectedPotionId);
                msg.append(used ? hero.getName() + " uses a potion! "
                               : "No potions in inventory! ");
            }
            case FLEE -> {
                if (random.nextDouble() < FLEE_SUCCESS_RATE) {
                    fled = true;
                    msg.append(hero.getName()).append(" successfully flees! ");
                } else {
                    msg.append("Flee attempt failed! ");
                }
            }
        }

        if (!fled && enemy.isAlive()) {
            enemyDmg = calcEnemyAttack(hero, enemy, msg);
        }

        boolean enemyDefeated = !enemy.isAlive();
        boolean heroDefeated  = !hero.isAlive();

        if (enemyDefeated) {
            hero.gainExperience(enemy.getXpReward());
            msg.append(enemy.getName())
               .append(" defeated! +").append(enemy.getXpReward()).append(" XP.");
        }
        if (heroDefeated) {
            msg.append(" ").append(hero.getName()).append(" has been defeated...");
        }

        return new CombatResult(heroDmg, enemyDmg, fled,
                enemyDefeated, heroDefeated, msg.toString().trim());
    }

    private int calcHeroAttack(Hero hero) {
        int variance = random.nextInt(5) - 2;
        return Math.max(1, hero.getAttack() + variance);
    }

    private int calcHeroSpecial(Hero hero) {
        return switch (hero.getHeroClass()) {
            case WARRIOR -> (int)(hero.getAttack() * 1.8);
            case MAGE    -> (int)(hero.getMagic()  * 1.5);
            case ARCHER  -> hero.getAttack() + hero.getMagic();
        };
    }

    private int calcEnemyAttack(Hero hero, Enemy enemy, StringBuilder msg) {
        int dmg;
        int hpBefore;
        switch (enemy.getType()) {
            case GOBLIN -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getAttack());
                hero.takeDamage(enemy.getAttack());
                dmg = hpBefore - hero.getCurrentHp();
                int rawG = enemy.getAttack() * 2;
                msg.append(enemy.getName()).append(" attacks twice for ").append(rawG).append(" dmg");
                appendResistance(msg, rawG, dmg);
            }
            case DARK_MAGE, WITCH -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getMagic());
                dmg = hpBefore - hero.getCurrentHp();
                msg.append(enemy.getName()).append(" casts a spell for ").append(enemy.getMagic()).append(" dmg");
                appendResistance(msg, enemy.getMagic(), dmg);
            }
            case ASSASSIN -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getAttack());
                hero.takeDamage(enemy.getAttack());
                dmg = hpBefore - hero.getCurrentHp();
                int rawA = enemy.getAttack() * 2;
                msg.append(enemy.getName()).append(" strikes twice for ").append(rawA).append(" dmg");
                appendResistance(msg, rawA, dmg);
            }
            case KNIGHT -> {
                hpBefore = hero.getCurrentHp();
                int rawK = (int)(enemy.getAttack() * 1.25);
                hero.takeDamage(rawK);
                dmg = hpBefore - hero.getCurrentHp();
                msg.append(enemy.getName()).append(" slams for ").append(rawK).append(" dmg");
                appendResistance(msg, rawK, dmg);
            }
            case DEMON -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getAttack());
                hero.takeDamage(enemy.getMagic());
                dmg = hpBefore - hero.getCurrentHp();
                int rawD = enemy.getAttack() + enemy.getMagic();
                msg.append(enemy.getName()).append(" strikes with dark power for ").append(rawD).append(" dmg");
                appendResistance(msg, rawD, dmg);
            }
            case LEVIATHAN -> {
                hpBefore = hero.getCurrentHp();
                int rawL = (int)(enemy.getAttack() * 1.3);
                hero.takeDamage(rawL);
                dmg = hpBefore - hero.getCurrentHp();
                msg.append("The ").append(enemy.getName()).append(" crushes you for ").append(rawL).append(" dmg");
                appendResistance(msg, rawL, dmg);
            }
            case DEMON_LORD -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getAttack());
                hero.takeDamage(enemy.getMagic());
                dmg = hpBefore - hero.getCurrentHp();
                int rawDL = enemy.getAttack() + enemy.getMagic();
                msg.append(enemy.getName()).append(" unleashes infernal wrath for ").append(rawDL).append(" dmg");
                appendResistance(msg, rawDL, dmg);
            }
            case DEMON_SOUL -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getMagic());
                hero.takeDamage(enemy.getMagic());
                hero.takeDamage(enemy.getAttack());
                dmg = hpBefore - hero.getCurrentHp();
                int rawDS = enemy.getMagic() * 2 + enemy.getAttack();
                msg.append("DEMON SOUL unleashes chaos for ").append(rawDS).append(" dmg");
                appendResistance(msg, rawDS, dmg);
            }
            default -> {
                hpBefore = hero.getCurrentHp();
                hero.takeDamage(enemy.getAttack());
                dmg = hpBefore - hero.getCurrentHp();
                msg.append(enemy.getName()).append(" attacks for ").append(enemy.getAttack()).append(" dmg");
                appendResistance(msg, enemy.getAttack(), dmg);
            }
        }
        msg.append(" ");
        return dmg;
    }

    /**
     * Appends resistance note if effective damage is less than raw damage.
     */
    private void appendResistance(StringBuilder msg, int raw, int effective) {
        if (effective < raw) {
            msg.append(" → ").append(effective).append(" (resistance reduced damage)");
        } else {
            msg.append(".");
        }
    }

    private boolean usePotion(Hero hero, String selectedId) {
        // Try the specifically selected potion first
        if (selectedId != null) {
            return hero.getInventory().stream()
                    .filter(i -> i.getId().equals(selectedId))
                    .findFirst()
                    .map(item -> {
                        applyPotionEffect(hero, item);
                        if (item.decrementQuantity()) {
                            hero.removeItem(item.getId());
                        }
                        return true;
                    })
                    .orElse(false);
        }
        // Fallback: use first available potion
        return hero.getInventory().stream()
                .filter(i -> i.getType() == ItemType.HEALTH_POTION
                          || i.getType() == ItemType.MANA_POTION
                          || i.getType() == ItemType.STRENGTH_POTION)
                .findFirst()
                .map(item -> {
                    applyPotionEffect(hero, item);
                    if (item.decrementQuantity()) {
                        hero.removeItem(item.getId());
                    }
                    return true;
                })
                .orElse(false);
    }

    private void applyPotionEffect(Hero hero, Item item) {
        switch (item.getType()) {
            case HEALTH_POTION   -> hero.heal(item.getValue());
            case MANA_POTION     -> hero.restoreMana(item.getValue());
            case STRENGTH_POTION -> hero.boostAttack(item.getValue()); // temp: reversed at combat end
        }
    }
}