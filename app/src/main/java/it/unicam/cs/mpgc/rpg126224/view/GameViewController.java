package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import it.unicam.cs.mpgc.rpg126224.model.RunStats;
import it.unicam.cs.mpgc.rpg126224.model.TrapType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * FXML controller for the main game screen.
 * Handles movement buttons, keyboard input, room events and UI refresh.
 * Scene structure is defined in GameView.fxml.
 * Dialog creation is delegated to {@link ViewGameDialogFactory}.
 */
public class GameViewController implements ViewRefreshable {

    @FXML private Pane  heroStatusPlaceholder;
    @FXML private Pane  mapPlaceholder;
    @FXML private Label roomInfoLabel;
    @FXML private Label messageLabel;

    private GameController gameController;
    private Runnable       onReturnToMenu;
    private DungeonMapView mapView;
    private HeroStatusView heroStatus;

    /**
     * Injects dependencies and wires the sub-views into the placeholders.
     * Must be called right after FXMLLoader.load().
     */
    public void setup(GameController gameController, Runnable onReturnToMenu) {
        this.gameController = gameController;
        this.onReturnToMenu = onReturnToMenu;

        heroStatus = new HeroStatusView(gameController);
        mapView    = new DungeonMapView(gameController);

        // Replace placeholders with actual sub-views
        heroStatusPlaceholder.getChildren().add(heroStatus.getRoot());
        mapPlaceholder.getChildren().add(mapView.getRoot());
    }

    /**
     * Wires keyboard input. Called after the scene is set on the stage.
     */
    public void setupKeyboardInput(javafx.scene.Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W, UP    -> handleMove(-1,  0);
                case S, DOWN  -> handleMove( 1,  0);
                case A, LEFT  -> handleMove( 0, -1);
                case D, RIGHT -> handleMove( 0,  1);
                default -> { }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    @FXML private void handleSave()  { gameController.saveGame(); showMessage("✓ Game saved."); }
    @FXML private void handleMenu()  {
        ViewGameDialogFactory.showPauseMenu(
                () -> { gameController.saveGame(); onReturnToMenu.run(); },
                onReturnToMenu
        );
    }
    @FXML private void handleUp()    { handleMove(-1,  0); }
    @FXML private void handleDown()  { handleMove( 1,  0); }
    @FXML private void handleLeft()  { handleMove( 0, -1); }
    @FXML private void handleRight() { handleMove( 0,  1); }

    // -------------------------------------------------------------------------
    // Movement and room logic
    // -------------------------------------------------------------------------

    private void handleMove(int dRow, int dCol) {
        if (gameController.getCurrentState().isGameOver()) return;
        if (!gameController.moveHero(dRow, dCol)) {
            showMessage("⛔ Can't move there.");
            return;
        }
        showMessage("");
        refresh();
        handleRoomEntry();
    }

    private void handleRoomEntry() {
        Room room = gameController.getCurrentRoom();
        handleTrap(room);
        handleTreasure(room);
        handleShop(room);
        handleCombat(room);
    }

    private void handleShop(Room room) {
        if (room.getType() != RoomType.SHOP || room.isCleared()) return;
        new ShopView(gameController, () -> {
            room.markCleared();
            refresh();
        }).show();
    }

    private void handleTrap(Room room) {
        if (!room.hasTrap()) return;
        TrapType trap = gameController.triggerTrap();
        if (trap == null) return;
        javafx.application.Platform.runLater(() ->
                ViewGameDialogFactory.showTrap(trap));
        refresh();
    }

    private void handleTreasure(Room room) {
        if (room.getItems().isEmpty()) return;
        List<Item> found = room.getItems();
        gameController.collectRoomItems();
        new TreasureView(found).show();
        refresh();
    }

    private void handleCombat(Room room) {
        if (room.isCleared() || room.getEnemies().isEmpty()) {
            handleExitCheck();
            return;
        }
        Enemy enemy = room.getEnemies().get(0);
        boolean isExitRoom = room.getType() == RoomType.EXIT;
        new CombatView(gameController, enemy, () -> {
            refresh();
            if (gameController.getCurrentState().isGameOver()) {
                RunStats stats = gameController.buildRunStats();
                Platform.runLater(() ->
                        ViewGameDialogFactory.showGameOver(stats, onReturnToMenu));
            } else if (isExitRoom && gameController.getCurrentRoom().isCleared()) {
                handleExitAfterCombat();
            }
        }).show();
    }

    private void handleExitAfterCombat() {
        if (!gameController.checkExitCondition()) return;
        if (gameController.isVictory()) {
            RunStats stats = gameController.buildRunStats();
            Platform.runLater(() ->
                    ViewGameDialogFactory.showVictory(stats, onReturnToMenu));
        } else {
            int level = gameController.getDungeonLevel();
            Platform.runLater(() -> {
                ViewGameDialogFactory.showLevelAdvance(level);
                refresh();
            });
        }
    }

    private void handleExitCheck() {
        if (!gameController.checkExitCondition()) return;
        if (gameController.isVictory()) {
            RunStats stats = gameController.buildRunStats();
            ViewGameDialogFactory.showVictory(stats, onReturnToMenu);
        } else {
            ViewGameDialogFactory.showLevelAdvance(gameController.getDungeonLevel());
            refresh();
        }
    }

    // -------------------------------------------------------------------------
    // Refresh
    // -------------------------------------------------------------------------

    @Override
    public void refresh() {
        if (gameController.getCurrentState() == null) return;
        mapView.refresh();
        heroStatus.refresh();
        updateRoomInfo();
    }

    private void updateRoomInfo() {
        Room room = gameController.getCurrentRoom();
        StringBuilder info = new StringBuilder();
        info.append("🏰 Floor ").append(gameController.getDungeonLevel()).append("/5");
        info.append("   📍 [").append(room.getRow()).append(",").append(room.getCol()).append("]");
        info.append("   ").append(room.getType());
        if (!room.isCleared() && !room.getEnemies().isEmpty())
            info.append("  ⚠ ").append(room.getEnemies().size()).append(" enemy");
        if (!room.getItems().isEmpty())
            info.append("  💰 ").append(room.getItems().size()).append(" item(s)");
        if (room.getType() == RoomType.SHOP && !room.isCleared())
            info.append("  🛒 Shop");
        roomInfoLabel.setText(info.toString());
    }

    private void showMessage(String msg) { messageLabel.setText(msg); }
}