package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

/**
 * Dungeon map grid with sprite-based hero and animated cells.
 */
public class DungeonMapView extends GridPane implements ViewRefreshable {

    private static final int CELL_SIZE = 54;
    private final GameController controller;

    public DungeonMapView(GameController controller) {
        this.controller = controller;
        setHgap(3);
        setVgap(3);
        setStyle("-fx-background-color: #080814; -fx-padding: 10;");
        refresh();
    }

    @Override
    public void refresh() {
        getChildren().clear();
        Dungeon dungeon = controller.getDungeon();
        Hero hero = controller.getHero();
        for (int r = 0; r < dungeon.getRows(); r++) {
            for (int c = 0; c < dungeon.getCols(); c++) {
                Room room = dungeon.getRoom(r, c);
                boolean isHero = hero.getRow() == r && hero.getCol() == c;
                add(buildCell(room, isHero, hero), c, r);
            }
        }
    }

    private StackPane buildCell(Room room, boolean isHero, Hero hero) {
        Rectangle bg = new Rectangle(CELL_SIZE, CELL_SIZE);
        bg.setArcWidth(7);
        bg.setArcHeight(7);

        StackPane cell = new StackPane();
        cell.setAlignment(Pos.CENTER);

        if (isHero) {
            bg.setFill(Color.web("#003344"));
            bg.setStroke(Color.web("#00e5ff"));
            bg.setStrokeWidth(2);
            bg.setEffect(new DropShadow(12, Color.web("#00e5ff")));

            ScaleTransition pulse = new ScaleTransition(Duration.millis(700), bg);
            pulse.setFromX(1.0); pulse.setFromY(1.0);
            pulse.setToX(1.06);  pulse.setToY(1.06);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();

            // Carica sprite
            ImageView sprite = loadSprite(hero);
            if (sprite != null) {
                cell.getChildren().addAll(bg, sprite);
            } else {
                Text icon = new Text("@");
                icon.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
                icon.setFill(Color.web("#00e5ff"));
                cell.getChildren().addAll(bg, icon);
            }
            return cell;
        }

        Text icon = new Text();
        icon.setFont(Font.font("Monospace", FontWeight.BOLD, 18));

        if (!room.isVisited()) {
            bg.setFill(Color.web("#0a0a16"));
            bg.setStroke(Color.web("#15152a"));
            bg.setStrokeWidth(1);
            icon.setText("?");
            icon.setFill(Color.web("#222244"));
        } else {
            switch (room.getType()) {
                case START -> {
                    bg.setFill(Color.web("#0d2a0d"));
                    bg.setStroke(Color.web("#1a5a1a"));
                    icon.setText("S");
                    icon.setFill(Color.web("#66ff66"));
                }
                case EXIT -> {
                    bg.setFill(Color.web("#2a1a00"));
                    bg.setStroke(Color.web("#ffd700"));
                    bg.setStrokeWidth(2);
                    icon.setText(room.isCleared() ? "E" : "!");
                    icon.setFill(Color.web("#ffd700"));
                    bg.setEffect(new DropShadow(10, Color.web("#ffd700")));
                }
                case ENEMY -> {
                    if (room.isCleared()) {
                        bg.setFill(Color.web("#0d1a0d"));
                        bg.setStroke(Color.web("#1a3a1a"));
                        icon.setText("v");
                        icon.setFill(Color.web("#2a5a2a"));
                    } else {
                        bg.setFill(Color.web("#2a0808"));
                        bg.setStroke(Color.web("#6a1a1a"));
                        bg.setStrokeWidth(1.5);
                        icon.setText("!");
                        icon.setFill(Color.web("#ff4444"));
                        FadeTransition fade = new FadeTransition(Duration.millis(900), icon);
                        fade.setFromValue(0.5);
                        fade.setToValue(1.0);
                        fade.setAutoReverse(true);
                        fade.setCycleCount(Animation.INDEFINITE);
                        fade.play();
                    }
                }
                case TREASURE -> {
                    if (room.isCleared()) {
                        bg.setFill(Color.web("#1a1a08"));
                        bg.setStroke(Color.web("#3a3a10"));
                        icon.setText(".");
                        icon.setFill(Color.web("#404020"));
                    } else {
                        bg.setFill(Color.web("#1a1600"));
                        bg.setStroke(Color.web("#5a4a00"));
                        bg.setStrokeWidth(1.5);
                        icon.setText("$");
                        icon.setFill(Color.web("#ffd700"));
                        icon.setEffect(new DropShadow(8, Color.web("#ffd700")));
                    }
                }
                default -> {
                    bg.setFill(Color.web("#111128"));
                    bg.setStroke(Color.web("#1a1a38"));
                    icon.setText(".");
                    icon.setFill(Color.web("#2a2a50"));
                }
            }
        }

        cell.getChildren().addAll(bg, icon);
        return cell;
    }

    private ImageView loadSprite(Hero hero) {
        try {
            String gender = hero.getGender();
            String cls = hero.getHeroClass().name().toLowerCase();
            String path = "/it/unicam/cs/mpgc/rpg126224/images/"
                    + gender + "_" + cls + ".png";
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) return null;
            Image img = new Image(stream);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(CELL_SIZE - 6);
            iv.setFitHeight(CELL_SIZE - 6);
            iv.setPreserveRatio(true);
            iv.setSmooth(false);
            return iv;
        } catch (Exception e) {
            return null;
        }
    }
}