package it.unicam.cs.mpgc.rpg126224.exception;

/**
 * Thrown when a hero movement is attempted toward a position that lies
 * outside the dungeon grid boundaries.
 *
 * <p>This is an unchecked exception: movement validation is a programming
 * concern (the view should never request an out-of-bounds move), not a
 * recoverable runtime condition for the player.</p>
 *
 * <p>Note: {@code DungeonManager.moveHero} returns {@code false} for normal
 * out-of-bounds moves triggered by the player pressing a direction key at the
 * edge of the map. This exception is thrown only for programmatic calls that
 * pass an invalid delta (e.g. {@code |dRow| > 1}).</p>
 */
public class InvalidMoveException extends RuntimeException {

    /**
     * Constructs a new exception with a detail message describing the
     * attempted move.
     *
     * @param message description of the invalid move
     */
    public InvalidMoveException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception for a specific out-of-bounds position.
     *
     * @param row the invalid row coordinate
     * @param col the invalid column coordinate
     */
    public InvalidMoveException(int row, int col) {
        super("Cannot move to position [" + row + ", " + col + "]: out of dungeon bounds.");
    }
}