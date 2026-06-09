package it.unicam.cs.mpgc.rpg126224.model;

import it.unicam.cs.mpgc.rpg126224.exception.InvalidDungeonPositionException;
import java.util.Objects;

/**
 * Represents the dungeon as a 2D grid of rooms with procedurally
 * generated walls between adjacent cells.
 *
 * <p>Walls are stored as two boolean matrices:</p>
 * <ul>
 *   <li>{@code wallRight[r][c]} — {@code true} if there is a wall between
 *       (r,c) and (r, c+1)</li>
 *   <li>{@code wallDown[r][c]}  — {@code true} if there is a wall between
 *       (r,c) and (r+1, c)</li>
 * </ul>
 * <p>By default all walls are present. {@code DungeonManager} carves
 * passages using a recursive-backtracker maze algorithm, guaranteeing
 * full connectivity from START (0,0) to EXIT (SIZE-1, SIZE-1).</p>
 */
public class Dungeon {

    public static final int SIZE = 8;

    private final Room[][]    grid;
    private final boolean[][] wallRight;  // wall between (r,c) and (r, c+1)
    private final boolean[][] wallDown;   // wall between (r,c) and (r+1, c)
    private final int rows;
    private final int cols;

    public Dungeon() { this(SIZE, SIZE); }

    public Dungeon(int rows, int cols) {
        this.rows      = rows;
        this.cols      = cols;
        this.grid      = new Room[rows][cols];
        this.wallRight = new boolean[rows][cols];
        this.wallDown  = new boolean[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c]      = new Room(r, c, RoomType.EMPTY);
                wallRight[r][c] = true;   // all walls present by default
                wallDown[r][c]  = true;
            }
        }
    }

    // ------------------------------------------------------------------
    // Room access
    // ------------------------------------------------------------------

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public Room getRoom(int row, int col) {
        validateCoordinates(row, col);
        return grid[row][col];
    }

    public void setRoom(int row, int col, Room room) {
        validateCoordinates(row, col);
        grid[row][col] = Objects.requireNonNull(room);
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // ------------------------------------------------------------------
    // Wall access
    // ------------------------------------------------------------------

    /**
     * Returns {@code true} if there is a wall between (r,c) and (r, c+1).
     */
    public boolean hasWallRight(int r, int c) {
        if (!isValidPosition(r, c) || c + 1 >= cols) return true;
        return wallRight[r][c];
    }

    /**
     * Returns {@code true} if there is a wall between (r,c) and (r+1, c).
     */
    public boolean hasWallDown(int r, int c) {
        if (!isValidPosition(r, c) || r + 1 >= rows) return true;
        return wallDown[r][c];
    }

    /** Removes the wall between (r,c) and (r, c+1). */
    public void removeWallRight(int r, int c) {
        if (isValidPosition(r, c) && c + 1 < cols) wallRight[r][c] = false;
    }

    /** Removes the wall between (r,c) and (r+1, c). */
    public void removeWallDown(int r, int c) {
        if (isValidPosition(r, c) && r + 1 < rows) wallDown[r][c] = false;
    }

    /**
     * Returns {@code true} if the hero can move from (fromR, fromC) to
     * (toR, toC). Only cardinal moves of exactly one step are supported.
     */
    public boolean canMove(int fromR, int fromC, int toR, int toC) {
        if (!isValidPosition(toR, toC)) return false;
        int dr = toR - fromR;
        int dc = toC - fromC;
        if (dr == 0 && dc == 1)  return !hasWallRight(fromR, fromC);
        if (dr == 0 && dc == -1) return !hasWallRight(toR, toC);
        if (dr == 1 && dc == 0)  return !hasWallDown(fromR, fromC);
        if (dr == -1 && dc == 0) return !hasWallDown(toR, toC);
        return false;
    }

    // ------------------------------------------------------------------
    // Serialisation helpers (used by JsonPersistenceManager)
    // ------------------------------------------------------------------

    public boolean[][] getWallRight() { return wallRight; }
    public boolean[][] getWallDown()  { return wallDown; }

    // ------------------------------------------------------------------
    // Private
    // ------------------------------------------------------------------

    private void validateCoordinates(int row, int col) {
        if (!isValidPosition(row, col))
            throw new InvalidDungeonPositionException(row, col);
    }

    @Override
    public String toString() { return "Dungeon[" + rows + "x" + cols + "]"; }
}