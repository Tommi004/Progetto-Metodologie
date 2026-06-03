package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * FXML controller for the dungeon map wrapper.
 * Applies a visual theme (colors, title, icon, border glow) per dungeon level.
 * Particles are handled directly in DungeonMapView on the main Canvas.
 */
public class DungeonMapViewController implements ViewRefreshable {

    @FXML private BorderPane rootPane;
    @FXML private HBox       headerBox;
    @FXML private Label      levelIconLabel;
    @FXML private Label      dungeonNameLabel;
    @FXML private Label      levelLabel;
    @FXML private Pane       darkOverlay;
    @FXML private StackPane  canvasPlaceholder;

    private GameController gameController;
    private int            currentLevel = -1;
    private Timeline       borderTimeline;
    private final DropShadow borderGlow = new DropShadow();

    public void setup(GameController gameController) {
        this.gameController = gameController;
        refresh();
    }

    public StackPane getCanvasPlaceholder() { return canvasPlaceholder; }

    @Override
    public void refresh() {
        if (gameController == null || gameController.getCurrentState() == null) return;
        int level = gameController.getDungeonLevel();
        if (level != currentLevel) {
            currentLevel = level;
            applyTheme(level);
        }
    }

    private void applyTheme(int level) {
        LevelTheme theme = LevelTheme.forLevel(level);

        levelIconLabel.setText(theme.icon());
        dungeonNameLabel.setText(theme.name());
        dungeonNameLabel.setStyle(
                "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-font-weight: bold; -fx-text-fill: " + theme.titleColor() + ";"
        );
        levelLabel.setText("— Floor " + level + " / 5");

        headerBox.setStyle(
                "-fx-padding: 6 12 6 12; " +
                "-fx-background-color: " + theme.headerBg() + "; " +
                "-fx-border-color: " + theme.borderColor() + "; " +
                "-fx-border-width: 0 0 2 0;"
        );

        rootPane.setStyle(
                "-fx-background-color: " + theme.bg() + "; " +
                "-fx-border-color: " + theme.borderColor() + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6;"
        );

        // Animated border glow
        if (borderTimeline != null) borderTimeline.stop();
        borderGlow.setColor(Color.web(theme.borderColor()));
        borderGlow.setRadius(6);
        borderGlow.setSpread(0.3);
        rootPane.setEffect(borderGlow);

        borderTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(borderGlow.radiusProperty(), 8)),
                new KeyFrame(Duration.millis(1800), new KeyValue(borderGlow.radiusProperty(), 45))
        );
        borderTimeline.setAutoReverse(true);
        borderTimeline.setCycleCount(Timeline.INDEFINITE);
        borderTimeline.play();
    }

    private record LevelTheme(
            String name, String icon, String bg,
            String headerBg, String borderColor, String titleColor
    ) {
        static LevelTheme forLevel(int level) {
            return switch (level) {
                case 1 -> new LevelTheme("Stone Dungeon", "🪨",
                        "#0d0d0d", "#111111", "#4a4a4a", "#a0a0a0");
                case 2 -> new LevelTheme("Cursed Crypt", "💀",
                        "#050f05", "#071007", "#1a5c1a", "#44cc44");
                case 3 -> new LevelTheme("Dragon's Lair", "🐉",
                        "#110404", "#160505", "#7a1a1a", "#ff6644");
                case 4 -> new LevelTheme("Abyssal Depths", "🌊",
                        "#03080f", "#040a14", "#0a3a6a", "#44aaff");
                default -> new LevelTheme("Infernal Realm", "👑",
                        "#0f0515", "#130618", "#5a0a8a", "#cc44ff");
            };
        }
    }
}