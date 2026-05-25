package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import java.util.List;

/**
 * Main game screen showing dungeon map, hero status and movement controls.
 */
public class GameView extends BorderPane implements ViewRefreshable {

    private final GameController controller;
    private final Runnable onReturnToMenu;
    private final DungeonMapView mapView;
    private final HeroStatusView heroStatus;
    private final Label roomInfoLabel = new Label();
    private final Label messageLabel  = new Label();

    public GameView(GameController controller, Runnable onReturnToMenu) {
        this.controller = controller;
        this.onReturnToMenu = onReturnToMenu;
        this.mapView = new DungeonMapView(controller);
        this.heroStatus = new HeroStatusView(controller);
        buildUI();
        refresh();
        setupKeyboardInput();
    }

    private void buildUI() {
        setStyle("-fx-background-color: #0a0a1a;");
        setTop(buildTopBar());
        setLeft(heroStatus);
        setCenter(buildCenterArea());
    }

    private HBox buildTopBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(8, 15, 8, 15));
        bar.setStyle("-fx-background-color: #060612; " +
                "-fx-border-color: #1a1a3a; -fx-border-width: 0 0 1 0;");

        Label title = new Label("DUNGEON PROTOCOL");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = topButton("Save", "#1a5c2a");
        Button menuBtn = topButton("Menu", "#333344");

        saveBtn.setOnAction(e -> {
            controller.saveGame();
            showMessage("Game saved.");
        });
        menuBtn.setOnAction(e -> onReturnToMenu.run());

        bar.getChildren().addAll(title, spacer, saveBtn, menuBtn);
        return bar;
    }

    private VBox buildCenterArea() {
        VBox center = new VBox(10);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(12));
        center.setStyle("-fx-background-color: #0a0a1a;");

        roomInfoLabel.setFont(Font.font("Monospace", 12));
        roomInfoLabel.setTextFill(Color.web("#8080b0"));
        roomInfoLabel.setAlignment(Pos.CENTER);

        messageLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        messageLabel.setTextFill(Color.web("#ffd700"));
        messageLabel.setAlignment(Pos.CENTER);

        center.getChildren().addAll(
                roomInfoLabel, mapView,
                buildMovementControls(), messageLabel);
        return center;
    }

    private GridPane buildMovementControls() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(6);
        grid.setVgap(6);

        Button up    = dirButton("^");
        Button down  = dirButton("v");
        Button left  = dirButton("<");
        Button right = dirButton(">");

        up.setOnAction(e    -> handleMove(-1, 0));
        down.setOnAction(e  -> handleMove(1, 0));
        left.setOnAction(e  -> handleMove(0, -1));
        right.setOnAction(e -> handleMove(0, 1));

        grid.add(up,    1, 0);
        grid.add(left,  0, 1);
        grid.add(down,  1, 1);
        grid.add(right, 2, 1);
        return grid;
    }

    private void setupKeyboardInput() {
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case W, UP    -> handleMove(-1, 0);
                    case S, DOWN  -> handleMove(1, 0);
                    case A, LEFT  -> handleMove(0, -1);
                    case D, RIGHT -> handleMove(0, 1);
                    default -> { }
                }
            });
        });
    }

    private void handleMove(int dRow, int dCol) {
        if (controller.getCurrentState().isGameOver()) return;
        boolean moved = controller.moveHero(dRow, dCol);
        if (!moved) { showMessage("Can't move there."); return; }
        refresh();
        handleRoomEntry();
    }

    private void handleRoomEntry() {
        Room room = controller.getCurrentRoom();

        if (!room.getItems().isEmpty()) {
            List<Item> found = room.getItems();
            controller.collectRoomItems();
            new TreasureView(found).show();
            refresh();
        }

        if (!room.isCleared() && !room.getEnemies().isEmpty()) {
            Enemy enemy = room.getEnemies().get(0);
            new CombatView(controller, enemy, () -> {
                refresh();
                if (controller.getCurrentState().isGameOver()) showGameOver();
                else if (controller.checkExitCondition()) {
                    if (controller.isVictory()) showVictory();
                    else { showLevelAdvance(controller.getDungeonLevel()); refresh(); }
                }
            }).show();
        } else if (controller.checkExitCondition()) {
            if (controller.isVictory()) showVictory();
            else { showLevelAdvance(controller.getDungeonLevel()); refresh(); }
        }
    }

    @Override
    public void refresh() {
        mapView.refresh();
        heroStatus.refresh();
        updateRoomInfo();
    }

    private void updateRoomInfo() {
        Room room = controller.getCurrentRoom();
        StringBuilder info = new StringBuilder();
        info.append("Floor ").append(controller.getDungeonLevel()).append("/3");
        info.append("  [").append(room.getRow()).append(",").append(room.getCol()).append("]");
        info.append("  ").append(room.getType());
        if (!room.isCleared() && !room.getEnemies().isEmpty())
            info.append("  ! ").append(room.getEnemies().size()).append(" enemy");
        if (!room.getItems().isEmpty())
            info.append("  $ ").append(room.getItems().size()).append(" item(s)");
        roomInfoLabel.setText(info.toString());
    }

    private void showMessage(String msg) { messageLabel.setText(msg); }

    private void showLevelAdvance(int newLevel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Floor Cleared!");
        alert.setHeaderText("You defeated the boss!");
        alert.setContentText("Descending to floor " + newLevel + "...");
        alert.showAndWait();
    }

    private void showGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GAME OVER");
        alert.setHeaderText("You have been defeated...");
        alert.setContentText("The dungeon claimed another soul.");
        alert.showAndWait();
        onReturnToMenu.run();
    }

    private void showVictory() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("VICTORY!");
        alert.setHeaderText("You conquered Dungeon Protocol!");
        alert.setContentText("The Dragon is slain!");
        alert.showAndWait();
        onReturnToMenu.run();
    }

    private Button dirButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(44, 44);
        btn.setStyle("-fx-background-color: #12122a; -fx-text-fill: #8080c0; " +
                "-fx-font-size: 16; -fx-background-radius: 6; " +
                "-fx-border-color: #2a2a5a; -fx-border-radius: 6;");
        return btn;
    }

    private Button topButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #c0c0e0; " +
                "-fx-font-family: Monospace; -fx-font-size: 12; -fx-padding: 5 12; " +
                "-fx-background-radius: 4;");
        return btn;
    }
}