package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.HeroClass;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FXML controller for the character creation screen.
 * Features: particle background, animated class preview with stat bars,
 * avatar float animation, title glow, button hover effects.
 */
public class CharacterCreationViewController {

    @FXML private Pane   particlePane;
    @FXML private Label  titleLabel;
    @FXML private TextField nameField;
    @FXML private HBox   classBox;
    @FXML private Label  classInfo;
    @FXML private Button startBtn;
    @FXML private Button backBtn;
    @FXML private VBox   previewPanel;
    @FXML private Label  previewAvatar;
    @FXML private Label  previewClassName;
    @FXML private VBox   statBars;

    private GameController gameController;
    private Runnable       onGameStarted;
    private Runnable       onReturnToMenu;
    private ToggleGroup    classGroup;

    private TranslateTransition avatarFloat;
    private final Random rng = new Random();

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        classGroup = new ToggleGroup();

        for (HeroClass hc : HeroClass.values()) {
            RadioButton rb = new RadioButton(hc.name());
            rb.setToggleGroup(classGroup);
            rb.setUserData(hc);
            rb.setStyle(
                    "-fx-text-fill: #c0c0e0; -fx-font-family: Monospace; " +
                    "-fx-font-size: 13; -fx-cursor: hand;"
            );
            if (hc == HeroClass.WARRIOR) rb.setSelected(true);
            classBox.getChildren().add(rb);
        }

        classGroup.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw != null) updatePreview((HeroClass) nw.getUserData());
        });
    }

    public void setup(GameController gameController, Runnable onGameStarted,
                      Runnable onReturnToMenu) {
        this.gameController  = gameController;
        this.onGameStarted   = onGameStarted;
        this.onReturnToMenu  = onReturnToMenu;

        setupParticles();
        setupTitleGlow();
        setupStartButton();
        setupBackButton();
        updatePreview(HeroClass.WARRIOR);
        animatePreviewEntrance();
    }

    @FXML
    private void handleBack() { onReturnToMenu.run(); }

    private void setupBackButton() {
        String base  = backBtn.getStyle();
        String hover = base.replace("#606080", "#a0a0c0")
                          .replace("#303050", "#606080");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(hover));
        backBtn.setOnMouseExited(e  -> backBtn.setStyle(base));
    }

    // -------------------------------------------------------------------------
    // Action handler
    // -------------------------------------------------------------------------

    @FXML
    private void handleStart() {
        String name = nameField.getText().trim();
        if (name.isBlank()) {
            nameField.setStyle(nameField.getStyle() + "-fx-border-color: #ff4444;");
            shakeNode(nameField);
            return;
        }
        Toggle selected = classGroup.getSelectedToggle();
        HeroClass heroClass = selected != null
                ? (HeroClass) selected.getUserData()
                : HeroClass.WARRIOR;

        gameController.startNewGame(name, heroClass);
        onGameStarted.run();
    }

    // -------------------------------------------------------------------------
    // Preview panel
    // -------------------------------------------------------------------------

    private void updatePreview(HeroClass hc) {
        // Avatar
        previewAvatar.setText(getAvatar(hc));
        previewClassName.setText(hc.name());
        previewClassName.setTextFill(getClassColor(hc));

        // Restart float animation
        if (avatarFloat != null) avatarFloat.stop();
        avatarFloat = new TranslateTransition(Duration.millis(1400), previewAvatar);
        avatarFloat.setByY(-10);
        avatarFloat.setAutoReverse(true);
        avatarFloat.setCycleCount(Animation.INDEFINITE);
        avatarFloat.play();

        // Glow on avatar
        Glow glow = new Glow(0.5);
        previewAvatar.setEffect(glow);
        Timeline glowTl = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(glow.levelProperty(), 0.3)),
                new KeyFrame(Duration.millis(900),  new KeyValue(glow.levelProperty(), 0.8))
        );
        glowTl.setAutoReverse(true);
        glowTl.setCycleCount(Animation.INDEFINITE);
        glowTl.play();

        // Description
        classInfo.setText(getClassDescription(hc));

        // Stat bars
        buildStatBars(hc);

        // Pop animation on preview panel
        ScaleTransition pop = new ScaleTransition(Duration.millis(200), previewPanel);
        pop.setFromX(0.95); pop.setFromY(0.95);
        pop.setToX(1.0);    pop.setToY(1.0);
        pop.setInterpolator(Interpolator.EASE_OUT);
        pop.play();
    }

    private void buildStatBars(HeroClass hc) {
        statBars.getChildren().clear();

        int[] values = getStatValues(hc); // [hp, atk, def, mag] normalized 0-100
        String[][] labels = {
                {"HP",  "#ff6666"},
                {"ATK", "#ff8844"},
                {"DEF", "#44aaff"},
                {"MAG", "#cc66ff"}
        };

        for (int i = 0; i < labels.length; i++) {
            statBars.getChildren().add(buildStatRow(labels[i][0], labels[i][1], values[i]));
        }
    }

    private HBox buildStatRow(String label, String color, int value) {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web(color));
        lbl.setMinWidth(35);

        ProgressBar bar = new ProgressBar(0);
        bar.setPrefWidth(130);
        bar.setPrefHeight(10);
        bar.setStyle("-fx-accent: " + color + ";");

        // Animate bar fill
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(400),
                        new KeyValue(bar.progressProperty(),
                                value / 100.0, Interpolator.EASE_OUT))
        );
        tl.play();

        row.getChildren().addAll(lbl, bar);
        return row;
    }

    private void animatePreviewEntrance() {
        previewPanel.setOpacity(0);
        previewPanel.setTranslateX(30);
        FadeTransition fade = new FadeTransition(Duration.millis(500), previewPanel);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), previewPanel);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, slide).play();
    }

    // -------------------------------------------------------------------------
    // Title glow
    // -------------------------------------------------------------------------

    private void setupTitleGlow() {
        DropShadow glow = new DropShadow(12, Color.web("#e94560"));
        titleLabel.setEffect(glow);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(glow.radiusProperty(), 6)),
                new KeyFrame(Duration.millis(1200), new KeyValue(glow.radiusProperty(), 24))
        );
        tl.setAutoReverse(true);
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
    }

    // -------------------------------------------------------------------------
    // Start button
    // -------------------------------------------------------------------------

    private void setupStartButton() {
        String base  = startBtn.getStyle();
        String hover = base.replace("#8b1a2a", "#c0392b");
        startBtn.setOnMouseEntered(e -> {
            startBtn.setStyle(hover);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), startBtn);
            st.setToX(1.04); st.setToY(1.04);
            st.play();
        });
        startBtn.setOnMouseExited(e -> {
            startBtn.setStyle(base);
            ScaleTransition st = new ScaleTransition(Duration.millis(100), startBtn);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });
    }

    // -------------------------------------------------------------------------
    // Particle background
    // -------------------------------------------------------------------------

    private void setupParticles() {
        particlePane.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            if (bounds.getWidth() <= 0) return;

            particlePane.getChildren().clear();
            Canvas canvas = new Canvas(bounds.getWidth(), bounds.getHeight());
            particlePane.getChildren().add(canvas);

            List<MenuParticle> particles = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                particles.add(new MenuParticle(bounds.getWidth(), bounds.getHeight(), rng));
            }

            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, bounds.getWidth(), bounds.getHeight());
                    for (MenuParticle p : particles) {
                        p.update(bounds.getHeight());
                        gc.setGlobalAlpha(p.opacity);
                        gc.setFill(p.color);
                        gc.fillOval(p.x, p.y, p.size, p.size);
                    }
                    gc.setGlobalAlpha(1.0);
                }
            }.start();
        });
    }

    // -------------------------------------------------------------------------
    // Shake animation for invalid input
    // -------------------------------------------------------------------------

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setByX(8);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.play();
    }

    // -------------------------------------------------------------------------
    // Data helpers
    // -------------------------------------------------------------------------

    private String getAvatar(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "⚔";
            case MAGE    -> "🔮";
            case ARCHER  -> "🏹";
        };
    }

    private Color getClassColor(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> Color.web("#ff8844");
            case MAGE    -> Color.web("#cc66ff");
            case ARCHER  -> Color.web("#44ff88");
        };
    }

    private String getClassDescription(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "High HP & defense.\nPower Strike: 150% ATK damage.";
            case MAGE    -> "High magic power.\nMagic Blast: magic x2 damage.";
            case ARCHER  -> "Balanced stats.\nPrecise Shot: ATK + magic damage.";
        };
    }

    /**
     * Returns normalized stat values [0-100] for HP, ATK, DEF, MAG.
     * Based on HeroClass base stats: WARRIOR(110hp,13atk,10def,2mag),
     * MAGE(80hp,5atk,3def,15mag), ARCHER(95hp,11atk,3def,8mag).
     */
    private int[] getStatValues(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> new int[]{100, 90, 80, 20};
            case MAGE    -> new int[]{45,  30, 20, 100};
            case ARCHER  -> new int[]{70,  70, 60, 40};
        };
    }

    // -------------------------------------------------------------------------
    // Particle data class
    // -------------------------------------------------------------------------

    private static class MenuParticle {
        double x, y, size, speed, opacity;
        Color  color;

        private static final Color[] COLORS = {
                Color.web("#e94560", 0.5),
                Color.web("#4444cc", 0.4),
                Color.web("#8844ff", 0.4),
                Color.web("#ffffff", 0.2),
        };

        MenuParticle(double w, double h, Random rng) {
            x       = rng.nextDouble() * w;
            y       = rng.nextDouble() * h;
            size    = 1.5 + rng.nextDouble() * 3.0;
            speed   = 0.3 + rng.nextDouble() * 0.6;
            opacity = 0.15 + rng.nextDouble() * 0.4;
            color   = COLORS[rng.nextInt(COLORS.length)];
        }

        void update(double h) {
            y -= speed;
            if (y < -10) {
                y       = h + 5;
                x       = new Random().nextDouble() * 940;
                opacity = 0.15 + new Random().nextDouble() * 0.4;
            }
        }
    }
}