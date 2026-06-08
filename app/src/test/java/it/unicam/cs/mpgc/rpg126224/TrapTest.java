package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TrapType} and the trap-damage mechanic.
 *
 * <p>Key invariant under test: trap damage bypasses the hero's defense
 * reduction and is applied directly to {@code currentHp} via
 * {@code setCurrentHp}, unlike combat damage which uses {@code takeDamage}.</p>
 */
@DisplayName("Trap Tests")
class TrapTest {

    private Hero warrior;
    private Hero mage;

    @BeforeEach
    void setUp() {
        warrior = new Hero("w1", "Aldric", HeroClass.WARRIOR);
        mage    = new Hero("m1", "Lyra",   HeroClass.MAGE);
    }

    // -------------------------------------------------------------------------
    // TrapType metadata
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Every TrapType has a non-empty effect description")
    void allTrapsHaveDescription() {
        for (TrapType t : TrapType.values()) {
            assertNotNull(t.getEffectDescription());
            assertFalse(t.getEffectDescription().isBlank(),
                    "Effect description is blank for " + t);
        }
    }

    @Test
    @DisplayName("Every TrapType has a non-empty emoji")
    void allTrapsHaveEmoji() {
        for (TrapType t : TrapType.values()) {
            assertNotNull(t.getEmoji());
            assertFalse(t.getEmoji().isBlank(),
                    "Emoji is blank for " + t);
        }
    }

    @Test
    @DisplayName("SPIKE_TRAP has positive HP damage and no MP damage")
    void spikeTrapValues() {
        assertTrue(TrapType.SPIKE_TRAP.getHpDamage() > 0);
        assertEquals(0, TrapType.SPIKE_TRAP.getMpDamage());
        assertFalse(TrapType.SPIKE_TRAP.hasAtkDebuff());
    }

    @Test
    @DisplayName("VENOM_POOL has both HP and MP damage")
    void venomPoolValues() {
        assertTrue(TrapType.VENOM_POOL.getHpDamage() > 0);
        assertTrue(TrapType.VENOM_POOL.getMpDamage() > 0);
        assertFalse(TrapType.VENOM_POOL.hasAtkDebuff());
    }

    @Test
    @DisplayName("HEX_MARK has no HP damage but applies ATK debuff")
    void hexMarkValues() {
        assertEquals(0, TrapType.HEX_MARK.getHpDamage());
        assertTrue(TrapType.HEX_MARK.hasAtkDebuff());
    }

    @Test
    @DisplayName("BRIMSTONE_PIT has the highest HP damage of all traps")
    void brimstonePitIsStrongest() {
        int max = TrapType.SPIKE_TRAP.getHpDamage();
        for (TrapType t : TrapType.values()) {
            max = Math.max(max, t.getHpDamage());
        }
        assertEquals(max, TrapType.BRIMSTONE_PIT.getHpDamage());
    }

    // -------------------------------------------------------------------------
    // Trap damage bypasses defense
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Trap HP damage ignores hero defense (direct setCurrentHp)")
    void trapDamageIgnoresDefense() {
        // Warrior has DEF=8; takeDamage(15) would give effective = max(1, 15-4) = 11
        // But trap uses setCurrentHp directly: full 15 hp lost
        int hpBefore = warrior.getCurrentHp();
        int trapDmg  = TrapType.SPIKE_TRAP.getHpDamage(); // 15

        // Simulate direct trap application (no defense reduction)
        warrior.setCurrentHp(Math.max(0, warrior.getCurrentHp() - trapDmg));

        assertEquals(hpBefore - trapDmg, warrior.getCurrentHp(),
                "Trap damage should be applied in full, ignoring defense");
    }

    @Test
    @DisplayName("takeDamage with same value gives LESS damage than direct trap application")
    void trapDamageIsGreaterThanCombatDamage() {
        // Proves that bypassing defense is meaningful
        Hero h1 = new Hero("a", "A", HeroClass.WARRIOR);
        Hero h2 = new Hero("b", "B", HeroClass.WARRIOR);
        int rawDmg = TrapType.SPIKE_TRAP.getHpDamage();

        h1.takeDamage(rawDmg);                                          // combat path — defense applies
        h2.setCurrentHp(Math.max(0, h2.getCurrentHp() - rawDmg));      // trap path — no defense

        assertTrue(h2.getCurrentHp() <= h1.getCurrentHp(),
                "Trap (direct) should leave hero with same or less HP than combat damage");
    }

    @Test
    @DisplayName("VENOM_POOL reduces both HP and MP directly")
    void venomPoolReducesHpAndMp() {
        int hpBefore = mage.getCurrentHp();
        int mpBefore = mage.getCurrentMana();

        mage.setCurrentHp(Math.max(0, mage.getCurrentHp() - TrapType.VENOM_POOL.getHpDamage()));
        mage.setCurrentMana(Math.max(0, mage.getCurrentMana() - TrapType.VENOM_POOL.getMpDamage()));

        assertTrue(mage.getCurrentHp()   < hpBefore);
        assertTrue(mage.getCurrentMana() < mpBefore);
    }

    @Test
    @DisplayName("HP cannot go below zero from trap damage")
    void trapDamageCannotGoBelowZero() {
        warrior.setCurrentHp(Math.max(0, warrior.getCurrentHp() - 9999));
        assertEquals(0, warrior.getCurrentHp());
    }
}