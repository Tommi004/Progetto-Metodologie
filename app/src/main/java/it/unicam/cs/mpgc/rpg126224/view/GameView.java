package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Entry point for the main game screen.
 * Loads the scene from FXML, wires the controller and exposes the root
 * node and refresh delegate.
 *
 * Single responsibility: load FXML and wire dependencies.
 * All UI logic lives in {@link GameViewController};
 * all structure lives in GameView.fxml.
 */
public class GameView implements ViewRefreshable {

    private final Parent             root;
    private final GameViewController fxmlController;

    public GameView(GameController gameController, Runnable onReturnToMenu) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unicam/cs/mpgc/rpg126224/fxml/GameView.fxml"));
            root = loader.load();
            fxmlController = loader.getController();
            fxmlController.setup(gameController, onReturnToMenu);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load GameView.fxml", e);
        }
    }

    /** Returns the root node to be set as the scene content. */
    public Parent getRoot() {
        return root;
    }

    /**
     * Wires keyboard input to the scene.
     * Must be called after the root has been added to a Scene.
     */
    public void setupKeyboard(Scene scene) {
        fxmlController.setupKeyboardInput(scene);
    }

    @Override
    public void refresh() {
        fxmlController.refresh();
    }
}