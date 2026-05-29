package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.Enemy;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Entry point for the combat modal screen.
 * Loads the scene from FXML, wires the controller and shows the stage.
 *
 * Single responsibility: load FXML, configure the Stage and delegate
 * everything else to {@link CombatViewController}.
 */
public class CombatView {

    private final GameController gameController;
    private final Enemy          enemy;
    private final Runnable       onCombatEnd;

    public CombatView(GameController gameController, Enemy enemy, Runnable onCombatEnd) {
        this.gameController = gameController;
        this.enemy          = enemy;
        this.onCombatEnd    = onCombatEnd;
    }

    public void show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/it/unicam/cs/mpgc/rpg126224/fxml/Combat.fxml"));
            Parent root = loader.load();

            Stage stage = buildStage(root);

            CombatViewController fxmlController = loader.getController();
            fxmlController.setup(gameController, enemy, stage, onCombatEnd);

            playEntranceAnimation(root);
            stage.showAndWait();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load Combat.fxml", e);
        }
    }

    private Stage buildStage(Parent root) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("⚔ Combat — " + enemy.getName());
        stage.setResizable(false);
        stage.setScene(new Scene(root, 580, 480));
        return stage;
    }

    private void playEntranceAnimation(Parent root) {
        root.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}