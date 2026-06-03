package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * FXML controller for the hero status side panel.
 * Handles only display updates; all game logic is in {@link GameController}.
 * Scene structure is defined in HeroStatus.fxml.
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
    @FXML private ListView<String> inventoryList;

    private GameController gameController;

    /**
     * Injects the game controller. Must be called right after FXMLLoader.load().
     */
    public void setup(GameController gameController) {
        this.gameController = gameController;
    }

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
            case MANA_POTION     -> "[MP]";
        };
    }
}