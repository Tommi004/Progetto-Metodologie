package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Modal combat screen with animated enemy avatar, damage flash effects,
 * animated HP bars, and a styled combat log.
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
        stage.setTitle("⚔ Combat — " + enemy.getName());
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0010;");
        root.setPadding(new Insets(15));

        root.setTop(buildCombatantsArea());
        root.setCenter(buildLogArea());
        root.setBottom(buildActionBar());

        appendLog("══════════════════════════════");
        appendLog("⚔  COMBAT vs " + enemy.getName().toUpperCase() + "  ⚔");
        appendLog("HP: " + enemy.getCurrentHp() +
                "  ATK: " + enemy.getAttack() +
                "  DEF: " + enemy.getDefense());
        appendLog("══════════════════════════════");

        Scene scene = new Scene(root, 580, 480);
        stage.setScene(scene);

        // Entrance animation
        root.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        stage.showAndWait();
    }

    // -------------------------------------------------------------------------
    // UI building
    // -------------------------------------------------------------------------

    private HBox buildCombatantsArea() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(0, 0, 12, 0));

        box.getChildren().addAll(
                buildHeroPanel(),
                buildVsLabel(),
                buildEnemyPanel()
        );
        return box;
    }

    private VBox buildHeroPanel() {
        Hero hero = controller.getHero();

        heroAvatar = new Label(getHeroEmoji(hero.getHeroClass()));
        heroAvatar.setFont(Font.font(52));
        heroAvatar.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(hero.getName());
        nameLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web("#00e5ff"));

        heroHpLabel = new Label("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        heroHpLabel.setFont(Font.font("Monospace", 11));
        heroHpLabel.setTextFill(Color.web("#a0d0ff"));

        heroHpBar = new ProgressBar((double) hero.getCurrentHp() / hero.getMaxHp());
        heroHpBar.setPrefWidth(160);
        heroHpBar.setStyle("-fx-accent: #00e5ff; -fx-background-color: #1a1a3a;");

        Label levelLabel = new Label("Lv." + hero.getLevel() + " " + hero.getHeroClass());
        levelLabel.setFont(Font.font("Monospace", 10));
        levelLabel.setTextFill(Color.web("#6080a0"));

        VBox panel = new VBox(4, heroAvatar, nameLabel, heroHpLabel, heroHpBar, levelLabel);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #0d0d2a; -fx-border-color: #1a2a5a; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");
        return panel;
    }

    private VBox buildEnemyPanel() {
        enemyAvatar = new Label(getEnemyEmoji(enemy.getType()));
        enemyAvatar.setFont(Font.font(52));
        enemyAvatar.setAlignment(Pos.CENTER);

        // Glow effect on enemy
        Glow glow = new Glow(0.4);
        enemyAvatar.setEffect(glow);

        // Idle float animation
        TranslateTransition float1 = new TranslateTransition(Duration.millis(1200), enemyAvatar);
        float1.setByY(-6);
        float1.setAutoReverse(true);
        float1.setCycleCount(Animation.INDEFINITE);
        float1.play();

        Label nameLabel = new Label(enemy.getName());
        nameLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        nameLabel.setTextFill(Color.web("#ff4466"));

        enemyHpLabel = new Label("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        enemyHpLabel.setFont(Font.font("Monospace", 11));
        enemyHpLabel.setTextFill(Color.web("#ffa0a0"));

        enemyHpBar = new ProgressBar((double) enemy.getCurrentHp() / enemy.getMaxHp());
        enemyHpBar.setPrefWidth(160);
        enemyHpBar.setStyle("-fx-accent: #ff4444; -fx-background-color: #2a0a0a;");

        Label typeLabel = new Label(enemy.getType().name());
        typeLabel.setFont(Font.font("Monospace", 10));
        typeLabel.setTextFill(Color.web("#804040"));

        VBox panel = new VBox(4, enemyAvatar, nameLabel, enemyHpLabel, enemyHpBar, typeLabel);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #1a0a0a; -fx-border-color: #5a1a1a; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12;");
        return panel;
    }

    private Label buildVsLabel() {
        Label vs = new Label("VS");
        vs.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        vs.setTextFill(Color.web("#e94560"));

        // Pulse animation on VS
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
        logArea.setStyle(
                "-fx-control-inner-background: #050010; " +
                "-fx-text-fill: #c8b8ff; " +
                "-fx-font-family: Monospace; " +
                "-fx-font-size: 12; " +
                "-fx-border-color: #2a1a4a; " +
                "-fx-border-radius: 4;"
        );
        BorderPane.setMargin(logArea, new Insets(8, 0, 8, 0));
        return logArea;
    }

    private HBox buildActionBar() {
        actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setPadding(new Insets(6, 0, 0, 0));

        Button attackBtn  = actionButton("⚔  Attack",   "#c0392b", "#e74c3c");
        Button specialBtn = actionButton("✦  Special",  "#6c3483", "#9b59b6");
        Button potionBtn  = actionButton("🧪  Potion",  "#1a5c2a", "#27ae60");
        Button fleeBtn    = actionButton("🏃  Flee",    "#444455", "#666677");

        attackBtn.setOnAction(e  -> processTurn(CombatAction.ATTACK));
        specialBtn.setOnAction(e -> processTurn(CombatAction.SPECIAL));
        potionBtn.setOnAction(e  -> processTurn(CombatAction.USE_POTION));
        fleeBtn.setOnAction(e    -> processTurn(CombatAction.FLEE));

        actionBar.getChildren().addAll(attackBtn, specialBtn, potionBtn, fleeBtn);
        return actionBar;
    }

    private Button actionButton(String text, String darkColor, String lightColor) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
        btn.setStyle(
                "-fx-background-color: " + darkColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: Monospace; " +
                "-fx-font-size: 12; " +
                "-fx-padding: 10 8; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle()
                .replace(darkColor, lightColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle()
                .replace(lightColor, darkColor)));
        return btn;
    }

    // -------------------------------------------------------------------------
    // Combat logic
    // -------------------------------------------------------------------------

    private void processTurn(CombatAction action) {
        // Disable buttons during animation
        actionBar.setDisable(true);

        CombatResult result = controller.executeCombatTurn(enemy, action);
        appendLog("▶ " + result.message());

        // Animate hero attack
        if (result.heroDamageDealt() > 0) {
            animateAttack(heroAvatar, true);
            animateDamage(enemyAvatar, enemyHpBar);
        }
        // Animate enemy attack
        if (result.enemyDamageDealt() > 0) {
            animateAttack(enemyAvatar, false);
            animateDamage(heroAvatar, heroHpBar);
        }

        // Update HP displays after short delay
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
                        appendLog("★ Victory! Room cleared.");
                    }
                    stage.close();
                    onCombatEnd.run();
                });
                endDelay.play();
            }
        });
        delay.play();
    }

    // -------------------------------------------------------------------------
    // Animations
    // -------------------------------------------------------------------------

    /** Shake animation when attacking. */
    private void animateAttack(Label avatar, boolean heroAttacking) {
        int dir = heroAttacking ? 1 : -1;
        TranslateTransition tt = new TranslateTransition(Duration.millis(80), avatar);
        tt.setByX(dir * 18);
        tt.setAutoReverse(true);
        tt.setCycleCount(2);
        tt.play();
    }

    /** Red flash + scale pulse when taking damage. */
    private void animateDamage(Label avatar, ProgressBar bar) {
        // Flash red
        String original = avatar.getStyle();
        avatar.setStyle(original + "-fx-background-color: rgba(255,0,0,0.4);");
        PauseTransition flash = new PauseTransition(Duration.millis(200));
        flash.setOnFinished(e -> avatar.setStyle(original));
        flash.play();

        // Scale shake
        ScaleTransition st = new ScaleTransition(Duration.millis(100), avatar);
        st.setToX(1.2); st.setToY(1.2);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        // HP bar color warning
        if (bar.getProgress() < 0.3) {
            bar.setStyle(bar.getStyle().replace("#00e5ff", "#ff8800")
                    .replace("#ff4444", "#ff0000"));
        }
    }

    private void updateHpDisplay() {
        Hero hero = controller.getHero();
        heroHpLabel.setText("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        double heroRatio = (double) hero.getCurrentHp() / hero.getMaxHp();
        heroHpBar.setProgress(heroRatio);

        enemyHpLabel.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        double enemyRatio = (double) enemy.getCurrentHp() / enemy.getMaxHp();
        enemyHpBar.setProgress(enemyRatio);
    }

    private void appendLog(String text) {
        logArea.appendText(text + "\n");
    }

    // -------------------------------------------------------------------------
    // Emoji mappings
    // -------------------------------------------------------------------------

    private String getEnemyEmoji(EnemyType type) {
        return switch (type) {
            case GOBLIN    -> "👺";
            case SKELETON  -> "💀";
            case DARK_MAGE -> "🧙";
            case TROLL     -> "👹";
            case ASSASSIN  -> "🗡";
            case DRAGON    -> "🐉";
        };
    }

    private String getHeroEmoji(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "⚔";
            case MAGE    -> "🔮";
            case ARCHER  -> "🏹";
        };
    }
}
