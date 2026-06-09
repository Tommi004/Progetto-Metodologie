package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.model.Item;
import it.unicam.cs.mpgc.rpg126224.model.ItemType;
import it.unicam.cs.mpgc.rpg126224.model.Rarity;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enhanced treasure popup with golden particle background,
 * animated title, glow effects and staggered item card entrance.
 */
public class TreasureView {

    private final List<Item> items;
    private final Random     rng = new Random();

    public TreasureView(List<Item> items) {
        this.items = items;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Treasure Found!");
        stage.setResizable(false);

        double width  = 420;
        double height = 150 + items.size() * 90;

        // Root is a StackPane so particles sit behind content
        StackPane root = new StackPane();
        // Background color based on highest rarity in loot
        Rarity maxRarity = items.stream()
                .map(Item::getRarity)
                .max(java.util.Comparator.comparingInt(Enum::ordinal))
                .orElse(Rarity.COMMON);

        String bgColor = switch (maxRarity) {
            case COMMON    -> "#0d0d0d";
            case RARE      -> "#000d1a";
            case EPIC      -> "#0d0018";
            case LEGENDARY -> "#1a0d00";
        };
        root.setStyle("-fx-background-color: " + bgColor + ";");

        // Particle canvas
        Canvas particleCanvas = new Canvas(width, height);
        List<GoldParticle> particles = spawnParticles((int) width, (int) height);
        AnimationTimer timer = buildParticleTimer(particleCanvas, particles, width, height);
        timer.start();

        // Content layer
        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28));

        content.getChildren().add(buildTitle(maxRarity));
        content.getChildren().add(buildItemsBox());
        content.getChildren().add(buildCloseButton(stage, timer));

        root.getChildren().addAll(particleCanvas, content);

        // Entrance fade
        root.setOpacity(0);
        FadeTransition entrance = new FadeTransition(Duration.millis(250), root);
        entrance.setToValue(1);
        entrance.play();

        stage.setScene(new Scene(root, width, height));
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------
    // UI sections
    // -------------------------------------------------------------------------

    private Label buildTitle(Rarity maxRarity) {
        String titleText = switch (maxRarity) {
            case COMMON    -> "✦  TREASURE FOUND!  ✦";
            case RARE      -> "✦✦  RARE TREASURE!  ✦✦";
            case EPIC      -> "★★  EPIC TREASURE!  ★★";
            case LEGENDARY -> "✦★  LEGENDARY LOOT!  ★✦";
        };
        String glowHex = maxRarity.getColor();

        Label title = new Label(titleText);
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(glowHex));

        DropShadow glow = new DropShadow(18, Color.web(glowHex));
        title.setEffect(glow);

        double glowMax = switch (maxRarity) {
            case COMMON    -> 20;
            case RARE      -> 28;
            case EPIC      -> 36;
            case LEGENDARY -> 48;
        };

        Timeline glowTl = new Timeline(
                new KeyFrame(Duration.ZERO,        new KeyValue(glow.radiusProperty(), 8)),
                new KeyFrame(Duration.millis(900),  new KeyValue(glow.radiusProperty(), glowMax))
        );
        glowTl.setAutoReverse(true);
        glowTl.setCycleCount(Animation.INDEFINITE);
        glowTl.play();

        ScaleTransition pop = new ScaleTransition(Duration.millis(500), title);
        pop.setFromX(0.6); pop.setFromY(0.6);
        pop.setToX(1.0);   pop.setToY(1.0);
        pop.setInterpolator(Interpolator.EASE_OUT);
        pop.play();

        return title;
    }

    private VBox buildItemsBox() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        for (int i = 0; i < items.size(); i++) {
            box.getChildren().add(buildItemCard(items.get(i), i));
        }
        return box;
    }

    private HBox buildItemCard(Item item, int index) {
        Rarity rarity = item.getRarity();
        String rarityColor = rarity.getColor();

        String cardBg = switch (rarity) {
            case COMMON    -> "#0d0d0d";
            case RARE      -> "#001428";
            case EPIC      -> "#120028";
            case LEGENDARY -> "#1a0d00";
        };

        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 18, 10, 18));
        card.setStyle(
                "-fx-background-color: " + cardBg + "; " +
                "-fx-border-color: " + rarityColor + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;"
        );
        card.setMaxWidth(350);

        // Border glow for rare+
        if (rarity != Rarity.COMMON) {
            DropShadow cardGlow = new DropShadow(8, Color.web(rarityColor, 0.6));
            card.setEffect(cardGlow);
            if (rarity == Rarity.LEGENDARY) {
                Timeline cardPulse = new Timeline(
                        new KeyFrame(Duration.ZERO,        new KeyValue(cardGlow.radiusProperty(), 6)),
                        new KeyFrame(Duration.millis(800),  new KeyValue(cardGlow.radiusProperty(), 18))
                );
                cardPulse.setAutoReverse(true);
                cardPulse.setCycleCount(Timeline.INDEFINITE);
                cardPulse.play();
            }
        }

        // Icon
        Label icon = new Label(getItemEmoji(item.getType()));
        icon.setFont(Font.font("Segoe UI Emoji", 28));
        Glow iconGlow = new Glow(rarity == Rarity.LEGENDARY ? 0.8 : 0.5);
        icon.setEffect(iconGlow);

        // Text info
        VBox info = new VBox(3);

        // Rarity badge + name
        Label rarityBadge = new Label("[" + rarity.getDisplayName().toUpperCase() + "]");
        rarityBadge.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        rarityBadge.setTextFill(Color.web(rarityColor));

        Label name = new Label(item.getName());
        name.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        name.setTextFill(Color.web(rarityColor));

        Label effect = new Label(getEffectDescription(item));
        effect.setFont(Font.font("Monospace", 11));
        effect.setTextFill(Color.web(rarityColor, 0.7));

        info.getChildren().addAll(rarityBadge, name, effect);
        card.getChildren().addAll(icon, info);

        // Staggered slide-in from left + fade
        card.setTranslateX(-50);
        card.setOpacity(0);
        PauseTransition wait = new PauseTransition(Duration.millis(120 + index * 130));
        wait.setOnFinished(e -> {
            TranslateTransition slide = new TranslateTransition(Duration.millis(320), card);
            slide.setToX(0);
            slide.setInterpolator(Interpolator.EASE_OUT);
            FadeTransition fade = new FadeTransition(Duration.millis(320), card);
            fade.setToValue(1);
            new ParallelTransition(slide, fade).play();
        });
        wait.play();

        return card;
    }

    private Button buildCloseButton(Stage stage, AnimationTimer timer) {
        Button btn = new Button("✦  Take Items  ✦");
        btn.setStyle(
                "-fx-background-color: #7a5a00; -fx-text-fill: #ffd700; " +
                "-fx-font-family: Monospace; -fx-font-size: 13; " +
                "-fx-padding: 9 28; -fx-background-radius: 6; -fx-cursor: hand;"
        );

        DropShadow btnGlow = new DropShadow(10, Color.web("#ffd700", 0.4));
        btn.setEffect(btnGlow);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle().replace("#7a5a00", "#b08000"));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.04); st.setToY(1.04);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(btn.getStyle().replace("#b08000", "#7a5a00"));
            ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });

        btn.setOpacity(0);
        PauseTransition wait = new PauseTransition(
                Duration.millis(200 + items.size() * 130));
        wait.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(300), btn);
            fade.setToValue(1);
            fade.play();
        });
        wait.play();

        btn.setOnAction(e -> { timer.stop(); stage.close(); });
        return btn;
    }

    // -------------------------------------------------------------------------
    // Particle system
    // -------------------------------------------------------------------------

    private List<GoldParticle> spawnParticles(int w, int h) {
        List<GoldParticle> list = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            list.add(new GoldParticle(w, h, rng));
        }
        return list;
    }

    private AnimationTimer buildParticleTimer(Canvas canvas,
                                              List<GoldParticle> particles,
                                              double w, double h) {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, w, h);
                for (GoldParticle p : particles) {
                    p.update(h);
                    gc.setGlobalAlpha(p.opacity);
                    gc.setFill(p.color);
                    gc.fillOval(p.x, p.y, p.size, p.size);
                }
                gc.setGlobalAlpha(1.0);
            }
        };
    }

    // -------------------------------------------------------------------------
    // Mappings
    // -------------------------------------------------------------------------

    private String getItemEmoji(ItemType type) {
        return switch (type) {
            case HEALTH_POTION   -> "🧪";
            case SWORD           -> "⚔";
            case SPEAR           -> "🔱";
            case BOW             -> "🏹";
            case CROSSBOW        -> "🎯";
            case STAFF           -> "🔮";
            case ARMOR           -> "🛡";
            case HELMET          -> "⛑";
            case AMULET          -> "📿";
            case STRENGTH_POTION -> "💪";
            case MANA_POTION     -> "💧";
        };
    }

    private String getEffectDescription(Item item) {
        return switch (item.getType()) {
            case HEALTH_POTION   -> "Restores +" + item.getValue() + " HP";
            case SWORD, SPEAR    -> "Attack +"   + item.getValue() + " (permanent)";
            case BOW             -> "Attack +"   + item.getValue() + " + Magic +" + (int)(item.getValue() * 0.3) + " (permanent)";
            case CROSSBOW        -> "Attack +"   + item.getValue() + " + Magic +" + item.getValue() + " (permanent)";
            case STAFF, AMULET   -> "Magic +"    + item.getValue() + " (permanent)";
            case ARMOR, HELMET   -> "Defense +"  + item.getValue() + " (permanent)";
            case STRENGTH_POTION -> "Attack +"   + item.getValue() + " boost";
            case MANA_POTION     -> "Restores +" + item.getValue() + " MP";
        };
    }

    // -------------------------------------------------------------------------
    // Particle data class
    // -------------------------------------------------------------------------

    private static class GoldParticle {
        double x, y, size, speed, opacity;
        Color  color;

        private static final Color[] COLORS = {
                Color.web("#ffd700", 0.7),
                Color.web("#ffaa00", 0.5),
                Color.web("#ffe866", 0.6),
                Color.web("#ffffff", 0.3),
        };

        GoldParticle(double w, double h, Random rng) {
            x       = rng.nextDouble() * w;
            y       = rng.nextDouble() * h;
            size    = 1.5 + rng.nextDouble() * 3.0;
            speed   = 0.3 + rng.nextDouble() * 0.6;
            opacity = 0.2 + rng.nextDouble() * 0.5;
            color   = COLORS[rng.nextInt(COLORS.length)];
        }

        void update(double h) {
            y -= speed;
            if (y < -10) {
                y       = h + 5;
                x       = new Random().nextDouble() * 420;
                opacity = 0.2 + new Random().nextDouble() * 0.5;
            }
        }
    }
}