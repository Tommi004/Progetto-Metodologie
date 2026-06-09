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
 *
 * <p>Scene transitions preserve the current window state (fullscreen,
 * maximised, size) by saving it before swapping the scene and restoring
 * it immediately after via {@link Platform#runLater}.</p>
 */
public class MainApp extends Application {

    private static final double DEFAULT_W = 940;
    private static final double DEFAULT_H = 660;

    private GameController controller;
    private Stage          primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.controller   = new GameController(new JsonPersistenceManager());
        stage.setTitle("Level Up!");
        stage.setMinWidth(DEFAULT_W);
        stage.setMinHeight(DEFAULT_H);
        showMainMenu();
        stage.show();
    }

    // -------------------------------------------------------------------------
    // Scene transitions
    // -------------------------------------------------------------------------

    private void showMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/it/unicam/cs/mpgc/rpg126224/fxml/MainMenu.fxml"));
            Parent root = loader.load();
            MainMenuViewController fxmlCtrl = loader.getController();
            fxmlCtrl.setup(controller,
                    this::showCharacterCreation,
                    this::showGame,
                    primaryStage::close);
            setScene(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MainMenu.fxml", e);
        }
    }

    private void showCharacterCreation() {
        CharacterCreationView creation =
                new CharacterCreationView(controller, this::showIntro, this::showMainMenu);
        setScene(creation.getRoot());
    }

    private void showIntro() {
        IntroView intro = new IntroView(this::showGame);
        setScene(intro.getRoot());
    }

    private void showGame() {
        GameView game  = new GameView(controller, this::showMainMenu);
        Scene    scene = buildScene(game.getRoot());
        game.setupKeyboard(scene);
        setScenePreservingState(scene);
        Platform.runLater(game::refresh);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a Scene using the current Stage dimensions (or defaults),
     * then swaps it preserving fullscreen/maximised state.
     */
    private void setScene(Parent root) {
        setScenePreservingState(buildScene(root));
    }

    /**
     * Builds a Scene sized to the current Stage dimensions.
     * This ensures the root node fills the entire window regardless of
     * its preferred size.
     */
    private Scene buildScene(Parent root) {
        double w = primaryStage.getWidth();
        double h = primaryStage.getHeight();
        // Use defaults on first call (stage not yet shown → NaN)
        if (Double.isNaN(w) || w < DEFAULT_W) w = DEFAULT_W;
        if (Double.isNaN(h) || h < DEFAULT_H) h = DEFAULT_H;
        return new Scene(root, w, h);
    }

    /**
     * Swaps the primary stage's scene while preserving fullscreen/maximised state.
     */
    private void setScenePreservingState(Scene scene) {
        boolean wasFullScreen = primaryStage.isFullScreen();
        boolean wasMaximised  = primaryStage.isMaximized();

        primaryStage.setScene(scene);

        if (wasFullScreen || wasMaximised) {
            Platform.runLater(() -> {
                primaryStage.setMaximized(wasMaximised);
                primaryStage.setFullScreen(wasFullScreen);
            });
        }
    }

    public static void main(String[] args) { launch(args); }
}