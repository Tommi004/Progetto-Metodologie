package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.controller.CombatManager;
import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Combat Tests")
class CombatTest {

    private CombatManager manager;
    private Hero hero;
    private Enemy goblin;

    @BeforeEach
    void setUp() {
        manager = new CombatManager();
        hero    = new Hero("h1", "Aldric", HeroClass.WARRIOR, "male");
        goblin  = new Enemy("e1", EnemyType.GOBLIN);
    }

    @Test
    @DisplayName("ATTACK action deals positive damage to enemy")
    void attackDealsDamage() {
        CombatResult result = manager.executeTurn(hero, goblin, CombatAction.ATTACK);
        assertTrue(result.heroDamageDealt() > 0);
        assertTrue(result.message().contains("attacks"));
    }

    @Test
    @DisplayName("USE_POTION with no potions returns message")
    void potionWithNoInventory() {
        CombatResult result = manager.executeTurn(hero, goblin, CombatAction.USE_POTION);
        assertTrue(result.message().toLowerCase().contains("no") ||
                result.message().toLowerCase().contains("potion"));
    }

    @Test
    @DisplayName("SPECIAL action returns valid result")
    void specialActionReturnsResult() {
        CombatResult result = manager.executeTurn(hero, goblin, CombatAction.SPECIAL);
        assertNotNull(result);
        assertTrue(result.heroDamageDealt() >= 0);
    }

    @Test
    @DisplayName("CombatResult is over when enemy defeated")
    void combatOverOnDefeat() {
        CombatResult result = new CombatResult(10, 0, false, true, false, "Enemy defeated!");
        assertTrue(result.isCombatOver());
    }

    @Test
    @DisplayName("CombatResult is over when hero fled")
    void combatOverOnFlee() {
        CombatResult result = new CombatResult(0, 0, true, false, false, "Fled!");
        assertTrue(result.isCombatOver());
    }

    @Test
    @DisplayName("Hero gains XP after defeating enemy")
    void heroGainsXpOnKill() {
        int xpBefore = hero.getExperience();
        goblin.takeDamage(999);
        hero.gainExperience(goblin.getXpReward());
        assertTrue(hero.getExperience() > xpBefore || hero.getLevel() > 1);
    }
}