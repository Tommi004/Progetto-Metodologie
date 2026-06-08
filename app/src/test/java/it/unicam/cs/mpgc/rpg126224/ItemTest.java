package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Item}, {@link Rarity} and {@link ItemType}.
 *
 * <p>Covers: item construction, default rarity, rarity ordering,
 * multipliers, colours and effect descriptions.</p>
 */
@DisplayName("Item / Rarity / ItemType Tests")
class ItemTest {

    // -------------------------------------------------------------------------
    // Item construction
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Item stores all fields correctly")
    void itemStoresFields() {
        Item item = new Item("i1", "Iron Sword", ItemType.SWORD, 15, Rarity.RARE);
        assertEquals("i1",          item.getId());
        assertEquals("Iron Sword",  item.getName());
        assertEquals(ItemType.SWORD, item.getType());
        assertEquals(15,            item.getValue());
        assertEquals(Rarity.RARE,   item.getRarity());
    }

    @Test
    @DisplayName("Default-rarity constructor sets COMMON")
    void defaultRarityIsCommon() {
        Item item = new Item("i2", "Health Potion", ItemType.HEALTH_POTION, 30);
        assertEquals(Rarity.COMMON, item.getRarity());
    }

    @Test
    @DisplayName("Item constructor throws on null id")
    void nullIdThrows() {
        assertThrows(NullPointerException.class,
                () -> new Item(null, "Sword", ItemType.SWORD, 10, Rarity.COMMON));
    }

    @Test
    @DisplayName("Item constructor throws on null name")
    void nullNameThrows() {
        assertThrows(NullPointerException.class,
                () -> new Item("i1", null, ItemType.SWORD, 10, Rarity.COMMON));
    }

    @Test
    @DisplayName("Item constructor throws on null type")
    void nullTypeThrows() {
        assertThrows(NullPointerException.class,
                () -> new Item("i1", "Sword", null, 10, Rarity.COMMON));
    }

    @Test
    @DisplayName("Item constructor throws on null rarity")
    void nullRarityThrows() {
        assertThrows(NullPointerException.class,
                () -> new Item("i1", "Sword", ItemType.SWORD, 10, null));
    }

    // -------------------------------------------------------------------------
    // Rarity ordering
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Rarity isHigherThan returns correct ordering")
    void rarityOrdering() {
        assertTrue(Rarity.RARE.isHigherThan(Rarity.COMMON));
        assertTrue(Rarity.EPIC.isHigherThan(Rarity.RARE));
        assertTrue(Rarity.LEGENDARY.isHigherThan(Rarity.EPIC));
        assertFalse(Rarity.COMMON.isHigherThan(Rarity.RARE));
        assertFalse(Rarity.COMMON.isHigherThan(Rarity.COMMON));
    }

    @Test
    @DisplayName("Rarity multipliers are strictly increasing")
    void rarityMultipliersIncreasing() {
        assertTrue(Rarity.RARE.getMultiplier()      > Rarity.COMMON.getMultiplier());
        assertTrue(Rarity.EPIC.getMultiplier()      > Rarity.RARE.getMultiplier());
        assertTrue(Rarity.LEGENDARY.getMultiplier() > Rarity.EPIC.getMultiplier());
    }

    @Test
    @DisplayName("Rarity display names are non-empty")
    void rarityDisplayNames() {
        for (Rarity r : Rarity.values()) {
            assertNotNull(r.getDisplayName());
            assertFalse(r.getDisplayName().isBlank());
        }
    }

    @Test
    @DisplayName("Rarity colors are valid hex strings")
    void rarityColors() {
        for (Rarity r : Rarity.values()) {
            String color = r.getColor();
            assertNotNull(color);
            assertTrue(color.startsWith("#"),
                    "Color should start with #: " + color);
            assertEquals(7, color.length(),
                    "Color should be 7 chars (#RRGGBB): " + color);
        }
    }

    // -------------------------------------------------------------------------
    // ItemType effect descriptions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Every ItemType has a non-empty effect description")
    void allItemTypesHaveDescription() {
        for (ItemType type : ItemType.values()) {
            String desc = type.getEffectDescription();
            assertNotNull(desc,
                    "getEffectDescription() returned null for " + type);
            assertFalse(desc.isBlank(),
                    "getEffectDescription() is blank for " + type);
        }
    }
}