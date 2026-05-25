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
 * Side panel showing hero stats and inventory.
 */
public class HeroStatusView extends VBox implements ViewRefreshable {

    private final GameController controller;
    private final Label avatarLabel  = new Label();
    private final Label nameLabel    = new Label();
    private final Label classLabel   = new Label();
    private final Label levelLabel   = new Label();
    private final Label hpLabel      = new Label();
    private final ProgressBar hpBar  = new ProgressBar();
    private final Label xpLabel      = new Label();
    private final ProgressBar xpBar  = new ProgressBar();
    private final Label atkLabel     = new Label();
    private final Label defLabel     = new Label();
    private final Label magLabel     = new Label();
    private final Label posLabel     = new Label();
    private final Label floorLabel   = new Label();
    private final ListView<String> inventoryList = new ListView<>();

    public HeroStatusView(GameController controller) {
        this.controller = controller;
        buildUI();
        refresh();
    }

    private void buildUI() {
        setSpacing(5);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #0a0a1e; " +
                "-fx-border-color: #1e1e4a; -fx-border-width: 0 1 0 0;");
        setMinWidth(190);
        setMaxWidth(190);

        Label title = styledLabel("-- HERO --", "#e94560", 13, true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        avatarLabel.setFont(Font.font(40));
        avatarLabel.setMaxWidth(Double.MAX_VALUE);
        avatarLabel.setAlignment(Pos.CENTER);

        styleLabel(nameLabel, "#00e5ff", 13, true);
        styleLabel(classLabel, "#8080c0", 11, false);
        styleLabel(levelLabel, "#ffd700", 12, true);
        styleLabel(hpLabel, "#ff6666", 11, false);
        styleLabel(xpLabel, "#9b59b6", 11, false);
        styleLabel(atkLabel, "#ff8844", 11, false);
        styleLabel(defLabel, "#44aaff", 11, false);
        styleLabel(magLabel, "#cc66ff", 11, false);
        styleLabel(posLabel, "#606080", 10, false);
        styleLabel(floorLabel, "#ffd700", 11, true);

        hpBar.setMaxWidth(Double.MAX_VALUE);
        hpBar.setPrefHeight(10);
        hpBar.setStyle("-fx-accent: #e94560;");

        xpBar.setMaxWidth(Double.MAX_VALUE);
        xpBar.setPrefHeight(8);
        xpBar.setStyle("-fx-accent: #9b59b6;");

        Label statsTitle = styledLabel("-- STATS --", "#606090", 10, false);
        statsTitle.setMaxWidth(Double.MAX_VALUE);
        statsTitle.setAlignment(Pos.CENTER);

        Label invTitle = styledLabel("-- ITEMS --", "#606090", 10, false);
        invTitle.setMaxWidth(Double.MAX_VALUE);
        invTitle.setAlignment(Pos.CENTER);

        inventoryList.setStyle("-fx-background-color: #080820; " +
                "-fx-control-inner-background: #080820; " +
                "-fx-font-family: Monospace; -fx-font-size: 10;");
        inventoryList.setMaxHeight(130);

        getChildren().addAll(
                title, avatarLabel, nameLabel, classLabel, levelLabel,
                hpLabel, hpBar, xpLabel, xpBar,
                new Separator(), statsTitle,
                atkLabel, defLabel, magLabel, posLabel, floorLabel,
                new Separator(), invTitle, inventoryList
        );
    }

    @Override
    public void refresh() {
        if (controller.getCurrentState() == null) return;
        Hero hero = controller.getHero();

        avatarLabel.setText(getHeroEmoji(hero.getHeroClass()));
        nameLabel.setText(hero.getName());
        classLabel.setText(hero.getHeroClass().name());
        levelLabel.setText("Level " + hero.getLevel());

        hpLabel.setText("HP  " + hero.getCurrentHp() + " / " + hero.getMaxHp());
        hpBar.setProgress((double) hero.getCurrentHp() / hero.getMaxHp());

        xpLabel.setText("XP  " + hero.getExperience() + " / " + hero.xpForNextLevel());
        xpBar.setProgress((double) hero.getExperience() / hero.xpForNextLevel());

        atkLabel.setText("ATK  " + hero.getAttack());
        defLabel.setText("DEF  " + hero.getDefense());
        magLabel.setText("MAG  " + hero.getMagic());
        posLabel.setText("[" + hero.getRow() + "," + hero.getCol() + "]");
        floorLabel.setText("Floor " + controller.getDungeonLevel() + " / 3");

        List<Item> inv = hero.getInventory();
        inventoryList.getItems().setAll(
                inv.stream().map(i -> getItemEmoji(i.getType()) + " " + i.getName()).toList()
        );
        if (inv.isEmpty()) inventoryList.getItems().add("(empty)");
    }

    private String getHeroEmoji(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "W";
            case MAGE    -> "M";
            case ARCHER  -> "A";
        };
    }

    private String getItemEmoji(ItemType type) {
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

    private void styleLabel(Label l, String color, int size, boolean bold) {
        l.setFont(Font.font("Monospace", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setTextFill(Color.web(color));
    }

    private Label styledLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        styleLabel(l, color, size, bold);
        return l;
    }
}