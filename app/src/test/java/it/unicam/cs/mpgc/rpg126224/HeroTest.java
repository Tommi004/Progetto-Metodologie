package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Hero class.
 * Verifies stat initialisation, damage, healing, level-up and inventory logic.
 */
@DisplayName("Hero Tests")
class HeroTest {

    private Hero warrior;
    private Hero mage;

    @BeforeEach
    void setUp() {
        warrior = new Hero("w1", "Aldric", HeroClass.WARRIOR);
        mage    = new Hero("m1", "Lyra",   HeroClass.MAGE);
    }

    @Test
    @DisplayName("Warrior starts with correct base stats")
    void warriorBaseStats() {
        assertEquals(120, warrior.getMaxHp());
        assertEquals(18,  warrior.getAttack());
        assertEquals(8,   warrior.getDefense());
        assertEquals(4,   warrior.getMagic());
        assertEquals(1,   warrior.getLevel());
        assertTrue(warrior.isAlive());
    }

    @Test
    @DisplayName("Mage starts with correct base stats")
    void mageBaseStats() {
        assertEquals(70, mage.getMaxHp());
        assertEquals(20, mage.getMagic());
    }

    @Test
    @DisplayName("takeDamage reduces HP correctly applying defense")
    void takeDamageReducesHp() {
        int hpBefore = warrior.getCurrentHp();
        warrior.takeDamage(20); // effective = 20 - 8 defense = 12
        assertEquals(hpBefore - 16, warrior.getCurrentHp());
    }

    @Test
    @DisplayName("HP cannot go below zero")
    void hpDoesNotGoBelowZero() {
        warrior.takeDamage(10000);
        assertEquals(0, warrior.getCurrentHp());
        assertFalse(warrior.isAlive());
    }

    @Test
    @DisplayName("Heal restores HP without exceeding maxHp")
    void healCapsAtMaxHp() {
        warrior.takeDamage(50);
        warrior.heal(200);
        assertEquals(warrior.getMaxHp(), warrior.getCurrentHp());
    }

    @Test
    @DisplayName("Gaining enough XP triggers a level-up")
    void levelUpOnXpThreshold() {
        warrior.gainExperience(30);
        assertEquals(2, warrior.getLevel());
    }

    @Test
    @DisplayName("Level-up increases attack and defense")
    void levelUpIncreasesStats() {
        int atkBefore = warrior.getAttack();
        int defBefore = warrior.getDefense();
        warrior.gainExperience(100);
        assertTrue(warrior.getAttack() > atkBefore);
        assertTrue(warrior.getDefense() > defBefore);
    }

    @Test
    @DisplayName("Items can be added to and removed from inventory")
    void inventoryAddRemove() {
        Item potion = new Item("p1", "Health Potion", ItemType.HEALTH_POTION, 30);
        warrior.addItem(potion);
        assertEquals(1, warrior.getInventory().size());
        assertTrue(warrior.removeItem("p1"));
        assertTrue(warrior.getInventory().isEmpty());
    }

    @Test
    @DisplayName("Hero position can be updated")
    void positionUpdate() {
        warrior.setPosition(3, 5);
        assertEquals(3, warrior.getRow());
        assertEquals(5, warrior.getCol());
    }

    @Test
    @DisplayName("Constructor throws on null arguments")
    void constructorNullCheck() {
        assertThrows(NullPointerException.class, () -> new Hero(null, "name", HeroClass.WARRIOR));
        assertThrows(NullPointerException.class, () -> new Hero("id", null, HeroClass.WARRIOR));
        assertThrows(NullPointerException.class, () -> new Hero("id", "name", null));
    }
}
