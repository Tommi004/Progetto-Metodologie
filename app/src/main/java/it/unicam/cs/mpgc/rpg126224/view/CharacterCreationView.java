package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Entry point for the character creation screen.
 * Loads the scene from FXML and injects dependencies into
 * {@link CharacterCreationViewController}.
 */
public class CharacterCreationView {

    private final Parent root;
    private final CharacterCreationViewController fxmlController;

    public CharacterCreationView(GameController gameController, Runnable onGameStarted) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/it/unicam/cs/mpgc/rpg126224/fxml/CharacterCreation.fxml"));
            root = loader.load();
            fxmlController = loader.getController();
            fxmlController.setup(gameController, onGameStarted);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load CharacterCreation.fxml", e);
        }
    }

    public Parent getRoot() {
        return root;
    }
}