package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.model.Item;
import it.unicam.cs.mpgc.rpg126224.model.ItemType;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

/**
 * Animated popup shown when the hero finds items in a treasure room.
 */
public class TreasureView {

    private final List<Item> items;

    public TreasureView(List<Item> items) {
        this.items = items;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Treasure Found!");
        stage.setResizable(false);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #1a1200;");

        Label title = new Label("*** TREASURE FOUND! ***");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#ffd700"));

        ScaleTransition titlePulse = new ScaleTransition(Duration.millis(600), title);
        titlePulse.setFromX(0.8); titlePulse.setFromY(0.8);
        titlePulse.setToX(1.0);   titlePulse.setToY(1.0);
        titlePulse.play();

        VBox itemsBox = new VBox(10);
        itemsBox.setAlignment(Pos.CENTER);

        for (Item item : items) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 16, 8, 16));
            row.setStyle("-fx-background-color: #2a2000; " +
                    "-fx-border-color: #5a4000; " +
                    "-fx-border-radius: 6; -fx-background-radius: 6;");

            Label symbol = new Label(getItemSymbol(item.getType()));
            symbol.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
            symbol.setTextFill(Color.web("#ffd700"));

            VBox info = new VBox(2);
            Label name = new Label(item.getName());
            name.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
            name.setTextFill(Color.web("#ffe080"));

            Label effect = new Label(getEffectDescription(item));
            effect.setFont(Font.font("Monospace", 11));
            effect.setTextFill(Color.web("#a08040"));

            info.getChildren().addAll(name, effect);
            row.getChildren().addAll(symbol, info);

            row.setTranslateX(-40);
            row.setOpacity(0);
            TranslateTransition slide = new TranslateTransition(Duration.millis(300), row);
            slide.setToX(0);
            FadeTransition fade = new FadeTransition(Duration.millis(300), row);
            fade.setToValue(1);
            ParallelTransition pt = new ParallelTransition(slide, fade);
            pt.setDelay(Duration.millis(items.indexOf(item) * 120));
            pt.play();

            itemsBox.getChildren().add(row);
        }

        Button closeBtn = new Button("Take Items");
        closeBtn.setStyle("-fx-background-color: #8a6a00; -fx-text-fill: #ffd700; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; -fx-padding: 8 24; " +
                "-fx-background-radius: 4;");
        closeBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, itemsBox, closeBtn);
        stage.setScene(new Scene(root, 380, 120 + items.size() * 80));
        stage.showAndWait();
    }

    private String getItemSymbol(ItemType type) {
        return switch (type) {
            case HEALTH_POTION   -> "[HP]";
            case SWORD           -> "[SW]";
            case BOW             -> "[BW]";
            case STAFF           -> "[ST]";
            case ARMOR           -> "[AR]";
            case AMULET          -> "[AM]";
            case STRENGTH_POTION -> "[SP]";
        };
    }

    private String getEffectDescription(Item item) {
        return switch (item.getType()) {
            case HEALTH_POTION   -> "Restores +" + item.getValue() + " HP";
            case SWORD, BOW      -> "Attack +" + item.getValue() + " (permanent)";
            case STAFF, AMULET   -> "Magic +" + item.getValue() + " (permanent)";
            case ARMOR           -> "Defense +" + item.getValue() + " (permanent)";
            case STRENGTH_POTION -> "Attack +" + item.getValue() + " boost";
        };
    }
}