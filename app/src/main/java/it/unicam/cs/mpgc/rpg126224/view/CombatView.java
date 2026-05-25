package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Modal combat screen with animated avatar and combat log.
 */
public class CombatView {

    private final GameController controller;
    private final Enemy enemy;
    private final Runnable onCombatEnd;

    private Stage stage;
    private Label heroHpLabel;
    private Label enemyHpLabel;
    private ProgressBar heroHpBar;
    private ProgressBar enemyHpBar;
    private TextArea logArea;
    private Label enemyAvatar;
    private Label heroAvatar;
    private HBox actionBar;

    public CombatView(GameController controller, Enemy enemy, Runnable onCombatEnd) {
        this.controller = controller;
        this.enemy = enemy;
        this.onCombatEnd = onCombatEnd;
    }

    public void show() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Combat - " + enemy.getName());
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0010;");
        root.setPadding(new Insets(15));
        root.setTop(buildCombatantsArea());
        root.setCenter(buildLogArea());
        root.setBottom(buildActionBar());

        appendLog("============================");
        appendLog("COMBAT vs " + enemy.getName().toUpperCase());
        appendLog("HP: " + enemy.getCurrentHp() +
                "  ATK: " + enemy.getAttack() +
                "  DEF: " + enemy.getDefense());
        appendLog("============================");

        root.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        stage.setScene(new Scene(root, 580, 480));
        stage.showAndWait();
    }

    private HBox buildCombatantsArea() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 0, 12, 0));
        box.getChildren().addAll(buildHeroPanel(), buildVsLabel(), buildEnemyPanel());
        return box;
    }

    private VBox buildHeroPanel() {
        Hero hero = controller.getHero();
        heroAvatar = new Label(getHeroSymbol(hero.getHeroClass()));
        heroAvatar.setFont(Font.font("Monospace", FontWeight.BOLD, 40));
        heroAvatar.setTextFill(Color.web("#00e5ff"));
        heroAvatar.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(hero.getName());
        nameLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web("#00e5ff"));

        heroHpLabel = new Label("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        heroHpLabel.setFont(Font.font("Monospace", 11));
        heroHpLabel.setTextFill(Color.web("#a0d0ff"));

        heroHpBar = new ProgressBar((double) hero.getCurrentHp() / hero.getMaxHp());
        heroHpBar.setPrefWidth(160);
        heroHpBar.setStyle("-fx-accent: #00e5ff;");

        VBox panel = new VBox(4, heroAvatar, nameLabel, heroHpLabel, heroHpBar);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #0d0d2a; -fx-border-color: #1a2a5a; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");
        return panel;
    }

    private VBox buildEnemyPanel() {
        enemyAvatar = new Label(getEnemySymbol(enemy.getType()));
        enemyAvatar.setFont(Font.font("Monospace", FontWeight.BOLD, 40));
        enemyAvatar.setTextFill(Color.web("#ff4466"));
        enemyAvatar.setAlignment(Pos.CENTER);

        TranslateTransition floatAnim = new TranslateTransition(Duration.millis(1200), enemyAvatar);
        floatAnim.setByY(-6);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.play();

        Label nameLabel = new Label(enemy.getName());
        nameLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web("#ff4466"));

        enemyHpLabel = new Label("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        enemyHpLabel.setFont(Font.font("Monospace", 11));
        enemyHpLabel.setTextFill(Color.web("#ffa0a0"));

        enemyHpBar = new ProgressBar((double) enemy.getCurrentHp() / enemy.getMaxHp());
        enemyHpBar.setPrefWidth(160);
        enemyHpBar.setStyle("-fx-accent: #ff4444;");

        VBox panel = new VBox(4, enemyAvatar, nameLabel, enemyHpLabel, enemyHpBar);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #1a0a0a; -fx-border-color: #5a1a1a; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");
        return panel;
    }

    private Label buildVsLabel() {
        Label vs = new Label("VS");
        vs.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        vs.setTextFill(Color.web("#e94560"));
        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), vs);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.15);  pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
        return vs;
    }

    private TextArea buildLogArea() {
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(160);
        logArea.setStyle("-fx-control-inner-background: #050010; " +
                "-fx-text-fill: #c8b8ff; -fx-font-family: Monospace; -fx-font-size: 12;");
        BorderPane.setMargin(logArea, new Insets(8, 0, 8, 0));
        return logArea;
    }

    private HBox buildActionBar() {
        actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setPadding(new Insets(6, 0, 0, 0));

        Button attackBtn  = actionButton("Attack",  "#c0392b");
        Button specialBtn = actionButton("Special", "#6c3483");
        Button potionBtn  = actionButton("Potion",  "#1a5c2a");
        Button fleeBtn    = actionButton("Flee",    "#444455");

        attackBtn.setOnAction(e  -> processTurn(CombatAction.ATTACK));
        specialBtn.setOnAction(e -> processTurn(CombatAction.SPECIAL));
        potionBtn.setOnAction(e  -> processTurn(CombatAction.USE_POTION));
        fleeBtn.setOnAction(e    -> processTurn(CombatAction.FLEE));

        actionBar.getChildren().addAll(attackBtn, specialBtn, potionBtn, fleeBtn);
        return actionBar;
    }

    private Button actionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-family: Monospace; -fx-font-size: 12; -fx-padding: 10 8; " +
                "-fx-background-radius: 6;");
        return btn;
    }

    private void processTurn(CombatAction action) {
        actionBar.setDisable(true);
        CombatResult result = controller.executeCombatTurn(enemy, action);
        appendLog("> " + result.message());

        if (result.heroDamageDealt() > 0) animateAttack(heroAvatar);
        if (result.enemyDamageDealt() > 0) animateAttack(enemyAvatar);

        PauseTransition delay = new PauseTransition(Duration.millis(350));
        delay.setOnFinished(e -> {
            updateHpDisplay();
            actionBar.setDisable(false);
            if (result.isCombatOver()) {
                actionBar.setDisable(true);
                PauseTransition endDelay = new PauseTransition(Duration.millis(600));
                endDelay.setOnFinished(ev -> {
                    if (result.enemyDefeated()) {
                        controller.resolveEnemyDefeat(enemy.getId());
                        appendLog("Victory! Room cleared.");
                    }
                    stage.close();
                    onCombatEnd.run();
                });
                endDelay.play();
            }
        });
        delay.play();
    }

    private void animateAttack(Label avatar) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(80), avatar);
        tt.setByX(15);
        tt.setAutoReverse(true);
        tt.setCycleCount(2);
        tt.play();
    }

    private void updateHpDisplay() {
        Hero hero = controller.getHero();
        heroHpLabel.setText("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        heroHpBar.setProgress((double) hero.getCurrentHp() / hero.getMaxHp());
        enemyHpLabel.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        enemyHpBar.setProgress((double) enemy.getCurrentHp() / enemy.getMaxHp());
    }

    private void appendLog(String text) {
        logArea.appendText(text + "\n");
    }

    private String getEnemySymbol(EnemyType type) {
        return switch (type) {
            case GOBLIN    -> "G";
            case SKELETON  -> "S";
            case DARK_MAGE -> "M";
            case TROLL     -> "T";
            case ASSASSIN  -> "A";
            case DRAGON    -> "D";
        };
    }

    private String getHeroSymbol(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "W";
            case MAGE    -> "M";
            case ARCHER  -> "A";
        };
    }
}