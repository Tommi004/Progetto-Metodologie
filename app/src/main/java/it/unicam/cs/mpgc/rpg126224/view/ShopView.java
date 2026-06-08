package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.ShopItem;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * Modal shop dialog that lets the hero purchase items using gold.
 *
 * <p>The catalogue stays open after each purchase: item rows are refreshed
 * in-place so the player can buy multiple items in a single visit.
 * The shop is marked cleared only when the player explicitly closes it.</p>
 */
public class ShopView {

    private static final String FONT = "Monospace";
    private static final String BG   = "#0a0a00";

    private final GameController gameController;
    private final Runnable       onClose;

    public ShopView(GameController gameController, Runnable onClose) {
        this.gameController = gameController;
        this.onClose        = onClose;
    }

    /** Builds and displays the shop dialog modally. */
    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Shop");
        stage.setResizable(false);

        // ── Gold balance label (refreshed after each purchase) ───────────
        Label goldLabel = new Label();
        updateGoldLabel(goldLabel);
        goldLabel.setFont(Font.font(FONT, FontWeight.BOLD, 13));
        goldLabel.setTextFill(Color.web("#ffd700"));

        // ── Header ───────────────────────────────────────────────────────
        Label title = new Label("🛒  DUNGEON SHOP");
        title.setFont(Font.font(FONT, FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#ffd700"));
        DropShadow glow = new DropShadow(16, Color.web("#ffd700"));
        title.setEffect(glow);

        Label sub = new Label("Spend your hard-earned gold wisely.");
        sub.setFont(Font.font(FONT, 11));
        sub.setTextFill(Color.web("#808060"));

        Separator sep = new Separator();
        sep.setMaxWidth(360);
        sep.setStyle("-fx-background-color: #3a3a00;");

        // ── Item rows container (rebuilt on purchase) ────────────────────
        VBox itemsBox = new VBox(8);
        itemsBox.setAlignment(Pos.CENTER_LEFT);
        itemsBox.setMaxWidth(380);

        // Generate catalogue once per visit
        List<ShopItem> catalogue = gameController.getShopCatalogue();
        buildItemRows(itemsBox, catalogue, goldLabel);

        // ── Close button ─────────────────────────────────────────────────
        Button closeBtn = new Button("Leave Shop");
        closeBtn.setFont(Font.font(FONT, 12));
        closeBtn.setStyle(
                "-fx-background-color: #1a1a00; -fx-text-fill: #808040; " +
                "-fx-padding: 8 24; -fx-background-radius: 4; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> { stage.close(); onClose.run(); });

        // ── Root layout ──────────────────────────────────────────────────
        VBox root = new VBox(12, title, sub, goldLabel, sep, itemsBox, closeBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + BG + ";");

        FadeTransition ft = new FadeTransition(Duration.millis(350), root);
        ft.setFromValue(0);
        ft.setToValue(1);

        stage.setScene(new Scene(root, 440, 460));
        ft.play();
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void updateGoldLabel(Label lbl) {
        lbl.setText("💰  " + gameController.getHero().getGold() + " gold");
    }

    /**
     * Populates {@code itemsBox} with one row per catalogue entry.
     * Called once on open and again after each successful purchase.
     */
    private void buildItemRows(VBox itemsBox,
                               List<ShopItem> catalogue, Label goldLabel) {
        itemsBox.getChildren().clear();
        for (ShopItem shopItem : catalogue) {
            itemsBox.getChildren().add(
                    buildItemRow(shopItem, catalogue, itemsBox, goldLabel));
        }
    }

    private HBox buildItemRow(ShopItem shopItem,
                              List<ShopItem> catalogue,
                              VBox itemsBox,
                              Label goldLabel) {
        boolean canAfford = gameController.getHero().getGold() >= shopItem.goldCost();
        String  rarityColor = shopItem.item().getRarity().getColor();

        // Item name
        Label nameLbl = new Label(shopItem.item().getName());
        nameLbl.setFont(Font.font(FONT, FontWeight.BOLD, 12));
        nameLbl.setTextFill(Color.web(rarityColor));
        nameLbl.setMinWidth(160);

        // Effect description
        Label fxLbl = new Label(shopItem.item().getType().getEffectDescription());
        fxLbl.setFont(Font.font(FONT, 10));
        fxLbl.setTextFill(Color.web("#707050"));
        fxLbl.setMaxWidth(140);
        fxLbl.setWrapText(true);

        VBox nameBox = new VBox(2, nameLbl, fxLbl);

        // Price
        Label priceLbl = new Label("🪙 " + shopItem.goldCost());
        priceLbl.setFont(Font.font(FONT, FontWeight.BOLD, 12));
        priceLbl.setTextFill(canAfford ? Color.web("#ffd700") : Color.web("#605030"));
        priceLbl.setMinWidth(70);

        // Buy button
        Button buyBtn = new Button("Buy");
        buyBtn.setFont(Font.font(FONT, 11));
        buyBtn.setDisable(!canAfford);
        buyBtn.setStyle(canAfford
                ? "-fx-background-color: #2a2a00; -fx-text-fill: #ffd700; " +
                  "-fx-padding: 5 14; -fx-background-radius: 4; -fx-cursor: hand;"
                : "-fx-background-color: #141400; -fx-text-fill: #404020; " +
                  "-fx-padding: 5 14; -fx-background-radius: 4;");

        buyBtn.setOnAction(e -> {
            if (gameController.buyShopItem(shopItem)) {
                // Update gold label
                updateGoldLabel(goldLabel);
                // Rebuild rows in-place with updated affordability
                buildItemRows(itemsBox, catalogue, goldLabel);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, nameBox, spacer, priceLbl, buyBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle("-fx-background-color: #0e0e00; -fx-background-radius: 4;");
        return row;
    }
}