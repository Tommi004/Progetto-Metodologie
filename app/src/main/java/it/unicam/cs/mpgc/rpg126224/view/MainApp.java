package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.persistence.JsonPersistenceManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application entry point.
 * Manages scene transitions between main menu, character creation and game.
 */
public class MainApp extends Application {

    private GameController controller;
    private Stage          primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.controller   = new GameController(new JsonPersistenceManager());
        stage.setTitle("Dungeon Protocol");
        stage.setMinWidth(940);
        stage.setMinHeight(660);
        showMainMenu();
        stage.show();
    }

    // -------------------------------------------------------------------------
    // Scene transitions
    // -------------------------------------------------------------------------

    private void showMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unicam/cs/mpgc/rpg126224/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            MainMenuViewController fxmlCtrl = loader.getController();
            fxmlCtrl.setup(controller,
                    this::showCharacterCreation,
                    this::showGame,
                    primaryStage::close);

            primaryStage.setScene(new Scene(root, 940, 660));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MainMenu.fxml", e);
        }
    }

    private void showCharacterCreation() {
        CharacterCreationView creation =
                new CharacterCreationView(controller, this::showGame);
        primaryStage.setScene(new Scene(creation.getRoot(), 940, 660));
    }

    private void showGame() {
        GameView game  = new GameView(controller, this::showMainMenu);
        Scene    scene = new Scene(game.getRoot(), 960, 680);
        game.setupKeyboard(scene);
        primaryStage.setScene(scene);
        Platform.runLater(game::refresh);
    }

    public static void main(String[] args) { launch(args); }
}