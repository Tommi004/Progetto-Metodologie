package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Entry point for the hero status side panel.
 * Loads the scene from FXML and injects dependencies into
 * {@link HeroStatusViewController}.
 *
 * Single responsibility: load FXML and wire the controller.
 * All UI logic lives in HeroStatusViewController;
 * all structure lives in HeroStatus.fxml.
 */
public class HeroStatusView implements ViewRefreshable {

    private final Parent root;
    private final HeroStatusViewController fxmlController;

    public HeroStatusView(GameController gameController) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unicam/cs/mpgc/rpg126224/fxml/HeroStatus.fxml"));
            root = loader.load();
            fxmlController = loader.getController();
            fxmlController.setup(gameController);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HeroStatus.fxml", e);
        }
    }

    /**
     * Returns the root node to be placed in the scene (left panel of GameView).
     */
    public Parent getRoot() {
        return root;
    }

    @Override
    public void refresh() {
        fxmlController.refresh();
    }
}