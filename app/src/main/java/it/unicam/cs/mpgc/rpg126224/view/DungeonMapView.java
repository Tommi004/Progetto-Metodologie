package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Enhanced dungeon map rendered on a JavaFX Canvas.
 * Features: fog of war with radial gradient, corridors between rooms,
 * animated hero movement, pulsing enemy indicators, glow effects.
 */
public class DungeonMapView extends Pane implements ViewRefreshable {

    private static final int CELL  = 52;   // cell size in px
    private static final int GAP   = 4;    // gap between cells
    private static final int STEP  = CELL + GAP;
    private static final int PAD   = 14;   // canvas padding

    private final GameController controller;
    private final Canvas canvas;

    // Hero's animated position (in pixels, relative to canvas)
    private double heroX;
    private double heroY;
    private TranslateTransition moveAnim;

    public DungeonMapView(GameController controller) {
        this.controller = controller;

        Dungeon d = controller.getDungeon() != null
                ? controller.getDungeon()
                : null;
        int cols = d != null ? d.getCols() : 8;
        int rows = d != null ? d.getRows() : 8;

        double w = PAD * 2 + cols * STEP - GAP;
        double h = PAD * 2 + rows * STEP - GAP;

        canvas = new Canvas(w, h);
        getChildren().add(canvas);
        setPrefSize(w, h);

        startIdleAnimation();
    }

    // -------------------------------------------------------------------------
    // Public refresh
    // -------------------------------------------------------------------------

    @Override
    public void refresh() {
        if (controller.getCurrentState() == null) return;
        draw();
    }

    // -------------------------------------------------------------------------
    // Main draw
    // -------------------------------------------------------------------------

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Dungeon dungeon    = controller.getDungeon();
        Hero hero          = controller.getHero();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Background
        gc.setFill(Color.web("#06060f"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw corridors first (below cells)
        drawCorridors(gc, dungeon, hero);

        // Draw cells
        for (int r = 0; r < dungeon.getRows(); r++) {
            for (int c = 0; c < dungeon.getCols(); c++) {
                Room room   = dungeon.getRoom(r, c);
                boolean isHero = hero.getRow() == r && hero.getCol() == c;
                drawCell(gc, room, r, c, isHero, hero);
            }
        }

        // Draw hero sprite on top
        drawHeroSprite(gc, hero);

        // Fog of war overlay
        drawFogOfWar(gc, hero);
    }

    // -------------------------------------------------------------------------
    // Corridors
    // -------------------------------------------------------------------------

    private void drawCorridors(GraphicsContext gc, Dungeon dungeon, Hero hero) {
        for (int r = 0; r < dungeon.getRows(); r++) {
            for (int c = 0; c < dungeon.getCols(); c++) {
                Room room = dungeon.getRoom(r, c);
                if (!room.isVisited()) continue;

                double cx = PAD + c * STEP + CELL / 2.0;
                double cy = PAD + r * STEP + CELL / 2.0;

                int[][] dirs = {{0, 1}, {1, 0}};
                for (int[] dir : dirs) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];
                    if (!dungeon.isValidPosition(nr, nc)) continue;
                    Room neighbor = dungeon.getRoom(nr, nc);
                    if (!neighbor.isVisited()) continue;

                    double nx = PAD + nc * STEP + CELL / 2.0;
                    double ny = PAD + nr * STEP + CELL / 2.0;

                    gc.setStroke(Color.web("#1a1a2e"));
                    gc.setLineWidth(GAP + 2);
                    gc.strokeLine(cx, cy, nx, ny);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cell drawing
    // -------------------------------------------------------------------------

    private void drawCell(GraphicsContext gc, Room room, int r, int c,
                          boolean isHero, Hero hero) {
        double x = PAD + c * STEP;
        double y = PAD + r * STEP;

        if (!room.isVisited()) {
            drawUnvisitedCell(gc, x, y);
            return;
        }

        drawVisitedCell(gc, room, x, y);

        // Room icon (skip hero cell — hero is drawn separately)
        if (!isHero) {
            drawCellIcon(gc, room, x, y);
        }
    }

    private void drawUnvisitedCell(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#080810"));
        fillRoundRect(gc, x, y, CELL, CELL, 7);
        gc.setStroke(Color.web("#111122"));
        gc.setLineWidth(1);
        strokeRoundRect(gc, x, y, CELL, CELL, 7);
        // Question mark
        gc.setFill(Color.web("#1a1a35"));
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        drawCenteredText(gc, "?", x, y, CELL, CELL);
    }

    private void drawVisitedCell(GraphicsContext gc, Room room, double x, double y) {
        // Pick colors based on room type and state
        String bgColor, strokeColor;
        double strokeWidth = 1.5;

        if (room.getType() == RoomType.START) {
            bgColor = "#0d2a0d"; strokeColor = "#2a6a2a";
        } else if (room.getType() == RoomType.EXIT) {
            bgColor = "#2a1a00"; strokeColor = "#ffd700"; strokeWidth = 2;
        } else if (room.getType() == RoomType.TREASURE) {
            bgColor = room.isCleared() ? "#141408" : "#1e1a00";
            strokeColor = room.isCleared() ? "#2a2810" : "#5a4800";
        } else if (room.getType() == RoomType.ENEMY) {
            bgColor = room.isCleared() ? "#0d150d" : "#200808";
            strokeColor = room.isCleared() ? "#1a3a1a" : "#5a1515";
        } else {
            bgColor = "#0e0e1e"; strokeColor = "#1c1c38";
        }

        // Stone tile texture (subtle grid lines)
        gc.setFill(Color.web(bgColor));
        fillRoundRect(gc, x, y, CELL, CELL, 6);

        // Inner tile lines for stone texture
        gc.setStroke(Color.web(strokeColor, 0.25));
        gc.setLineWidth(0.5);
        for (int i = 1; i < 3; i++) {
            gc.strokeLine(x + i * (CELL / 3.0), y + 2, x + i * (CELL / 3.0), y + CELL - 2);
            gc.strokeLine(x + 2, y + i * (CELL / 3.0), x + CELL - 2, y + i * (CELL / 3.0));
        }

        // Border
        gc.setStroke(Color.web(strokeColor));
        gc.setLineWidth(strokeWidth);
        strokeRoundRect(gc, x, y, CELL, CELL, 6);

        // Glow for exit
        if (room.getType() == RoomType.EXIT) {
            gc.setStroke(Color.web("#ffd700", 0.3));
            gc.setLineWidth(4);
            strokeRoundRect(gc, x - 2, y - 2, CELL + 4, CELL + 4, 8);
        }
    }

    private void drawCellIcon(GraphicsContext gc, Room room, double x, double y) {
        String icon;
        String color;
        int size = 20;

        switch (room.getType()) {
            case START -> { icon = "⬆"; color = "#66ff66"; }
            case EXIT  -> {
                icon  = room.isCleared() ? "🚪" : "🔒";
                color = "#ffd700";
                size  = 22;
            }
            case ENEMY -> {
                if (room.isCleared()) { icon = "✓"; color = "#2a6a2a"; size = 16; }
                else { icon = getEnemyIcon(room); color = "#ff4444"; size = 22; }
            }
            case TREASURE -> {
                if (room.isCleared()) { icon = "·"; color = "#3a3820"; size = 14; }
                else { icon = "💰"; color = "#ffd700"; size = 20; }
            }
            default -> { icon = "·"; color = "#2a2a50"; size = 12; }
        }

        gc.setFill(Color.web(color));
        gc.setFont(Font.font("Segoe UI Emoji", size));
        drawCenteredText(gc, icon, x, y, CELL, CELL);
    }

    private String getEnemyIcon(Room room) {
        if (room.getEnemies().isEmpty()) return "!";
        return switch (room.getEnemies().get(0).getType()) {
            case GOBLIN    -> "👺";
            case SKELETON  -> "💀";
            case DARK_MAGE -> "🧙";
            case TROLL     -> "👹";
            case ASSASSIN  -> "🗡";
            case DRAGON    -> "🐉";
        };
    }

    // -------------------------------------------------------------------------
    // Hero sprite
    // -------------------------------------------------------------------------

    private void drawHeroSprite(GraphicsContext gc, Hero hero) {
        double x = PAD + hero.getCol() * STEP;
        double y = PAD + hero.getRow() * STEP;

        // Glow ring
        gc.setStroke(Color.web("#00e5ff", 0.6));
        gc.setLineWidth(2.5);
        strokeRoundRect(gc, x + 2, y + 2, CELL - 4, CELL - 4, 8);

        // Hero cell background
        gc.setFill(Color.web("#003344"));
        fillRoundRect(gc, x, y, CELL, CELL, 6);

        // Hero emoji
        String heroIcon = switch (hero.getHeroClass()) {
            case WARRIOR -> "⚔";
            case MAGE    -> "🔮";
            case ARCHER  -> "🏹";
        };
        gc.setFont(Font.font("Segoe UI Emoji", 24));
        gc.setFill(Color.web("#00e5ff"));
        drawCenteredText(gc, heroIcon, x, y, CELL, CELL);
    }

    // -------------------------------------------------------------------------
    // Fog of war
    // -------------------------------------------------------------------------

    private void drawFogOfWar(GraphicsContext gc, Hero hero) {
        double heroPixelX = PAD + hero.getCol() * STEP + CELL / 2.0;
        double heroPixelY = PAD + hero.getRow() * STEP + CELL / 2.0;

        double radius = STEP * 2.8;

        RadialGradient fog = new RadialGradient(
                0, 0,
                heroPixelX, heroPixelY,
                radius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(0.55, Color.TRANSPARENT),
                new Stop(0.75, Color.web("#06060f", 0.55)),
                new Stop(1.0,  Color.web("#06060f", 0.88))
        );

        gc.setFill(fog);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    // -------------------------------------------------------------------------
    // Idle animation (redraws periodically for pulsing effects)
    // -------------------------------------------------------------------------

    private void startIdleAnimation() {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(800), e -> {
                    if (controller.getCurrentState() != null) draw();
                })
        );
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
    }

    // -------------------------------------------------------------------------
    // Canvas drawing helpers
    // -------------------------------------------------------------------------

    private void fillRoundRect(GraphicsContext gc, double x, double y,
                               double w, double h, double r) {
        gc.fillRoundRect(x, y, w, h, r, r);
    }

    private void strokeRoundRect(GraphicsContext gc, double x, double y,
                                 double w, double h, double r) {
        gc.strokeRoundRect(x, y, w, h, r, r);
    }

    private void drawCenteredText(GraphicsContext gc, String text,
                                  double x, double y, double w, double h) {
        var fm = gc.getFont();
        // Approximate vertical centering
        gc.fillText(text, x + w / 2.0 - estimateTextWidth(text, gc) / 2.0,
                y + h / 2.0 + 7);
    }

    private double estimateTextWidth(String text, GraphicsContext gc) {
        // Rough estimate: font size * 0.6 per char
        return text.length() * gc.getFont().getSize() * 0.6;
    }
}