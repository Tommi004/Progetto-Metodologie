package it.unicam.cs.mpgc.rpg126224.model;

import it.unicam.cs.mpgc.rpg126224.exception.InvalidDungeonPositionException;
import java.util.Objects;

/**
 * Represents the dungeon as a 2D grid of rooms.
 */
public class Dungeon {

    public static final int SIZE = 8;

    private final Room[][] grid;
    private final int rows;
    private final int cols;

    public Dungeon() {
        this(SIZE, SIZE);
    }

    public Dungeon(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Room[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Room(r, c, RoomType.EMPTY);
            }
        }
    }

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

    private void validateCoordinates(int row, int col) {
        if (!isValidPosition(row, col)) {
            throw new InvalidDungeonPositionException(row, col);
        }
    }

    @Override
    public String toString() {
        return "Dungeon[" + rows + "x" + cols + "]";
    }
}