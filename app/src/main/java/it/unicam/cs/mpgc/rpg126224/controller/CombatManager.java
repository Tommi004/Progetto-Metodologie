package it.unicam.cs.mpgc.rpg126224.controller;

import it.unicam.cs.mpgc.rpg126224.model.*;
import java.util.Random;

/**
 * Implementation of CombatController.
 */
public class CombatManager implements CombatController {

    private static final double FLEE_SUCCESS_RATE = 0.45;
    private static final int TROLL_REGEN = 5;
    private final Random random = new Random();

    @Override
    public CombatResult executeTurn(Hero hero, Enemy enemy, CombatAction action) {
        int heroDmg = 0;
        int enemyDmg = 0;
        boolean fled = false;
        StringBuilder msg = new StringBuilder();

        switch (action) {
            case ATTACK -> {
                heroDmg = calcHeroAttack(hero);
                enemy.takeDamage(heroDmg);
                msg.append(hero.getName()).append(" attacks for ").append(heroDmg).append(" dmg. ");
            }
            case SPECIAL -> {
                heroDmg = calcHeroSpecial(hero);
                enemy.takeDamage(heroDmg);
                msg.append(hero.getName()).append(" uses special for ").append(heroDmg).append(" dmg. ");
            }
            case USE_POTION -> {
                boolean used = usePotion(hero);
                msg.append(used ? hero.getName() + " drinks a potion and recovers HP. "
                               : "No health potions in inventory! ");
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

        if (!fled && enemy.isAlive() && enemy.getType() == EnemyType.TROLL) {
            enemy.heal(TROLL_REGEN);
            msg.append("Troll regenerates ").append(TROLL_REGEN).append(" HP. ");
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
            case WARRIOR -> (int) (hero.getAttack() * 1.5);
            case MAGE    -> hero.getMagic() * 2;
            case ARCHER  -> hero.getAttack() + hero.getMagic();
        };
    }

    private int calcEnemyAttack(Hero hero, Enemy enemy, StringBuilder msg) {
        int dmg;
        switch (enemy.getType()) {
            case GOBLIN -> {
                int hit1 = Math.max(1, enemy.getAttack() - hero.getDefense() / 2);
                int hit2 = Math.max(1, enemy.getAttack() - hero.getDefense() / 2);
                hero.takeDamage(hit1);
                hero.takeDamage(hit2);
                dmg = hit1 + hit2;
                msg.append(enemy.getName()).append(" attacks twice for ")
                   .append(hit1).append("+").append(hit2).append(" dmg. ");
            }
            case DARK_MAGE -> {
                int spellDmg = Math.max(1, enemy.getMagic() - hero.getDefense() / 3);
                hero.takeDamage(spellDmg);
                dmg = spellDmg;
                msg.append(enemy.getName())
                   .append(" casts a spell for ").append(spellDmg).append(" dmg. ");
            }
            case ASSASSIN -> {
                int hit1 = Math.max(1, enemy.getAttack() - hero.getDefense() / 3);
                int hit2 = Math.max(1, enemy.getAttack() - hero.getDefense() / 3);
                hero.takeDamage(hit1);
                hero.takeDamage(hit2);
                dmg = hit1 + hit2;
                msg.append(enemy.getName()).append(" strikes twice for ")
                   .append(hit1).append("+").append(hit2).append(" dmg! ");
            }
            default -> {
                dmg = Math.max(1, enemy.getAttack() - hero.getDefense() / 2);
                hero.takeDamage(dmg);
                msg.append(enemy.getName()).append(" attacks for ").append(dmg).append(" dmg. ");
            }
        }
        return dmg;
    }

    private boolean usePotion(Hero hero) {
        return hero.getInventory().stream()
                .filter(i -> i.getType() == ItemType.HEALTH_POTION)
                .findFirst()
                .map(item -> {
                    hero.heal(item.getValue());
                    hero.removeItem(item.getId());
                    return true;
                })
                .orElse(false);
    }
}