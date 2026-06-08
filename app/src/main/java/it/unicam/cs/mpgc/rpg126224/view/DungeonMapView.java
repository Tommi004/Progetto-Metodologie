package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

import java.io.IOException;

/**
 * Enhanced dungeon map rendered on a JavaFX Canvas.
 * Features: fog of war with radial gradient, corridors between rooms,
 * animated hero movement, pulsing enemy indicators, glow effects.
 *
 * The Canvas is embedded inside a themed FXML wrapper (DungeonMap.fxml)
 * managed by {@link DungeonMapViewController}, which applies a different
 * visual theme (colors, title, icon) for each of the 5 dungeon levels.
 *
 * Single responsibility: this class handles only Canvas rendering.
 * Theme and structural layout live in DungeonMapViewController / DungeonMap.fxml.
 */
public class DungeonMapView implements ViewRefreshable {

    private static final int CELL  = 52;   // cell size in px
    private static final int GAP   = 4;    // gap between cells
    private static final int STEP  = CELL + GAP;
    private static final int PAD   = 14;   // canvas padding

    private final GameController       controller;
    private final Canvas               canvas;
    private final Parent               root;
    private final DungeonMapViewController fxmlController;

    // Hero's animated position (in pixels, relative to canvas)
    private double heroX;
    private double heroY;
    private TranslateTransition moveAnim;

    public DungeonMapView(GameController controller) {
        this.controller = controller;

        Dungeon d = controller.getDungeon() != null ? controller.getDungeon() : null;
        int cols = d != null ? d.getCols() : 8;
        int rows = d != null ? d.getRows() : 8;

        double w = PAD * 2 + cols * STEP - GAP;
        double h = PAD * 2 + rows * STEP - GAP;

        canvas = new Canvas(w, h);

        // Load FXML wrapper
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/it/unicam/cs/mpgc/rpg126224/fxml/DungeonMap.fxml"));
            root = loader.load();
            fxmlController = loader.getController();
            fxmlController.setup(controller);

            // Place canvas inside the FXML placeholder StackPane
            javafx.scene.layout.StackPane placeholder = fxmlController.getCanvasPlaceholder();
            javafx.scene.layout.StackPane.setAlignment(canvas, javafx.geometry.Pos.TOP_LEFT);
            placeholder.getChildren().add(canvas);
            placeholder.setMinSize(w, h);
            placeholder.setPrefSize(w, h);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DungeonMap.fxml", e);
        }

        startIdleAnimation();
    }

    /**
     * Returns the themed FXML root to be placed in the scene.
     */
    public Parent getRoot() { return root; }

    // -------------------------------------------------------------------------
    // Public refresh
    // -------------------------------------------------------------------------

    @Override
    public void refresh() {
        if (controller.getCurrentState() == null) return;
        fxmlController.refresh();
        draw();
    }

    // -------------------------------------------------------------------------
    // Level theme for Canvas
    // -------------------------------------------------------------------------

    /**
     * Visual theme applied to the Canvas rendering for each dungeon level.
     *
     * @param bg           main background fill color
     * @param corridorColor color of corridors between visited rooms
     * @param fogColor      color of the fog-of-war overlay tint
     * @param unvisitedBg  background of unvisited cells
     * @param unvisitedStroke stroke of unvisited cells
     * @param textureColor  color used for procedural texture dots/marks
     */
    private record CanvasTheme(
            String bg,
            String corridorColor,
            String fogColor,
            String unvisitedBg,
            String unvisitedStroke,
            String textureColor
    ) {
        static CanvasTheme forLevel(int level) {
            return switch (level) {
                case 1 -> new CanvasTheme(  // Stone Dungeon — grey
                        "#08080e", "#1a1a2e", "#000008",
                        "#0a0a12", "#141428", "#2a2a44"
                );
                case 2 -> new CanvasTheme(  // Cursed Crypt — dark green
                        "#020e04", "#0a2a0e", "#00050000",
                        "#030f05", "#0a1e0c", "#103a14"
                );
                case 3 -> new CanvasTheme(  // Dragon's Lair — dark red/orange
                        "#0e0402", "#2e0a04", "#08000000",
                        "#100403", "#2a0804", "#3a1206"
                );
                case 4 -> new CanvasTheme(  // Abyssal Depths — deep blue
                        "#01040e", "#051228", "#00000800",
                        "#020510", "#081830", "#0a2040"
                );
                default -> new CanvasTheme( // Infernal Realm — purple
                        "#080010", "#200840", "#05000800",
                        "#090012", "#1a0830", "#2a1050"
                );
            };
        }
    }

    // -------------------------------------------------------------------------
    // Main draw
    // -------------------------------------------------------------------------

    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Dungeon dungeon    = controller.getDungeon();
        Hero hero          = controller.getHero();
        int level          = controller.getDungeonLevel();
        CanvasTheme theme  = CanvasTheme.forLevel(level);

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Themed background
        gc.setFill(Color.web(theme.bg()));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Procedural background texture
        drawBackgroundTexture(gc, level, theme);

        // Draw corridors first (below cells)
        drawCorridors(gc, dungeon, hero, theme);

        // Draw cells
        for (int r = 0; r < dungeon.getRows(); r++) {
            for (int c = 0; c < dungeon.getCols(); c++) {
                Room room      = dungeon.getRoom(r, c);
                boolean isHero = hero.getRow() == r && hero.getCol() == c;
                boolean isReachable = !isHero && dungeon.isValidPosition(r, c)
                        && isAdjacentToHero(r, c, hero);
                drawCell(gc, room, r, c, isHero, isReachable, hero, theme);
            }
        }

        // Draw hero sprite on top
        drawHeroSprite(gc, hero);

        // Fog of war overlay
        drawFogOfWar(gc, hero);
    }

    /** Returns true if (r,c) is directly adjacent (4 directions) to the hero. */
    private boolean isAdjacentToHero(int r, int c, Hero hero) {
        int dr = Math.abs(r - hero.getRow());
        int dc = Math.abs(c - hero.getCol());
        return (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
    }

    // -------------------------------------------------------------------------
    // Corridors
    // -------------------------------------------------------------------------

    /**
     * Draws a procedural background texture on the canvas based on the dungeon level.
     * Each level has a unique pattern: stone cracks (L1), rune dots (L2),
     * ember sparks (L3), bubble rings (L4), arcane sigils (L5).
     */
    private void drawBackgroundTexture(GraphicsContext gc, int level, CanvasTheme theme) {
        gc.setStroke(Color.web(theme.textureColor(), 0.35));
        gc.setLineWidth(0.6);
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        switch (level) {
            case 1 -> { // Stone cracks: irregular diagonal lines
                for (int i = 0; i < 18; i++) {
                    double x = (i * 37.3) % w;
                    double y = (i * 53.7) % h;
                    gc.strokeLine(x, y, x + 12 + (i % 5) * 3, y + 8 + (i % 4) * 2);
                    gc.strokeLine(x + 6, y + 2, x + 6, y + 14 + (i % 3) * 2);
                }
            }
            case 2 -> { // Rune dots: small circles in a loose grid
                gc.setFill(Color.web(theme.textureColor(), 0.25));
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 7; j++) {
                        double x = 20 + i * (w / 7);
                        double y = 20 + j * (h / 7);
                        gc.fillOval(x, y, 3, 3);
                        gc.strokeOval(x - 5, y - 5, 13, 13);
                    }
                }
            }
            case 3 -> { // Ember sparks: small scattered dots + upward lines
                gc.setFill(Color.web("#ff4400", 0.2));
                for (int i = 0; i < 30; i++) {
                    double x = (i * 29.7 + 10) % w;
                    double y = (i * 43.1 + 10) % h;
                    double s = 1.5 + (i % 3);
                    gc.fillOval(x, y, s, s);
                    gc.strokeLine(x + s / 2, y, x + s / 2, y - 6 - (i % 4));
                }
            }
            case 4 -> { // Bubble rings: concentric circles
                for (int i = 0; i < 8; i++) {
                    double x = (i * 71.3 + 15) % (w - 20);
                    double y = (i * 59.7 + 15) % (h - 20);
                    gc.strokeOval(x, y, 14, 14);
                    gc.strokeOval(x + 4, y + 4, 6, 6);
                }
            }
            default -> { // Arcane sigils L5: X marks + small diamonds
                for (int i = 0; i < 10; i++) {
                    double x = (i * 61.1 + 20) % (w - 20);
                    double y = (i * 47.3 + 20) % (h - 20);
                    gc.strokeLine(x - 6, y - 6, x + 6, y + 6);
                    gc.strokeLine(x + 6, y - 6, x - 6, y + 6);
                    gc.strokeLine(x, y - 8, x + 6, y);
                    gc.strokeLine(x + 6, y, x, y + 8);
                    gc.strokeLine(x, y + 8, x - 6, y);
                    gc.strokeLine(x - 6, y, x, y - 8);
                }
            }
        }
    }

    private void drawCorridors(GraphicsContext gc, Dungeon dungeon, Hero hero,
                                CanvasTheme theme) {
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

                    gc.setStroke(Color.web(theme.corridorColor()));
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
                          boolean isHero, boolean isReachable, Hero hero, CanvasTheme theme) {
        double x = PAD + c * STEP;
        double y = PAD + r * STEP;

        if (!room.isVisited()) {
            drawUnvisitedCell(gc, x, y, theme);
            // Highlight unvisited reachable cells with a subtle white pulse
            if (isReachable) drawReachableHighlight(gc, x, y, "#ffffff", 0.18);
            return;
        }

        drawVisitedCell(gc, room, x, y);

        // Room icon (skip hero cell — hero is drawn separately)
        if (!isHero) {
            drawCellIcon(gc, room, x, y);
        }

        // Highlight reachable visited cells
        if (isReachable) {
            String highlightColor = switch (room.getType()) {
                case ENEMY    -> room.isCleared() ? "#44ff44" : "#ff4444";
                case TREASURE -> room.isCleared() ? "#808040" : "#ffd700";
                case EXIT     -> "#ffd700";
                case TRAP     -> room.hasTrap()   ? "#ff8800" : "#44ff44";
                default       -> "#4488ff";
            };
            drawReachableHighlight(gc, x, y, highlightColor, 0.55);
        }
    }

    /**
     * Draws a coloured highlight border around a reachable adjacent cell.
     *
     * @param gc      the graphics context
     * @param x       cell top-left x
     * @param y       cell top-left y
     * @param color   hex color string for the highlight
     * @param opacity opacity of the highlight (0.0–1.0)
     */
    private void drawReachableHighlight(GraphicsContext gc, double x, double y,
                                        String color, double opacity) {
        // Outer glow
        gc.setStroke(Color.web(color, opacity * 0.4));
        gc.setLineWidth(4);
        strokeRoundRect(gc, x - 2, y - 2, CELL + 4, CELL + 4, 9);

        // Inner border
        gc.setStroke(Color.web(color, opacity));
        gc.setLineWidth(1.5);
        strokeRoundRect(gc, x, y, CELL, CELL, 6);

        // Small corner dots for a "target" feel
        double dotSize = 3;
        gc.setFill(Color.web(color, opacity));
        gc.fillOval(x + 2,          y + 2,          dotSize, dotSize);
        gc.fillOval(x + CELL - 5,   y + 2,          dotSize, dotSize);
        gc.fillOval(x + 2,          y + CELL - 5,   dotSize, dotSize);
        gc.fillOval(x + CELL - 5,   y + CELL - 5,   dotSize, dotSize);
    }

    private void drawUnvisitedCell(GraphicsContext gc, double x, double y,
                                   CanvasTheme theme) {
        gc.setFill(Color.web(theme.unvisitedBg()));
        fillRoundRect(gc, x, y, CELL, CELL, 7);
        gc.setStroke(Color.web(theme.unvisitedStroke()));
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
        } else if (room.getType() == RoomType.SHOP) {
            bgColor = room.isCleared() ? "#141408" : "#1a1a00";
            strokeColor = room.isCleared() ? "#2a2810" : "#5a5000"; strokeWidth = 2;
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
            case TRAP -> {
                if (!room.hasTrap()) { icon = "·"; color = "#2a2a50"; size = 12; }
                else { icon = "·"; color = "#2a2a50"; size = 12; }
            }
            case SHOP -> {
                if (room.isCleared()) { icon = "✓"; color = "#2a6a2a"; size = 16; }
                else { icon = "🛒"; color = "#ffd700"; size = 20; }
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
            case GOBLIN     -> "👺";
            case SKELETON   -> "💀";
            case DARK_MAGE  -> "🧙";
            case TROLL      -> "👹";
            case ASSASSIN   -> "🗡";
            case DRAGON     -> "🐉";
            case KNIGHT     -> "⚔";
            case WITCH      -> "🪄";
            case DEMON      -> "😈";
            case LEVIATHAN  -> "🐋";
            case DEMON_LORD -> "👑";
            case DEMON_SOUL -> "💀";
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