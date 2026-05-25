package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.persistence.JsonPersistenceManager;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main JavaFX application entry point for Dungeon Protocol.
 */
public class MainApp extends Application {

    private GameController controller;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.controller = new GameController(new JsonPersistenceManager());
        showMainMenu();
        stage.setTitle("Dungeon Protocol");
        stage.setMinWidth(940);
        stage.setMinHeight(660);
        stage.show();
    }

    private void showMainMenu() {
        VBox menu = new VBox(18);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(60));
        menu.setStyle("-fx-background-color: #0a0a1e;");

        Label title = new Label("DUNGEON PROTOCOL");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 44));
        title.setTextFill(Color.web("#e94560"));

        javafx.scene.effect.DropShadow glow =
                new javafx.scene.effect.DropShadow(20, Color.web("#e94560"));
        title.setEffect(glow);
        Timeline glowAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 10)),
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(glow.radiusProperty(), 28))
        );
        glowAnim.setAutoReverse(true);
        glowAnim.setCycleCount(Animation.INDEFINITE);
        glowAnim.play();

        Label tagline = new Label("Enter the dungeon. Survive or perish.");
        tagline.setFont(Font.font("Monospace", 15));
        tagline.setTextFill(Color.web("#404060"));

        Button newGameBtn  = menuButton("New Game",  "#c0392b");
        Button loadGameBtn = menuButton("Load Game", "#1a5276");
        Button quitBtn     = menuButton("Quit",      "#2a2a3a");

        loadGameBtn.setDisable(!controller.getPersistenceManager().hasSaveFile());

        newGameBtn.setOnAction(e  -> showCharacterCreation());
        loadGameBtn.setOnAction(e -> { if (controller.loadGame()) showGame(); });
        quitBtn.setOnAction(e     -> primaryStage.close());

        VBox buttons = new VBox(10, newGameBtn, loadGameBtn, quitBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(800), buttons);
        fade.setToValue(1);
        fade.play();

        menu.getChildren().addAll(title, tagline, new Separator(), buttons);
        primaryStage.setScene(new Scene(menu, 940, 660));
    }

    private void showCharacterCreation() {
        CharacterCreationView creation = new CharacterCreationView(controller, this::showGame);
        primaryStage.setScene(new Scene(creation, 940, 660));
    }

    private void showGame() {
        GameView game = new GameView(controller, this::showMainMenu);
        primaryStage.setScene(new Scene(game, 960, 680));
        game.requestFocus();
    }

    private Button menuButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(280);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #e0e0ff; " +
                "-fx-font-family: Monospace; -fx-font-size: 15; -fx-padding: 13 24; " +
                "-fx-background-radius: 6;");
        return btn;
    }

    public static void main(String[] args) { launch(args); }
}