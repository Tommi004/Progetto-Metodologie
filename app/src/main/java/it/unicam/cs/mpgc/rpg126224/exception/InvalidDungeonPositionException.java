package it.unicam.cs.mpgc.rpg126224.exception;

/**
 * Thrown when a dungeon grid access is attempted at coordinates that fall
 * outside the valid bounds of the grid.
 *
 * <p>Replaces the generic {@link IndexOutOfBoundsException} previously thrown
 * by {@code Dungeon.validateCoordinates}, providing a domain-specific type
 * that can be caught and handled distinctly from other index errors.</p>
 *
 * <h2>Valid range</h2>
 * <p>For the standard 8×8 dungeon: row and column must both be in
 * {@code [0, Dungeon.SIZE - 1]}.</p>
 */
public class InvalidDungeonPositionException extends RuntimeException {

    /** The invalid row coordinate. */
    private final int row;

    /** The invalid column coordinate. */
    private final int col;

    /**
     * Constructs a new exception for the given out-of-bounds coordinates.
     *
     * @param row the invalid row
     * @param col the invalid column
     */
    public InvalidDungeonPositionException(int row, int col) {
        super("Invalid dungeon position: [" + row + ", " + col + "].");
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the invalid row coordinate.
     *
     * @return row value that caused the exception
     */
    public int getRow() { return row; }

    /**
     * Returns the invalid column coordinate.
     *
     * @return column value that caused the exception
     */
    public int getCol() { return col; }
}