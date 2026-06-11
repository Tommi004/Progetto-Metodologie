package it.unicam.cs.mpgc.rpg126224.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Intro crawl screen shown after character creation and before the game starts.
 * Text scrolls from bottom to top in Star Wars style.
 * The player can skip by clicking the SKIP button.
 */
public class IntroView {

    private static final String[] LINES = {
        "A long time ago,",
        "in a kingdom forgotten by light...",
        "",
        "An unknown dungeon has swallowed countless heroes.",
        "None have returned.",
        "None have ever spoken of what lies within.",
        "",
        "Ancient evil stirs in its deepest chambers.",
        "The Demon Lord grows stronger with each passing day,",
        "feeding on the souls of the fallen.",
        "",
        "You are the last hope.",
        "Armed with courage and faith, you descend fearlessly",
        "through darkness, monsters, and legends.",
        "",
        "Your name is not yet known.",
        "But the dungeon will remember.",
    };

    private final StackPane root;
    private TranslateTransition scrollAnim;

    public IntroView(Runnable onFinished) {
        root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Build text block
        VBox textBox = new VBox(14);
        textBox.setAlignment(Pos.CENTER);
        textBox.setPadding(new Insets(0, 80, 0, 80));

        for (String line : LINES) {
            Text text = new Text(line);
            text.setTextAlignment(TextAlignment.CENTER);
            text.setFill(Color.web("#ffd700"));
            if (line.equals("A long time ago,") || line.equals("in a kingdom forgotten by light...")) {
                text.setFont(Font.font("Monospace", FontPosture.ITALIC, 18));
            } else if (line.isEmpty()) {
                text.setFont(Font.font(10));
            } else {
                text.setFont(Font.font("Monospace", 15));
            }
            textBox.getChildren().add(text);
        }

        // Position text below screen initially
        textBox.setTranslateY(700);

        // Scroll animation — from bottom to top
        scrollAnim = new TranslateTransition(Duration.seconds(28), textBox);
        scrollAnim.setFromY(700);
        scrollAnim.setToY(-900);
        scrollAnim.setInterpolator(Interpolator.LINEAR);
        scrollAnim.setOnFinished(e -> onFinished.run());
        scrollAnim.play();

        // SKIP button
        Button skipBtn = new Button("SKIP  ▶");
        skipBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #ffd700; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-border-color: #ffd700; -fx-border-radius: 4; " +
                "-fx-background-radius: 4; -fx-padding: 6 14; " +
                "-fx-cursor: hand; -fx-opacity: 0.7;"
        );
        skipBtn.setOnMouseEntered(e -> skipBtn.setStyle(
                "-fx-background-color: #ffd700; " +
                "-fx-text-fill: #000000; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-border-color: #ffd700; -fx-border-radius: 4; " +
                "-fx-background-radius: 4; -fx-padding: 6 14; " +
                "-fx-cursor: hand;"
        ));
        skipBtn.setOnMouseExited(e -> skipBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #ffd700; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-border-color: #ffd700; -fx-border-radius: 4; " +
                "-fx-background-radius: 4; -fx-padding: 6 14; " +
                "-fx-cursor: hand; -fx-opacity: 0.7;"
        ));
        skipBtn.setOnAction(e -> {
            scrollAnim.stop();
            // Fade out then call onFinished
            FadeTransition fade = new FadeTransition(Duration.millis(400), root);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> onFinished.run());
            fade.play();
        });

        StackPane.setAlignment(skipBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(skipBtn, new Insets(0, 30, 30, 0));

        // Fade in the whole screen
        root.setOpacity(0);
        root.getChildren().addAll(textBox, skipBtn);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public Parent getRoot() { return root; }
}