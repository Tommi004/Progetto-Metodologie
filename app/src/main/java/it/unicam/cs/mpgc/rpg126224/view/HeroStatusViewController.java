package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

/**
 * FXML controller for the hero status side panel.
 * Scene structure is defined in HeroStatus.fxml.
 *
 * <p>Each row in the inventory {@link ListView} shows a colour-coded rarity
 * badge; hovering over a row opens a {@link Tooltip} with the item name,
 * rarity tier, value and a short effect description.</p>
 */
public class HeroStatusViewController implements ViewRefreshable {

    @FXML private Label       avatarLabel;
    @FXML private Label       nameLabel;
    @FXML private Label       classLabel;
    @FXML private Label       levelLabel;
    @FXML private Label       hpLabel;
    @FXML private ProgressBar hpBar;
    @FXML private Label       xpLabel;
    @FXML private ProgressBar xpBar;
    @FXML private Label       manaLabel;
    @FXML private ProgressBar manaBar;
    @FXML private Label       atkLabel;
    @FXML private Label       defLabel;
    @FXML private Label       magLabel;
    @FXML private Label       posLabel;
    @FXML private Label       floorLabel;
    @FXML private Label       goldLabel;
    @FXML private ListView<Item> inventoryList;

    private GameController gameController;

    public void setup(GameController gameController) {
        this.gameController = gameController;
        installInventoryCellFactory();
    }

    // -------------------------------------------------------------------------
    // ViewRefreshable
    // -------------------------------------------------------------------------

    @Override
    public void refresh() {
        if (gameController.getCurrentState() == null) return;
        Hero hero = gameController.getHero();

        avatarLabel.setText(getHeroEmoji(hero.getHeroClass()));
        nameLabel.setText(hero.getName());
        classLabel.setText(hero.getHeroClass().name());
        levelLabel.setText("Level " + hero.getLevel());

        hpLabel.setText("HP  " + hero.getCurrentHp() + " / " + hero.getMaxHp());
        hpBar.setProgress((double) hero.getCurrentHp() / hero.getMaxHp());

        xpLabel.setText("XP  " + hero.getExperience() + " / " + hero.xpForNextLevel());
        xpBar.setProgress((double) hero.getExperience() / hero.xpForNextLevel());

        manaLabel.setText("MP  " + hero.getCurrentMana() + " / " + hero.getMaxMana());
        manaBar.setProgress(hero.getMaxMana() == 0 ? 0
                : (double) hero.getCurrentMana() / hero.getMaxMana());

        atkLabel.setText("ATK  " + hero.getAttack());
        defLabel.setText("DEF  " + hero.getDefense());
        magLabel.setText("MAG  " + hero.getMagic());
        posLabel.setText("[" + hero.getRow() + "," + hero.getCol() + "]");
        floorLabel.setText("Floor " + gameController.getDungeonLevel() + " / 5");
        goldLabel.setText("💰 " + hero.getGold() + " gold");

        List<Item> inv = hero.getInventory();
        inventoryList.getItems().setAll(inv);
        if (inv.isEmpty()) {
            // Add a sentinel null item so the cell factory can show "(empty)"
            inventoryList.getItems().add(null);
        }
    }

    // -------------------------------------------------------------------------
    // Inventory cell factory with tooltip
    // -------------------------------------------------------------------------

    /**
     * Installs a custom {@link ListCell} factory on {@code inventoryList}.
     *
     * <p>Each non-empty cell displays a short badge + item name coloured by
     * rarity, and a {@link Tooltip} that shows:</p>
     * <ul>
     *   <li>item name in the rarity colour</li>
     *   <li>rarity tier and base value</li>
     *   <li>a one-line effect description from {@link ItemType#getEffectDescription()}</li>
     * </ul>
     */
    private void installInventoryCellFactory() {
        inventoryList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(item == null && !empty ? "(empty)" : null);
                    setStyle("-fx-text-fill: #606060; -fx-font-style: italic;");
                    setTooltip(null);
                    return;
                }

                // ── Cell text ────────────────────────────────────────────
                String badge = getItemEmoji(item.getType());
                String rarityPrefix = item.getRarity() != Rarity.COMMON
                        && !item.getName().startsWith(item.getRarity().getDisplayName())
                        ? "[" + item.getRarity().getDisplayName().charAt(0) + "] "
                        : "";
                String qty = item.getQuantity() > 1 ? " ×" + item.getQuantity() : "";
                setText(badge + " " + rarityPrefix + item.getName() + qty);
                setStyle("-fx-text-fill: " + item.getRarity().getColor() + ";");

                // ── Tooltip ──────────────────────────────────────────────
                setTooltip(buildTooltip(item));
            }
        });
    }

    /**
     * Builds a styled {@link Tooltip} for the given item.
     *
     * <p>Layout (plain text, monospace):</p>
     * <pre>
     *  ╔══════════════════════════╗
     *  ║  [Rarity] Item Name      ║  
     *  ║  ─────────────────────   ║
     *  ║  Type    │ SWORD         ║
     *  ║  Rarity  │ Rare          ║
     *  ║  Value   │ +24           ║
     *  ║  ─────────────────────   ║
     *  ║  Effect description...   ║
     *  ╚══════════════════════════╝
     * </pre>
     */
    private Tooltip buildTooltip(Item item) {
        String rarityColor = item.getRarity().getColor();

        // ── Title ────────────────────────────────────────────────────────
        String titleContent = item.getRarity() != Rarity.COMMON
                ? item.getRarity().getDisplayName() + "  " + item.getName()
                : item.getName();
        Label titleLbl = new Label(titleContent);
        titleLbl.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        titleLbl.setTextFill(Color.web(rarityColor));

        // ── Stats rows ───────────────────────────────────────────────────
        Label typeLbl   = statLabel("Type   : " + item.getType().name(),   "#c0c0c0");
        Label rarLbl    = statLabel("Rarity : " + item.getRarity().getDisplayName(), rarityColor);
        Label valLbl    = statLabel("Value  : +" + item.getValue(),        "#c0c0c0");

        // ── Effect ───────────────────────────────────────────────────────
        Label fxLbl = new Label(item.getType().getEffectDescription());
        fxLbl.setFont(Font.font("Monospace", 10));
        fxLbl.setTextFill(Color.web("#909090"));
        fxLbl.setWrapText(true);
        fxLbl.setMaxWidth(170);

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(3,
                titleLbl, typeLbl, rarLbl, valLbl, fxLbl);
        box.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 8;");

        Tooltip tt = new Tooltip();
        tt.setGraphic(box);
        tt.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #333366;"
                + " -fx-border-width: 1; -fx-padding: 0;");
        tt.setShowDelay(Duration.millis(300));
        tt.setHideDelay(Duration.millis(100));
        return tt;
    }

    private static Label statLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", 11));
        l.setTextFill(Color.web(color));
        return l;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getHeroEmoji(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "⚔";
            case MAGE    -> "🔮";
            case ARCHER  -> "🏹";
        };
    }

    private String getItemEmoji(ItemType type) {
        return switch (type) {
            case HEALTH_POTION   -> "[HP]";
            case SWORD           -> "[SW]";
            case SPEAR           -> "[SP]";
            case BOW             -> "[BW]";
            case CROSSBOW        -> "[XB]";
            case STAFF           -> "[ST]";
            case ARMOR           -> "[AR]";
            case HELMET          -> "[HM]";
            case AMULET          -> "[AM]";
            case STRENGTH_POTION -> "[SP]";
            case MANA_POTION     -> "[MP]";
        };
    }
}