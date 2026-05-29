package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FXML controller for the main menu screen.
 * Features: floating particle background on Canvas, pulsing title glow,
 * staggered button fade-in.
 */
public class MainMenuViewController {

    @FXML private Pane   particlePane;
    @FXML private Label  titleLabel;
    @FXML private VBox   buttonBox;
    @FXML private Button newGameBtn;
    @FXML private Button loadGameBtn;
    @FXML private Button quitBtn;

    private GameController gameController;
    private Runnable       onNewGame;
    private Runnable       onLoadGame;
    private Runnable       onQuit;

    private Canvas          particleCanvas;
    private AnimationTimer  particleTimer;
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    /**
     * Injects dependencies. Must be called right after FXMLLoader.load().
     */
    public void setup(GameController gameController,
                      Runnable onNewGame, Runnable onLoadGame, Runnable onQuit) {
        this.gameController = gameController;
        this.onNewGame  = onNewGame;
        this.onLoadGame = onLoadGame;
        this.onQuit     = onQuit;

        loadGameBtn.setDisable(!gameController.getPersistenceManager().hasSaveFile());

        setupParticles();
        setupTitleGlow();
        setupButtonAnimations();
        setupButtonHover();
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    @FXML
    private void handleNewGame() { onNewGame.run(); }

    @FXML
    private void handleLoadGame() {
        if (gameController.loadGame()) onLoadGame.run();
    }

    @FXML
    private void handleQuit() { onQuit.run(); }

    // -------------------------------------------------------------------------
    // Particle background
    // -------------------------------------------------------------------------

    private void setupParticles() {
        // Create canvas sized to the pane once it has dimensions
        particlePane.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            if (bounds.getWidth() <= 0) return;

            if (particleCanvas != null) particlePane.getChildren().remove(particleCanvas);

            particleCanvas = new Canvas(bounds.getWidth(), bounds.getHeight());
            particlePane.getChildren().add(particleCanvas);

            // Spawn initial particles
            particles.clear();
            for (int i = 0; i < 60; i++) {
                particles.add(new Particle(bounds.getWidth(), bounds.getHeight(), rng));
            }

            if (particleTimer != null) particleTimer.stop();
            particleTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    updateParticles(bounds.getWidth(), bounds.getHeight());
                }
            };
            particleTimer.start();
        });
    }

    private void updateParticles(double w, double h) {
        GraphicsContext gc = particleCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        for (Particle p : particles) {
            p.update(h);
            gc.setGlobalAlpha(p.opacity);
            gc.setFill(p.color);
            gc.fillOval(p.x, p.y, p.size, p.size);
        }
        gc.setGlobalAlpha(1.0);
    }

    // -------------------------------------------------------------------------
    // Title glow
    // -------------------------------------------------------------------------

    private void setupTitleGlow() {
        DropShadow glow = new DropShadow(15, Color.web("#e94560"));
        titleLabel.setEffect(glow);

        Timeline glowTl = new Timeline(
                new KeyFrame(Duration.ZERO,         new KeyValue(glow.radiusProperty(), 8)),
                new KeyFrame(Duration.millis(1400),  new KeyValue(glow.radiusProperty(), 30))
        );
        glowTl.setAutoReverse(true);
        glowTl.setCycleCount(Animation.INDEFINITE);
        glowTl.play();

        // Subtle vertical bob on title
        TranslateTransition bob = new TranslateTransition(Duration.millis(2000), titleLabel);
        bob.setByY(-6);
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.play();
    }

    // -------------------------------------------------------------------------
    // Button entrance animation
    // -------------------------------------------------------------------------

    private void setupButtonAnimations() {
        // Staggered fade-in + slide up for each button
        Button[] btns = {newGameBtn, loadGameBtn, quitBtn};
        buttonBox.setOpacity(1); // parent visible immediately

        for (int i = 0; i < btns.length; i++) {
            Button btn = btns[i];
            btn.setOpacity(0);
            btn.setTranslateY(20);

            PauseTransition wait = new PauseTransition(Duration.millis(300 + i * 150));
            wait.setOnFinished(e -> {
                FadeTransition fade = new FadeTransition(Duration.millis(400), btn);
                fade.setToValue(1);
                TranslateTransition slide = new TranslateTransition(Duration.millis(400), btn);
                slide.setToY(0);
                new ParallelTransition(fade, slide).play();
            });
            wait.play();
        }
    }

    // -------------------------------------------------------------------------
    // Button hover effects
    // -------------------------------------------------------------------------

    private void setupButtonHover() {
        addHover(newGameBtn,  "#8b1a2a", "#c0392b");
        addHover(loadGameBtn, "#0e3a52", "#1a5276");
        addHover(quitBtn,     "#1e1e2e", "#2a2a40");
    }

    private void addHover(Button btn, String normal, String hover) {
        String baseStyle = btn.getStyle();
        btn.setOnMouseEntered(e -> {
            btn.setStyle(baseStyle.replace(normal, hover));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setToX(1.03); st.setToY(1.03);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });
    }

    // -------------------------------------------------------------------------
    // Particle data class
    // -------------------------------------------------------------------------

    private static class Particle {
        double x, y, size, speed, opacity;
        Color  color;

        private static final Color[] COLORS = {
                Color.web("#e94560", 0.6),
                Color.web("#4466ff", 0.4),
                Color.web("#ffd700", 0.5),
                Color.web("#aa44ff", 0.4),
                Color.web("#ffffff", 0.3),
        };

        Particle(double w, double h, Random rng) {
            reset(w, h, rng, true);
        }

        void reset(double w, double h, Random rng, boolean randomY) {
            x       = rng.nextDouble() * w;
            y       = randomY ? rng.nextDouble() * h : h + 10;
            size    = 1.5 + rng.nextDouble() * 3.5;
            speed   = 0.3 + rng.nextDouble() * 0.7;
            opacity = 0.2 + rng.nextDouble() * 0.6;
            color   = COLORS[rng.nextInt(COLORS.length)];
        }

        void update(double h) {
            y -= speed;
            if (y < -10) {
                x       = new Random().nextDouble() * 940;
                y       = h + 10;
                opacity = 0.2 + new Random().nextDouble() * 0.5;
            }
        }
    }
}