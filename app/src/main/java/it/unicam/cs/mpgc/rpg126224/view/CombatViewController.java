package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML controller for the combat modal screen.
 * Features: animated HP bars, floating damage numbers, attack/damage animations.
 * Scene structure is defined in Combat.fxml.
 */
public class CombatViewController {

    @FXML private Label       heroAvatar;
    @FXML private Label       heroNameLabel;
    @FXML private Label       heroHpLabel;
    @FXML private ProgressBar heroHpBar;
    @FXML private Label       heroLevelLabel;

    @FXML private Label       vsLabel;

    @FXML private Label       enemyAvatar;
    @FXML private Label       enemyNameLabel;
    @FXML private Label       enemyHpLabel;
    @FXML private ProgressBar enemyHpBar;
    @FXML private Label       enemyTypeLabel;

    @FXML private TextArea    logArea;
    @FXML private HBox        actionBar;

    @FXML private StackPane   heroPanelBox;
    @FXML private StackPane   enemyPanelBox;

    private GameController gameController;
    private Enemy          enemy;
    private Stage          stage;
    private Runnable       onCombatEnd;

    public void setup(GameController gameController, Enemy enemy,
                      Stage stage, Runnable onCombatEnd) {
        this.gameController = gameController;
        this.enemy          = enemy;
        this.stage          = stage;
        this.onCombatEnd    = onCombatEnd;
        populateInitialState();
        startAnimations();
    }

    private void populateInitialState() {
        Hero hero = gameController.getHero();

        heroAvatar.setText(getHeroEmoji(hero.getHeroClass()));
        heroNameLabel.setText(hero.getName());
        heroHpLabel.setText("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        heroHpBar.setProgress((double) hero.getCurrentHp() / hero.getMaxHp());
        heroLevelLabel.setText("Lv." + hero.getLevel() + " " + hero.getHeroClass());

        enemyAvatar.setText(getEnemyEmoji(enemy.getType()));
        enemyNameLabel.setText(enemy.getName());
        enemyHpLabel.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        enemyHpBar.setProgress((double) enemy.getCurrentHp() / enemy.getMaxHp());
        enemyTypeLabel.setText(enemy.getType().name());

        appendLog("══════════════════════════════");
        appendLog("⚔  COMBAT vs " + enemy.getName().toUpperCase() + "  ⚔");
        appendLog("HP: " + enemy.getCurrentHp() +
                "  ATK: " + enemy.getAttack() +
                "  DEF: " + enemy.getDefense());
        appendLog("══════════════════════════════");
    }

    private void startAnimations() {
        TranslateTransition floatAnim =
                new TranslateTransition(Duration.millis(1200), enemyAvatar);
        floatAnim.setByY(-7);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.play();

        Glow glow = new Glow(0.3);
        enemyAvatar.setEffect(glow);
        Timeline glowTl = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(glow.levelProperty(), 0.2)),
                new KeyFrame(Duration.millis(900),  new KeyValue(glow.levelProperty(), 0.7))
        );
        glowTl.setAutoReverse(true);
        glowTl.setCycleCount(Animation.INDEFINITE);
        glowTl.play();

        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), vsLabel);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.15);  pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    @FXML private void handleAttack()  { processTurn(CombatAction.ATTACK);     }
    @FXML private void handleSpecial() { processTurn(CombatAction.SPECIAL);    }
    @FXML private void handlePotion()  { processTurn(CombatAction.USE_POTION); }
    @FXML private void handleFlee()    { processTurn(CombatAction.FLEE);       }

    private void processTurn(CombatAction action) {
        actionBar.setDisable(true);

        CombatResult result = gameController.executeCombatTurn(enemy, action);
        appendLog("▶ " + result.message());

        if (result.heroDamageDealt() > 0) {
            animateAttack(heroAvatar, true);
            animateDamage(enemyAvatar, enemyHpBar);
            showDamageNumber(enemyPanelBox, result.heroDamageDealt(), false);
        }
        if (result.enemyDamageDealt() > 0) {
            animateAttack(enemyAvatar, false);
            animateDamage(heroAvatar, heroHpBar);
            showDamageNumber(heroPanelBox, result.enemyDamageDealt(), true);
        }

        PauseTransition delay = new PauseTransition(Duration.millis(400));
        delay.setOnFinished(e -> {
            animateHpBar(heroHpBar,
                    (double) gameController.getHero().getCurrentHp() /
                             gameController.getHero().getMaxHp());
            animateHpBar(enemyHpBar,
                    (double) enemy.getCurrentHp() / enemy.getMaxHp());
            updateHpLabels();
            actionBar.setDisable(false);
            if (result.isCombatOver()) endCombat(result);
        });
        delay.play();
    }

    private void endCombat(CombatResult result) {
        actionBar.setDisable(true);
        PauseTransition endDelay = new PauseTransition(Duration.millis(700));
        endDelay.setOnFinished(ev -> {
            if (result.enemyDefeated()) {
                gameController.resolveEnemyDefeat(enemy.getId());
                appendLog("★ Victory! Room cleared.");
            }
            stage.close();
            onCombatEnd.run();
        });
        endDelay.play();
    }

    private void animateAttack(Label avatar, boolean heroAttacking) {
        int dir = heroAttacking ? 1 : -1;
        TranslateTransition tt = new TranslateTransition(Duration.millis(80), avatar);
        tt.setByX(dir * 20);
        tt.setAutoReverse(true);
        tt.setCycleCount(2);
        tt.play();
    }

    private void animateDamage(Label avatar, ProgressBar bar) {
        String original = avatar.getStyle();
        avatar.setStyle(original + "-fx-background-color: rgba(255,50,50,0.5);");
        PauseTransition flash = new PauseTransition(Duration.millis(220));
        flash.setOnFinished(e -> avatar.setStyle(original));
        flash.play();

        ScaleTransition st = new ScaleTransition(Duration.millis(90), avatar);
        st.setToX(1.25); st.setToY(1.25);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        if (bar.getProgress() < 0.3) {
            bar.setStyle(bar.getStyle()
                    .replace("#00e5ff", "#ff8800")
                    .replace("#ff4444", "#ff0000"));
        }
    }

    private void animateHpBar(ProgressBar bar, double targetProgress) {
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(400),
                        new KeyValue(bar.progressProperty(),
                                targetProgress, Interpolator.EASE_OUT))
        );
        tl.play();
    }

    private void showDamageNumber(StackPane panel, int damage, boolean isHero) {
        Text dmgText = new Text("-" + damage);
        dmgText.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        dmgText.setFill(isHero ? Color.web("#ff4444") : Color.web("#ffd700"));
        dmgText.setStroke(Color.web("#000000", 0.7));
        dmgText.setStrokeWidth(1);
        dmgText.setTranslateX((Math.random() - 0.5) * 40);
        dmgText.setTranslateY(0);
        dmgText.setOpacity(1);

        panel.getChildren().add(dmgText);

        TranslateTransition rise = new TranslateTransition(Duration.millis(700), dmgText);
        rise.setByY(-50);
        FadeTransition fade = new FadeTransition(Duration.millis(700), dmgText);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(rise, fade);
        pt.setDelay(Duration.millis(100));
        pt.setOnFinished(e -> panel.getChildren().remove(dmgText));
        pt.play();
    }

    private void updateHpLabels() {
        Hero hero = gameController.getHero();
        heroHpLabel.setText("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        enemyHpLabel.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
    }

    private void appendLog(String text) { logArea.appendText(text + "\n"); }

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