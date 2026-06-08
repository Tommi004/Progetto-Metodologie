package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Enemy} and {@link EnemyType}.
 *
 * <p>Covers: stat initialisation, damage, death detection,
 * XP rewards and boss identification.</p>
 */
@DisplayName("Enemy Tests")
class EnemyTest {

    private Enemy goblin;
    private Enemy dragon;
    private Enemy troll;

    @BeforeEach
    void setUp() {
        goblin = new Enemy("e1", EnemyType.GOBLIN);
        dragon = new Enemy("e2", EnemyType.DRAGON);
        troll  = new Enemy("e3", EnemyType.TROLL);
    }

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Enemy id and type are stored correctly")
    void enemyIdAndType() {
        assertEquals("e1",          goblin.getId());
        assertEquals(EnemyType.GOBLIN, goblin.getType());
    }

    @Test
    @DisplayName("Enemy starts alive with full HP")
    void enemyStartsAlive() {
        assertTrue(goblin.isAlive());
        assertTrue(goblin.getCurrentHp() > 0);
        assertEquals(goblin.getMaxHp(), goblin.getCurrentHp());
    }

    @Test
    @DisplayName("Dragon has more HP than Goblin")
    void bossHasMoreHpThanRegular() {
        assertTrue(dragon.getMaxHp() > goblin.getMaxHp());
    }

    @Test
    @DisplayName("Dragon has more attack than Goblin")
    void bossHasMoreAttackThanRegular() {
        assertTrue(dragon.getAttack() > goblin.getAttack());
    }

    // -------------------------------------------------------------------------
    // Damage and death
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("takeDamage reduces enemy HP")
    void takeDamageReducesHp() {
        int hpBefore = goblin.getCurrentHp();
        goblin.takeDamage(5);
        assertTrue(goblin.getCurrentHp() < hpBefore);
    }

    @Test
    @DisplayName("Enemy HP cannot go below zero")
    void hpDoesNotGoBelowZero() {
        goblin.takeDamage(9999);
        assertEquals(0, goblin.getCurrentHp());
    }

    @Test
    @DisplayName("Enemy is dead after lethal damage")
    void enemyDeadAfterLethalDamage() {
        goblin.takeDamage(9999);
        assertFalse(goblin.isAlive());
    }

    @Test
    @DisplayName("setCurrentHp to zero kills enemy")
    void setCurrentHpToZeroKills() {
        goblin.setCurrentHp(0);
        assertFalse(goblin.isAlive());
    }

    // -------------------------------------------------------------------------
    // XP rewards
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Goblin gives 15 XP on defeat")
    void goblinXpReward() {
        assertEquals(15, goblin.getXpReward());
    }

    @Test
    @DisplayName("Dragon gives 150 XP on defeat")
    void dragonXpReward() {
        assertEquals(150, dragon.getXpReward());
    }

    @Test
    @DisplayName("Boss enemies give more XP than regular enemies")
    void bossGivesMoreXpThanRegular() {
        assertTrue(troll.getXpReward() > goblin.getXpReward());
    }

    @Test
    @DisplayName("All EnemyTypes have positive XP reward")
    void allEnemyTypesHavePositiveXp() {
        for (EnemyType type : EnemyType.values()) {
            assertTrue(type.getXpReward() > 0,
                    "XP reward should be > 0 for " + type);
        }
    }

    // -------------------------------------------------------------------------
    // EnemyType stats sanity
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("All EnemyTypes have positive HP")
    void allEnemyTypesHavePositiveHp() {
        for (EnemyType type : EnemyType.values()) {
            assertTrue(type.getBaseHp() > 0,
                    "Base HP should be > 0 for " + type);
        }
    }

    @Test
    @DisplayName("All EnemyTypes have positive attack")
    void allEnemyTypesHavePositiveAttack() {
        for (EnemyType type : EnemyType.values()) {
            assertTrue(type.getBaseAttack() > 0,
                    "Base attack should be > 0 for " + type);
        }
    }
}