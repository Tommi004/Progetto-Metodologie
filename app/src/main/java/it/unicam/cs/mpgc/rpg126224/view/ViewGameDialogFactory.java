package it.unicam.cs.mpgc.rpg126224.view;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Factory for modal game dialogs (level advance, game over, victory).
 * Extracted from GameView to respect the Single Responsibility Principle:
 * GameView handles the main game screen; this class handles dialog creation only.
 */
public class ViewGameDialogFactory {

    private static final String FONT_MONO = "Monospace";

    private ViewGameDialogFactory() {}

    /**
     * Shows the "Floor Cleared" dialog when the hero advances to the next level.
     *
     * @param newLevel the level the hero is advancing to
     */
    public static void showLevelAdvance(int newLevel) {
        Stage dialog = buildBaseDialog("Floor Cleared!", 380, 280, "#0a0010");

        Label icon  = iconLabel("⚔");
        Label title = titleLabel("FLOOR CLEARED!", "#ffd700");
        title.setEffect(glow("#ffd700", 20));

        Label msg = bodyLabel("You defeated the boss!");
        Label sub = subLabel("Descending to floor " + newLevel + " of 3...", "#8080b0");

        Button okBtn = confirmButton("CONTINUE", "#8a6a00", "#ffd700");
        okBtn.setOnAction(e -> dialog.close());

        showWithFade(dialog, buildRoot("#0a0010", icon, title, msg, sub, okBtn));
    }

    /**
     * Shows the "Game Over" dialog when the hero is defeated.
     *
     * @param onReturnToMenu callback invoked when the player confirms
     */
    public static void showGameOver(Runnable onReturnToMenu) {
        Stage dialog = buildBaseDialog("Game Over", 380, 260, "#0a0000");

        Label icon  = iconLabel("💀");
        Label title = titleLabel("YOU DIED", "#e94560");
        title.setEffect(glow("#e94560", 20));

        Label msg = bodyLabel("The dungeon claimed another soul...");
        msg.setTextFill(Color.web("#a06060"));

        Button okBtn = confirmButton("RETURN TO MENU", "#6a0000", "#ff8080");
        okBtn.setOnAction(e -> { dialog.close(); onReturnToMenu.run(); });

        showWithFade(dialog, buildRoot("#0a0000", icon, title, msg, okBtn));
    }

    /**
     * Shows the "Victory" dialog when the hero completes the dungeon.
     *
     * @param onReturnToMenu callback invoked when the player confirms
     */
    public static void showVictory(Runnable onReturnToMenu) {
        Stage dialog = buildBaseDialog("Victory!", 380, 300, "#0a0a00");

        Label icon  = iconLabel("🏆");
        Label title = titleLabel("VICTORY!", "#ffd700");
        title.setEffect(glow("#ffd700", 20));

        Label msg = bodyLabel("You conquered the dungeon!");
        Label sub = subLabel("The Demon Lord is slain. Your name will be remembered forever!", "#a0a060");

        Button okBtn = confirmButton("RETURN TO MENU", "#8a6a00", "#ffd700");
        okBtn.setOnAction(e -> { dialog.close(); onReturnToMenu.run(); });

        showWithFade(dialog, buildRoot("#0a0a00", icon, title, msg, sub, okBtn));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static Stage buildBaseDialog(String title, int w, int h, String bg) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    private static VBox buildRoot(String bg, javafx.scene.Node... nodes) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: " + bg + ";");
        root.getChildren().addAll(nodes);
        return root;
    }

    private static void showWithFade(Stage dialog, VBox root) {
        FadeTransition ft = new FadeTransition(Duration.millis(400), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        dialog.setScene(new Scene(root,
                root.getPrefWidth() > 0 ? root.getPrefWidth() : 380,
                root.getPrefHeight() > 0 ? root.getPrefHeight() : 280));
        // Resize hint is embedded in the Stage creation
        ft.play();
        dialog.showAndWait();
    }

    private static Label iconLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(48));
        return l;
    }

    private static Label titleLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 28));
        l.setTextFill(Color.web(color));
        return l;
    }

    private static Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, 13));
        l.setTextFill(Color.web("#e0e0ff"));
        return l;
    }

    private static Label subLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, 11));
        l.setTextFill(Color.web(color));
        return l;
    }

    private static Button confirmButton(String text, String bg, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-family: " + FONT_MONO + "; " +
                "-fx-font-size: 13; " +
                "-fx-padding: 8 30; " +
                "-fx-background-radius: 4;"
        );
        return btn;
    }

    private static DropShadow glow(String color, double radius) {
        return new DropShadow(radius, Color.web(color));
    }
}