package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.List;

/**
 * Main game screen showing dungeon map, hero status panel,
 * movement controls and room info. Handles room events including
 * combat, treasure popups and level advancement.
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

        Label title = new Label("⚔  DUNGEON PROTOCOL");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#e94560"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = topButton("💾  Save", "#1a5c2a", "#27ae60");
        Button menuBtn = topButton("⬅  Menu", "#333344", "#555566");

        saveBtn.setOnAction(e -> {
            controller.saveGame();
            showMessage("✓ Game saved.");
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
                roomInfoLabel,
                mapView,
                buildMovementControls(),
                messageLabel
        );
        return center;
    }

    private GridPane buildMovementControls() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(6);
        grid.setVgap(6);

        Button up    = dirButton("▲");
        Button down  = dirButton("▼");
        Button left  = dirButton("◀");
        Button right = dirButton("▶");

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
        if (!moved) {
            showMessage("⛔ Can't move there.");
            return;
        }
        refresh();
        handleRoomEntry();
    }

    private void handleRoomEntry() {
        Room room = controller.getCurrentRoom();

        // Treasure popup
        if (!room.getItems().isEmpty()) {
            List<Item> found = room.getItems();
            controller.collectRoomItems();
            new TreasureView(found).show();
            refresh();
        }

        // Combat
        if (!room.isCleared() && !room.getEnemies().isEmpty()) {
            Enemy enemy = room.getEnemies().get(0);
            boolean isExitRoom = room.getType() == RoomType.EXIT;
            new CombatView(controller, enemy, () -> {
                refresh();
                if (controller.getCurrentState().isGameOver()) {
                    Platform.runLater(this::showGameOver);
                } else if (isExitRoom && controller.getCurrentRoom().isCleared()) {
                    if (controller.checkExitCondition()) {
                        if (controller.isVictory()) {
                            Platform.runLater(this::showVictory);
                        } else {
                            int level = controller.getDungeonLevel();
                            Platform.runLater(() -> {
                                showLevelAdvance(level);
                                refresh();
                            });
                        }
                    }
                }
            }).show();
        } else if (controller.checkExitCondition()) {
            if (controller.isVictory()) showVictory();
            else {
                showLevelAdvance(controller.getDungeonLevel());
                refresh();
            }
        }
    }

    @Override
    public void refresh() {
        if (controller.getCurrentState() == null) return;
        mapView.refresh();
        heroStatus.refresh();
        updateRoomInfo();
    }

    private void updateRoomInfo() {
        Room room = controller.getCurrentRoom();
        StringBuilder info = new StringBuilder();
        info.append("🏰 Floor ").append(controller.getDungeonLevel()).append("/3");
        info.append("   📍 [").append(room.getRow()).append(",").append(room.getCol()).append("]");
        info.append("   ").append(room.getType());
        if (!room.isCleared() && !room.getEnemies().isEmpty())
            info.append("  ⚠ ").append(room.getEnemies().size()).append(" enemy");
        if (!room.getItems().isEmpty())
            info.append("  💰 ").append(room.getItems().size()).append(" item(s)");
        roomInfoLabel.setText(info.toString());
    }

    private void showMessage(String msg) { messageLabel.setText(msg); }

    private void showLevelAdvance(int newLevel) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Floor Cleared!");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #0a0010;");

        Label icon = new Label("⚔");
        icon.setFont(Font.font(48));

        Label title = new Label("FLOOR CLEARED!");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#ffd700"));

        javafx.scene.effect.DropShadow glow =
                new javafx.scene.effect.DropShadow(20, Color.web("#ffd700"));
        title.setEffect(glow);

        Label msg = new Label("You defeated the boss!");
        msg.setFont(Font.font("Monospace", 14));
        msg.setTextFill(Color.web("#e0e0ff"));

        Label sub = new Label("Descending to floor " + newLevel + " of 3...");
        sub.setFont(Font.font("Monospace", 12));
        sub.setTextFill(Color.web("#8080b0"));

        Button okBtn = new Button("CONTINUE");
        okBtn.setStyle("-fx-background-color: #8a6a00; -fx-text-fill: #ffd700; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; -fx-padding: 8 30; " +
                "-fx-background-radius: 4;");
        okBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(icon, title, msg, sub, okBtn);

        javafx.animation.FadeTransition ft =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), root);
        ft.setFromValue(0);
        ft.setToValue(1);

        dialog.setScene(new javafx.scene.Scene(root, 380, 280));
        ft.play();
        dialog.showAndWait();
    }

    private void showGameOver() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Game Over");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #0a0000;");

        Label icon = new Label("💀");
        icon.setFont(Font.font(48));

        Label title = new Label("YOU DIED");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#e94560"));

        javafx.scene.effect.DropShadow glow =
                new javafx.scene.effect.DropShadow(20, Color.web("#e94560"));
        title.setEffect(glow);

        Label msg = new Label("The dungeon claimed another soul...");
        msg.setFont(Font.font("Monospace", 13));
        msg.setTextFill(Color.web("#a06060"));

        Button okBtn = new Button("RETURN TO MENU");
        okBtn.setStyle("-fx-background-color: #6a0000; -fx-text-fill: #ff8080; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; -fx-padding: 8 30; " +
                "-fx-background-radius: 4;");
        okBtn.setOnAction(e -> { dialog.close(); onReturnToMenu.run(); });

        root.getChildren().addAll(icon, title, msg, okBtn);

        javafx.animation.FadeTransition ft =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), root);
        ft.setFromValue(0); ft.setToValue(1);

        dialog.setScene(new javafx.scene.Scene(root, 380, 260));
        ft.play();
        dialog.showAndWait();
    }

    private void showVictory() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Victory!");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #0a0a00;");

        Label icon = new Label("🏆");
        icon.setFont(Font.font(48));

        Label title = new Label("VICTORY!");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#ffd700"));

        javafx.scene.effect.DropShadow glow =
                new javafx.scene.effect.DropShadow(20, Color.web("#ffd700"));
        title.setEffect(glow);

        Label msg = new Label("You conquered Dungeon Protocol!");
        msg.setFont(Font.font("Monospace", 13));
        msg.setTextFill(Color.web("#e0e0ff"));

        Label sub = new Label("The Dragon is slain. The dungeon is yours!");
        sub.setFont(Font.font("Monospace", 11));
        sub.setTextFill(Color.web("#a0a060"));

        Button okBtn = new Button("RETURN TO MENU");
        okBtn.setStyle("-fx-background-color: #8a6a00; -fx-text-fill: #ffd700; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; -fx-padding: 8 30; " +
                "-fx-background-radius: 4;");
        okBtn.setOnAction(e -> { dialog.close(); onReturnToMenu.run(); });

        root.getChildren().addAll(icon, title, msg, sub, okBtn);

        javafx.animation.FadeTransition ft =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), root);
        ft.setFromValue(0); ft.setToValue(1);

        dialog.setScene(new javafx.scene.Scene(root, 380, 300));
        ft.play();
        dialog.showAndWait();
    }

    private Button dirButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(44, 44);
        btn.setStyle("-fx-background-color: #12122a; -fx-text-fill: #8080c0; " +
                "-fx-font-size: 16; -fx-background-radius: 6; " +
                "-fx-border-color: #2a2a5a; -fx-border-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle()
                .replace("#12122a", "#1e1e4a").replace("#8080c0", "#c0c0ff")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle()
                .replace("#1e1e4a", "#12122a").replace("#c0c0ff", "#8080c0")));
        return btn;
    }

    private Button topButton(String text, String dark, String light) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + dark + "; -fx-text-fill: #c0c0e0; " +
                "-fx-font-family: Monospace; -fx-font-size: 12; -fx-padding: 5 12; " +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle()
                .replace(dark, light)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle()
                .replace(light, dark)));
        return btn;
    }
}
