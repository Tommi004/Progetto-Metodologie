package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.*;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML controller for the combat modal screen.
 * Features: animated HP bars, floating damage numbers, attack/damage animations,
 * level-up overlay with animated stat gains.
 * Scene structure is defined in Combat.fxml.
 */
public class CombatViewController {

    @FXML private Label       heroAvatar;
    @FXML private Label       heroNameLabel;
    @FXML private Label       heroHpLabel;
    @FXML private ProgressBar heroHpBar;
    @FXML private Label       heroLevelLabel;
    @FXML private Label       heroManaLabel;
    @FXML private ProgressBar heroManaBar;

    @FXML private Label       vsLabel;

    @FXML private Label       enemyAvatar;
    @FXML private Label       enemyNameLabel;
    @FXML private Label       enemyHpLabel;
    @FXML private ProgressBar enemyHpBar;
    @FXML private Label       enemyTypeLabel;

    @FXML private TextArea    logArea;
    @FXML private HBox        actionBar;
    @FXML private Button      specialBtn;

    @FXML private StackPane   heroPanelBox;
    @FXML private StackPane   enemyPanelBox;

    private GameController gameController;
    private Enemy          enemy;
    private Stage          stage;
    private Runnable       onCombatEnd;

    private Timeline heroHpTimeline  = new Timeline();
    private Timeline enemyHpTimeline = new Timeline();

    public void setup(GameController gameController, Enemy enemy,
                      Stage stage, Runnable onCombatEnd) {
        this.gameController = gameController;
        this.enemy          = enemy;
        this.stage          = stage;
        this.onCombatEnd    = onCombatEnd;
        populateInitialState();
        startAnimations();
    }

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    private void populateInitialState() {
        Hero hero = gameController.getHero();

        heroAvatar.setText(getHeroEmoji(hero.getHeroClass()));
        heroNameLabel.setText(hero.getName());
        heroHpLabel.setText("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        heroHpBar.setProgress((double) hero.getCurrentHp() / hero.getMaxHp());
        heroLevelLabel.setText("Lv." + hero.getLevel() + " " + hero.getHeroClass());
        updateManaDisplay(hero);

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

    // -------------------------------------------------------------------------
    // Idle animations
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    @FXML private void handleAttack()  { processTurn(CombatAction.ATTACK);     }
    @FXML private void handleSpecial() { processTurn(CombatAction.SPECIAL);    }
    @FXML private void handlePotion()  { processTurn(CombatAction.USE_POTION); }
    @FXML private void handleFlee()    { processTurn(CombatAction.FLEE);       }

    // -------------------------------------------------------------------------
    // Core turn logic
    // -------------------------------------------------------------------------

    private void processTurn(CombatAction action) {
        actionBar.setDisable(true);

        Hero hero = gameController.getHero();
        int levelBefore = hero.getLevel();
        int maxHpBefore  = hero.getMaxHp();
        int atkBefore    = hero.getAttack();
        int defBefore    = hero.getDefense();
        int magBefore    = hero.getMagic();
        int manaBefore   = hero.getMaxMana();

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
                    (double) hero.getCurrentHp() / hero.getMaxHp(),
                    heroHpTimeline);
            animateHpBar(enemyHpBar,
                    (double) enemy.getCurrentHp() / enemy.getMaxHp(),
                    enemyHpTimeline);
            updateHpLabels();

            // Check for level-up and show overlay before re-enabling actions
            if (hero.getLevel() > levelBefore) {
                appendLog("★ LEVEL UP! → Lv." + hero.getLevel());
                showLevelUpOverlay(hero, levelBefore,
                        maxHpBefore, atkBefore, defBefore, magBefore, manaBefore,
                        () -> finishTurn(result));
            } else {
                finishTurn(result);
            }
        });
        delay.play();
    }

    /**
     * Re-enables the action bar and, if the combat is over, triggers endCombat.
     * Extracted so it can be called both after a normal turn and after
     * the level-up overlay closes.
     */
    private void finishTurn(CombatResult result) {
        heroLevelLabel.setText("Lv." + gameController.getHero().getLevel()
                + " " + gameController.getHero().getHeroClass());
        actionBar.setDisable(false);
        if (result.isCombatOver()) endCombat(result);
    }

    // -------------------------------------------------------------------------
    // Level-up overlay
    // -------------------------------------------------------------------------

    /**
     * Displays an animated level-up overlay on heroPanelBox.
     * The overlay shows the new level and stat gains, fades out after 2.2 s,
     * then invokes {@code onDone}.
     *
     * @param hero       the hero (already at the new level)
     * @param oldLevel   level before the gain
     * @param oldMaxHp   maxHp before the gain
     * @param oldAtk     attack before the gain
     * @param oldDef     defense before the gain
     * @param oldMag     magic before the gain
     * @param onDone     callback invoked when the overlay finishes
     */
    private void showLevelUpOverlay(Hero hero, int oldLevel,
                                    int oldMaxHp, int oldAtk,
                                    int oldDef, int oldMag, int oldMaxMana,
                                    Runnable onDone) {

        VBox overlay = buildLevelUpOverlay(hero, oldMaxHp, oldAtk, oldDef, oldMag, oldMaxMana);
        heroPanelBox.getChildren().add(overlay);

        // Star burst particles
        for (int i = 0; i < 6; i++) {
            spawnLevelUpStar(heroPanelBox, i * 60);
        }

        // Entrance: scale + fade in
        overlay.setOpacity(0);
        overlay.setScaleX(0.6);
        overlay.setScaleY(0.6);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), overlay);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), overlay);
        fadeIn.setToValue(1.0);

        ParallelTransition entranceAnim = new ParallelTransition(scaleIn, fadeIn);

        // Glow pulse on the overlay title
        DropShadow overlayGlow = new DropShadow(20, Color.web("#ffd700"));
        overlay.setEffect(overlayGlow);
        Timeline glowPulse = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(overlayGlow.radiusProperty(), 10)),
                new KeyFrame(Duration.millis(500),  new KeyValue(overlayGlow.radiusProperty(), 30))
        );
        glowPulse.setAutoReverse(true);
        glowPulse.setCycleCount(4);

        // Exit: fade out, then remove overlay and invoke callback
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), overlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(1400));
        fadeOut.setOnFinished(ev -> {
            heroPanelBox.getChildren().remove(overlay);
            onDone.run();
        });

        new SequentialTransition(
                entranceAnim,
                glowPulse,
                fadeOut
        ).play();
    }

    /**
     * Builds the VBox that forms the level-up overlay content.
     */
    private VBox buildLevelUpOverlay(Hero hero,
                                     int oldMaxHp, int oldAtk,
                                     int oldDef, int oldMag, int oldMaxMana) {
        VBox overlay = new VBox(6);
        overlay.setAlignment(javafx.geometry.Pos.CENTER);
        overlay.setStyle(
                "-fx-background-color: rgba(0,0,0,0.82); " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 14 20 14 20;"
        );
        overlay.setMaxWidth(180);
        overlay.setMaxHeight(180);

        Label starLabel = new Label("★");
        starLabel.setFont(Font.font(28));
        starLabel.setTextFill(Color.web("#ffd700"));

        Label levelLabel = new Label("LEVEL  UP!");
        levelLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        levelLabel.setTextFill(Color.web("#ffd700"));
        levelLabel.setTextAlignment(TextAlignment.CENTER);

        Label newLvLabel = new Label("→  Lv. " + hero.getLevel());
        newLvLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        newLvLabel.setTextFill(Color.web("#ffffff"));

        VBox stats = new VBox(2);
        stats.setAlignment(javafx.geometry.Pos.CENTER);
        addStatGainRow(stats, "HP ",  hero.getMaxHp()   - oldMaxHp,   "#ff6666");
        addStatGainRow(stats, "ATK",  hero.getAttack()  - oldAtk,     "#ff8844");
        addStatGainRow(stats, "DEF",  hero.getDefense() - oldDef,     "#44aaff");
        addStatGainRow(stats, "MAG",  hero.getMagic()   - oldMag,     "#cc66ff");
        addStatGainRow(stats, "MP ",  hero.getMaxMana() - oldMaxMana, "#44ddff");

        overlay.getChildren().addAll(starLabel, levelLabel, newLvLabel, stats);
        return overlay;
    }

    /**
     * Adds a single "+N STAT" label row to the stats VBox.
     * Stat rows with zero gain are omitted.
     */
    private void addStatGainRow(VBox parent, String statName, int gain, String color) {
        if (gain <= 0) return;
        Label row = new Label("+" + gain + "  " + statName);
        row.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        row.setTextFill(Color.web(color));
        parent.getChildren().add(row);
    }

    /**
     * Spawns a single golden star that rises and fades above heroPanelBox.
     *
     * @param container  the StackPane to add the star to
     * @param delayMs    stagger delay in milliseconds
     */
    private void spawnLevelUpStar(StackPane container, int delayMs) {
        Text star = new Text("★");
        star.setFont(Font.font(14 + Math.random() * 10));
        star.setFill(Color.web("#ffd700", 0.9));
        star.setTranslateX((Math.random() - 0.5) * 100);
        star.setTranslateY(20);
        star.setOpacity(0);

        container.getChildren().add(star);

        TranslateTransition rise = new TranslateTransition(Duration.millis(900), star);
        rise.setByY(-70 - Math.random() * 30);

        FadeTransition fadeIn  = new FadeTransition(Duration.millis(200), star);
        fadeIn.setToValue(1.0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), star);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.millis(200));

        ParallelTransition pt = new ParallelTransition(rise, fadeIn, fadeOut);
        pt.setDelay(Duration.millis(delayMs));
        pt.setOnFinished(e -> container.getChildren().remove(star));
        pt.play();
    }

    // -------------------------------------------------------------------------
    // End combat
    // -------------------------------------------------------------------------

    private void endCombat(CombatResult result) {
        actionBar.setDisable(true);
        PauseTransition endDelay = new PauseTransition(Duration.millis(700));
        endDelay.setOnFinished(ev -> {
            if (result.enemyDefeated()) {
                if (enemy.getType() == EnemyType.DEMON_LORD) {
                    triggerDemonSoulTransformation();
                    return;
                }
                gameController.resolveEnemyDefeat(enemy.getId());
                appendLog("★ Victory! Room cleared.");
            }
            stage.close();
            onCombatEnd.run();
        });
        endDelay.play();
    }

    /**
     * Handles the Demon Lord phase-2 transformation.
     * Removes the Demon Lord from the room, spawns Demon Soul,
     * shows a dramatic overlay, then resets the combat UI for phase 2.
     */
    private void triggerDemonSoulTransformation() {
        gameController.resolveEnemyDefeat(enemy.getId());
        enemy = gameController.transformToDemonSoul();

        appendLog("══════════════════════════════");
        appendLog("☠  THE DEMON LORD IS NOT DEAD!");
        appendLog("☠  HIS SOUL RISES FROM THE ASHES...");
        appendLog("══════════════════════════════");

        showTransformationOverlay(() -> resetCombatForPhase2());
    }

    /**
     * Shows a dramatic full-screen overlay for the Demon Soul transformation
     * in a separate modal Stage (avoids cast issues with the FXML root BorderPane).
     * Invokes {@code onDone} after the animation finishes and the overlay closes.
     */
    private void showTransformationOverlay(Runnable onDone) {
        Stage overlayStage = new Stage();
        overlayStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        overlayStage.initOwner(stage);
        overlayStage.setTitle("");
        overlayStage.setResizable(false);

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(18);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: #0a0000;");

        javafx.scene.text.Text skull = new javafx.scene.text.Text("☠");
        skull.setFont(Font.font(80));
        skull.setFill(Color.web("#cc0000"));

        javafx.scene.text.Text line1 = new javafx.scene.text.Text("DEMON SOUL");
        line1.setFont(Font.font("Monospace", FontWeight.BOLD, 34));
        line1.setFill(Color.web("#ff2200"));

        javafx.scene.text.Text line2 = new javafx.scene.text.Text("returns from beyond");
        line2.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        line2.setFill(Color.web("#ff6600"));

        DropShadow glow = new DropShadow(30, Color.web("#ff0000"));
        line1.setEffect(glow);

        root.getChildren().addAll(skull, line1, line2);
        root.setOpacity(0);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 420, 280);
        overlayStage.setScene(scene);

        // Entrance fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
        fadeIn.setToValue(1.0);

        // Glow + skull pulse
        Timeline glowPulse = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(glow.radiusProperty(), 15)),
                new KeyFrame(Duration.millis(450),  new KeyValue(glow.radiusProperty(), 50))
        );
        glowPulse.setAutoReverse(true);
        glowPulse.setCycleCount(4);

        ScaleTransition skullPulse = new ScaleTransition(Duration.millis(500), skull);
        skullPulse.setFromX(0.8); skullPulse.setFromY(0.8);
        skullPulse.setToX(1.2);   skullPulse.setToY(1.2);
        skullPulse.setAutoReverse(true);
        skullPulse.setCycleCount(4);

        // Exit fade-out, then close stage and invoke callback
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            overlayStage.close();
            onDone.run();
        });

        new SequentialTransition(
                fadeIn,
                new ParallelTransition(glowPulse, skullPulse),
                new javafx.animation.PauseTransition(Duration.millis(400)),
                fadeOut
        ).play();

        overlayStage.show();
    }

    /**
     * Resets the combat UI panels to display the Demon Soul (phase 2).
     */
    private void resetCombatForPhase2() {
        enemyAvatar.setText("💀");
        enemyNameLabel.setText(enemy.getName());
        enemyHpLabel.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        enemyTypeLabel.setText("DEMON SOUL — PHASE 2");

        enemyHpTimeline.stop();
        enemyHpBar.setProgress(1.0);
        enemyHpBar.setStyle("-fx-accent: #aa00ff; -fx-background-color: #1a002a;");

        appendLog("══════════════════════════════");
        appendLog("☠  DEMON SOUL — PHASE 2  ☠");
        appendLog("HP: " + enemy.getCurrentHp() +
                "  ATK: " + enemy.getAttack() +
                "  MAG: " + enemy.getMagic());
        appendLog("══════════════════════════════");

        actionBar.setDisable(false);
    }

    // -------------------------------------------------------------------------
    // Combat animations
    // -------------------------------------------------------------------------

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

    private void animateHpBar(ProgressBar bar, double targetProgress, Timeline existing) {
        existing.stop();
        existing.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(350),
                        new KeyValue(bar.progressProperty(),
                                targetProgress, Interpolator.EASE_OUT))
        );
        existing.play();
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

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    private void updateHpLabels() {
        Hero hero = gameController.getHero();
        heroHpLabel.setText("HP: " + hero.getCurrentHp() + "/" + hero.getMaxHp());
        enemyHpLabel.setText("HP: " + enemy.getCurrentHp() + "/" + enemy.getMaxHp());
        updateManaDisplay(hero);
    }

    /**
     * Refreshes the mana label, mana bar and Special button state.
     * Special is disabled when current mana is below the class cost.
     */
    private void updateManaDisplay(Hero hero) {
        int cur  = hero.getCurrentMana();
        int max  = hero.getMaxMana();
        int cost = hero.getHeroClass().getSpecialManaCost();

        heroManaLabel.setText("MP  " + cur + " / " + max + "  (cost: " + cost + ")");
        heroManaBar.setProgress(max == 0 ? 0 : (double) cur / max);

        boolean hasMana = cur >= cost;
        specialBtn.setDisable(!hasMana);
        specialBtn.setOpacity(hasMana ? 1.0 : 0.45);
        specialBtn.setText(hasMana ? "✦  Special" : "✦  No MP");
    }

    private void appendLog(String text) { logArea.appendText(text + "\n"); }

    private String getEnemyEmoji(EnemyType type) {
        return switch (type) {
            case GOBLIN     -> "👺";
            case SKELETON   -> "💀";
            case DARK_MAGE  -> "🧙";
            case TROLL      -> "👹";
            case ASSASSIN   -> "🗡";
            case DRAGON     -> "🐉";
            case KNIGHT     -> "⚔";
            case WITCH      -> "🪄";
            case DEMON      -> "😈";
            case LEVIATHAN  -> "🐋";
            case DEMON_LORD -> "👑";
            case DEMON_SOUL -> "💀";
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