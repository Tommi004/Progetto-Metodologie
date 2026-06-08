package it.unicam.cs.mpgc.rpg126224;

import it.unicam.cs.mpgc.rpg126224.exception.*;
import it.unicam.cs.mpgc.rpg126224.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the custom exception classes in Level Up!.
 *
 * <p>Verifies that each exception carries the correct domain data
 * and that the right exception type is thrown in each error scenario.</p>
 */
@DisplayName("Custom Exception Tests")
class ExceptionTest {

    // -------------------------------------------------------------------------
    // InvalidDungeonPositionException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("InvalidDungeonPositionException stores row and col")
    void invalidPositionStoresCoordinates() {
        InvalidDungeonPositionException ex =
                new InvalidDungeonPositionException(-1, 9);
        assertEquals(-1, ex.getRow());
        assertEquals(9,  ex.getCol());
        assertTrue(ex.getMessage().contains("-1"));
        assertTrue(ex.getMessage().contains("9"));
    }

    @Test
    @DisplayName("Dungeon.getRoom throws InvalidDungeonPositionException on bad coords")
    void dungeonThrowsOnInvalidAccess() {
        Dungeon dungeon = new Dungeon();
        assertThrows(InvalidDungeonPositionException.class,
                () -> dungeon.getRoom(-1, 0));
        assertThrows(InvalidDungeonPositionException.class,
                () -> dungeon.getRoom(0, Dungeon.SIZE));
        assertThrows(InvalidDungeonPositionException.class,
                () -> dungeon.getRoom(Dungeon.SIZE, Dungeon.SIZE));
    }

    // -------------------------------------------------------------------------
    // InsufficientManaException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("InsufficientManaException stores current and required mana")
    void insufficientManaStoresValues() {
        InsufficientManaException ex = new InsufficientManaException(10, 20);
        assertEquals(10, ex.getCurrentMana());
        assertEquals(20, ex.getRequiredMana());
        assertTrue(ex.getMessage().contains("10"));
        assertTrue(ex.getMessage().contains("20"));
    }

    @Test
    @DisplayName("InsufficientManaException message is human-readable")
    void insufficientManaMessageIsReadable() {
        InsufficientManaException ex = new InsufficientManaException(5, 15);
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    // -------------------------------------------------------------------------
    // ItemNotFoundException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("ItemNotFoundException stores the missing item id")
    void itemNotFoundStoresId() {
        ItemNotFoundException ex = new ItemNotFoundException("potion-42");
        assertEquals("potion-42", ex.getItemId());
        assertTrue(ex.getMessage().contains("potion-42"));
    }

    @Test
    @DisplayName("ItemNotFoundException message is human-readable")
    void itemNotFoundMessageIsReadable() {
        ItemNotFoundException ex = new ItemNotFoundException("sword-01");
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    // -------------------------------------------------------------------------
    // InvalidMoveException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("InvalidMoveException(String) stores the message")
    void invalidMoveStringConstructor() {
        InvalidMoveException ex = new InvalidMoveException("Cannot move north");
        assertEquals("Cannot move north", ex.getMessage());
    }

    @Test
    @DisplayName("InvalidMoveException(int,int) builds a readable message")
    void invalidMoveCoordConstructor() {
        InvalidMoveException ex = new InvalidMoveException(-1, 0);
        assertTrue(ex.getMessage().contains("-1"));
        assertTrue(ex.getMessage().contains("0"));
    }

    // -------------------------------------------------------------------------
    // Exception hierarchy — all extend RuntimeException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("All custom exceptions extend RuntimeException (unchecked)")
    void allExceptionsAreUnchecked() {
        assertInstanceOf(RuntimeException.class,
                new InvalidDungeonPositionException(0, 0));
        assertInstanceOf(RuntimeException.class,
                new InsufficientManaException(0, 1));
        assertInstanceOf(RuntimeException.class,
                new ItemNotFoundException("id"));
        assertInstanceOf(RuntimeException.class,
                new InvalidMoveException("msg"));
    }
}