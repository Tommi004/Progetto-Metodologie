package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.model.Item;
import it.unicam.cs.mpgc.rpg126224.model.RunStats;
import it.unicam.cs.mpgc.rpg126224.model.TrapType;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Factory for modal game dialogs (trap, level advance, game over, victory).
 *
 * <p>Extracted from GameView to respect the Single Responsibility Principle:
 * GameView handles the main game screen; this class handles dialog creation only.</p>
 *
 * <p>The {@link #showGameOver(RunStats, Runnable)} overload shows a detailed
 * post-run summary (enemies defeated, damage dealt/taken, floors cleared and
 * cause of death) instead of the generic one-liner used previously.</p>
 */
public class ViewGameDialogFactory {

    private static final String FONT_MONO = "Monospace";

    private ViewGameDialogFactory() {}

    // ------------------------------------------------------------------
    // Public dialog methods
    // ------------------------------------------------------------------

    /**
     * Shows a pause/menu dialog when the player clicks the Menu button.
     *
     * <p>Three options are presented:</p>
     * <ul>
     *   <li><b>Save &amp; Exit</b> — saves the game then returns to the main menu</li>
     *   <li><b>Exit without saving</b> — returns to the main menu immediately</li>
     *   <li><b>Cancel</b> — closes the dialog and resumes the game</li>
     * </ul>
     *
     * @param onSaveAndExit     callback that saves and then returns to menu
     * @param onExitWithoutSave callback that returns to menu without saving
     */
    public static void showPauseMenu(Runnable onSaveAndExit, Runnable onExitWithoutSave) {
        Stage dialog = buildBaseDialog("Paused", 380, 320);

        // ── Header ──────────────────────────────────────────────────────
        Label title = titleLabel("— PAUSED —", "#9090e0");
        title.setEffect(glow("#6060c0", 14));

        Label question = new Label("What would you like to do?");
        question.setFont(Font.font(FONT_MONO, 12));
        question.setTextFill(Color.web("#8080a0"));

        // ── Buttons ─────────────────────────────────────────────────────
        Button saveExitBtn = wideButton("💾   Save & Exit",        "#0d2a40", "#60aaff", "#1a4a70");
        Button exitBtn     = wideButton("↩   Exit without saving", "#2a0d0d", "#ff7070", "#4a1a1a");
        Button cancelBtn   = wideButton("✖   Cancel",              "#141424", "#7070a0", "#1e1e38");

        saveExitBtn.setOnAction(e -> { dialog.close(); onSaveAndExit.run(); });
        exitBtn.setOnAction(e -> { dialog.close(); onExitWithoutSave.run(); });
        cancelBtn.setOnAction(e -> dialog.close());

        VBox btnBox = new VBox(10, saveExitBtn, exitBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER);

        showWithFade(dialog, 380, 320, "#0d0d1f",
                title, question, btnBox);
    }

    /**
     * Shows a modal "Credits / About" dialog from the main menu.
     *
     * <p>Displays: game title, developer info, course details,
     * academic year and the technology stack used.</p>
     */
    public static void showCredits() {
        Stage dialog = buildBaseDialog("Credits", 400, 420);

        Label title = titleLabel("LEVEL UP!", "#e94560");
        title.setEffect(glow("#e94560", 18));

        Label subtitle = new Label("A turn-based dungeon RPG");
        subtitle.setFont(Font.font(FONT_MONO, 12));
        subtitle.setTextFill(Color.web("#ffd700"));

        Separator sep1 = new Separator();
        sep1.setMaxWidth(300);
        sep1.setStyle("-fx-background-color: #3a2a00;");

        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setMaxWidth(320);
        addCreditRow(infoBox, "Developer",  "Tommaso Leonardi",     "#e0e0ff");
        addCreditRow(infoBox, "Matricola",  "126224",               "#e0e0ff");
        addCreditRow(infoBox, "Course",     "Metodologie di Prog.", "#c0c0e0");
        addCreditRow(infoBox, "University", "UNICAM — Camerino",    "#c0c0e0");
        addCreditRow(infoBox, "A.Y.",       "2025 / 2026",          "#c0c0e0");

        Separator sep2 = new Separator();
        sep2.setMaxWidth(300);
        sep2.setStyle("-fx-background-color: #2a2a3a;");

        Label techHeader = new Label("Built with");
        techHeader.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 11));
        techHeader.setTextFill(Color.web("#808090"));

        Label techLabel = new Label("Java 25  ·  JavaFX 24  ·  Gradle 9  ·  JUnit 5");
        techLabel.setFont(Font.font(FONT_MONO, 11));
        techLabel.setTextFill(Color.web("#606070"));

        Button closeBtn = confirmButton("CLOSE", "#1e1e2e", "#a0a0c0");
        closeBtn.setOnAction(e -> dialog.close());

        showWithFade(dialog, 400, 420, "#0a0a14",
                title, subtitle, sep1, infoBox, sep2, techHeader, techLabel, closeBtn);
    }

    /**
     * Shows a dialog asking the player which offensive weapon to replace
     * when the 2-slot limit is reached.
     *
     * <p>The player can choose one of the two current weapons to replace,
     * or skip the pickup entirely.</p>
     *
     * @param newItem   the new weapon found
     * @param current   the two weapons currently equipped
     * @param onChoice  callback receiving the id of the weapon to replace,
     *                  or {@code null} if the player chose to skip
     */
    public static void showWeaponSwapDialog(Item newItem, List<Item> current,
                                            java.util.function.Consumer<String> onChoice) {
        Stage dialog = buildBaseDialog("New Weapon Found!", 380, 340);

        Label title = titleLabel("⚔  WEAPON FOUND", "#ffd700");
        title.setEffect(glow("#ffd700", 14));

        Label newLbl = new Label(newItem.getRarity().getDisplayName() + " " + newItem.getName());
        newLbl.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 13));
        newLbl.setTextFill(Color.web(newItem.getRarity().getColor()));

        Label question = new Label("Your weapon slots are full. Replace one?");
        question.setFont(Font.font(FONT_MONO, 11));
        question.setTextFill(Color.web("#a0a0c0"));

        // Buttons for each current weapon
        VBox btnBox = new VBox(8);
        btnBox.setAlignment(Pos.CENTER);
        for (Item w : current) {
            String label = "Replace  " + w.getRarity().getDisplayName() + " " + w.getName();
            Button btn = wideButton(label, "#1a1a2a", Color.web(w.getRarity().getColor()).toString().replace("0x","#").substring(0,7), "#2a2a3a");
            btn.setOnAction(e -> { dialog.close(); onChoice.accept(w.getId()); });
            btnBox.getChildren().add(btn);
        }

        Button skipBtn = wideButton("✖  Keep current weapons", "#1a0a0a", "#ff6060", "#2a1010");
        skipBtn.setOnAction(e -> { dialog.close(); onChoice.accept(null); });
        btnBox.getChildren().add(skipBtn);

        showWithFade(dialog, 380, 340, "#0a0a14",
                title, newLbl, question, btnBox);
    }

    /**
     * Shows a modal dialog when the hero triggers a trap.
     *
     * @param trap the trap that was triggered
     */
    public static void showTrap(TrapType trap) {
        Stage dialog = buildBaseDialog("Trap!", 360, 240);

        Label icon   = iconLabel(trap.getEmoji());
        Label title  = titleLabel(trap.getDisplayName().toUpperCase(), "#ff4444");
        title.setEffect(glow("#ff4444", 18));

        Label effect = bodyLabel(trap.getEffectDescription());
        effect.setTextFill(Color.web("#ff8888"));

        Button okBtn = confirmButton("CONTINUE", "#4a0000", "#ff8888");
        okBtn.setOnAction(e -> dialog.close());

        showWithFade(dialog, 360, 240, "#0a0000",
                icon, title, effect, okBtn);
    }

    /**
     * Shows the "Floor Cleared" dialog when the hero advances to the next level.
     *
     * @param newLevel the level the hero is advancing to
     */
    public static void showLevelAdvance(int newLevel) {
        Stage dialog = buildBaseDialog("Floor Cleared!", 380, 280);

        Label icon  = iconLabel("⚔");
        Label title = titleLabel("FLOOR CLEARED!", "#ffd700");
        title.setEffect(glow("#ffd700", 20));

        Label msg = bodyLabel("You defeated the boss!");
        Label sub = subLabel("Descending to floor " + newLevel + " of 5...", "#8080b0");

        Button okBtn = confirmButton("CONTINUE", "#8a6a00", "#ffd700");
        okBtn.setOnAction(e -> dialog.close());

        showWithFade(dialog, 380, 280, "#0a0010",
                icon, title, msg, sub, okBtn);
    }

    /**
     * Shows a detailed "Game Over" dialog with run statistics.
     *
     * <p>Displays: cause of death, enemies defeated, damage dealt, damage taken
     * and floors cleared — giving the player a full picture of their run.</p>
     *
     * @param stats          the {@link RunStats} snapshot collected during the run
     * @param onReturnToMenu callback invoked when the player dismisses the dialog
     */
    public static void showGameOver(RunStats stats, Runnable onReturnToMenu) {
        Stage dialog = buildBaseDialog("Game Over", 420, 460);

        // ── Header ──────────────────────────────────────────────────────
        Label icon  = iconLabel("💀");
        Label title = titleLabel("YOU DIED", "#e94560");
        title.setEffect(glow("#e94560", 20));

        Label causeLabel = buildCauseOfDeathLabel(stats.causeOfDeath());

        // ── Separator ───────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setMaxWidth(300);
        sep.setStyle("-fx-background-color: #5a0000;");

        // ── Stats block ─────────────────────────────────────────────────
        Label statsHeader = new Label("─── RUN SUMMARY ───");
        statsHeader.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        statsHeader.setTextFill(Color.web("#cc4444"));

        VBox statsBox = new VBox(6);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setMaxWidth(300);

        addStatRow(statsBox, "⚔  Enemies defeated",
                String.valueOf(stats.enemiesDefeated()), "#ff8888");
        addStatRow(statsBox, "🗡  Damage dealt",
                String.valueOf(stats.totalDamageDealt()), "#ffc0c0");
        addStatRow(statsBox, "🛡  Damage taken",
                String.valueOf(stats.totalDamageTaken()), "#ff6666");
        addStatRow(statsBox, "🏰  Floors cleared",
                stats.dungeonsCleared() + " / 5", "#cc8888");
        addStatRow(statsBox, "⭐  Final level",
                String.valueOf(stats.finalLevel()), "#ffaaaa");

        // ── Action button ────────────────────────────────────────────────
        Button okBtn = confirmButton("RETURN TO MENU", "#6a0000", "#ff8080");
        okBtn.setOnAction(e -> { dialog.close(); onReturnToMenu.run(); });

        showWithFade(dialog, 420, 460, "#0a0000",
                icon, title, causeLabel, sep, statsHeader, statsBox, okBtn);
    }

    /**
     * Shows the "Victory" dialog when the hero completes all five dungeon floors.
     *
     * <p>Displays a full run-statistics summary alongside the congratulations
     * message, so the player can appreciate their final performance.</p>
     *
     * @param stats          the {@link RunStats} snapshot collected during the run
     * @param onReturnToMenu callback invoked when the player confirms
     */
    public static void showVictory(RunStats stats, Runnable onReturnToMenu) {
        Stage dialog = buildBaseDialog("Victory!", 420, 480);

        // ── Header ──────────────────────────────────────────────────────
        Label icon  = iconLabel("🏆");
        Label title = titleLabel("VICTORY!", "#ffd700");
        title.setEffect(glow("#ffd700", 24));

        Label sub = subLabel("The Demon Soul is vanquished. The dungeon is yours!", "#c8c860");

        // ── Separator ───────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setMaxWidth(300);
        sep.setStyle("-fx-background-color: #5a4a00;");

        // ── Stats block ─────────────────────────────────────────────────
        Label statsHeader = new Label("─── RUN SUMMARY ───");
        statsHeader.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        statsHeader.setTextFill(Color.web("#bba020"));

        VBox statsBox = new VBox(6);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setMaxWidth(300);

        addStatRow(statsBox, "⚔  Enemies defeated",
                String.valueOf(stats.enemiesDefeated()), "#ffd700");
        addStatRow(statsBox, "🗡  Damage dealt",
                String.valueOf(stats.totalDamageDealt()), "#ffe080");
        addStatRow(statsBox, "🛡  Damage taken",
                String.valueOf(stats.totalDamageTaken()), "#ffcc60");
        addStatRow(statsBox, "🏰  Floors cleared",
                stats.dungeonsCleared() + " / 5", "#ffd700");
        addStatRow(statsBox, "⭐  Final level",
                String.valueOf(stats.finalLevel()), "#fff0a0");

        // ── Action button ────────────────────────────────────────────────
        Button okBtn = confirmButton("RETURN TO MENU", "#8a6a00", "#ffd700");
        okBtn.setOnAction(e -> { dialog.close(); onReturnToMenu.run(); });

        showWithFade(dialog, 420, 480, "#0a0a00",
                icon, title, sub, sep, statsHeader, statsBox, okBtn);
    }

    // ------------------------------------------------------------------
    // Private helpers — dialog construction
    // ------------------------------------------------------------------

    private static Stage buildBaseDialog(String title, int w, int h) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    /**
     * Builds the root VBox, wraps it in a Scene, applies a fade-in and
     * shows the dialog modally.
     */
    private static void showWithFade(Stage dialog, int w, int h,
                                     String bg, javafx.scene.Node... nodes) {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + bg + ";");
        root.getChildren().addAll(nodes);

        FadeTransition ft = new FadeTransition(Duration.millis(400), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        dialog.setScene(new Scene(root, w, h));
        ft.play();
        dialog.showAndWait();
    }

    /**
     * Builds the italicised cause-of-death label.
     * Falls back to a generic message when {@code causeOfDeath} is null
     * (e.g. the dialog is called unexpectedly after a victory).
     */
    private static Label buildCauseOfDeathLabel(String causeOfDeath) {
        String text = (causeOfDeath != null)
                ? causeOfDeath
                : "The dungeon claimed another soul...";
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, FontWeight.NORMAL, 12));
        l.setTextFill(Color.web("#c06060"));
        l.setWrapText(true);
        l.setMaxWidth(340);
        l.setStyle("-fx-font-style: italic;");
        return l;
    }

    /**
     * Adds a single two-column stat row to {@code parent}.
     *
     * @param parent    the VBox to append to
     * @param labelText left-side label (stat name with emoji)
     * @param value     right-side value string
     * @param color     hex color for the value text
     */
    private static void addCreditRow(VBox parent, String key, String value, String color) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLbl = new Label(String.format("%-12s", key));
        keyLbl.setFont(Font.font(FONT_MONO, 12));
        keyLbl.setTextFill(Color.web("#505060"));

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        valLbl.setTextFill(Color.web(color));

        row.getChildren().addAll(keyLbl, valLbl);
        parent.getChildren().add(row);
    }

    private static void addStatRow(VBox parent,
                                   String labelText, String value, String color) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(0);

        Label name = new Label(String.format("%-30s", labelText));
        name.setFont(Font.font(FONT_MONO, 12));
        name.setTextFill(Color.web("#c0c0c0"));

        Label val = new Label(value);
        val.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 12));
        val.setTextFill(Color.web(color));

        row.getChildren().addAll(name, val);
        parent.getChildren().add(row);
    }

    // ------------------------------------------------------------------
    // Private helpers — reusable UI primitives
    // ------------------------------------------------------------------

    private static Label iconLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(48));
        return l;
    }

    private static Label titleLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, FontWeight.BOLD, 28));
        l.setTextFill(Color.web(color));
        return l;
    }

    private static Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, 13));
        l.setTextFill(Color.web("#e0e0ff"));
        return l;
    }

    private static Label subLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font(FONT_MONO, 11));
        l.setTextFill(Color.web(color));
        l.setWrapText(true);
        l.setMaxWidth(340);
        return l;
    }

    private static Button confirmButton(String text, String bg, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-family: " + FONT_MONO + "; " +
                "-fx-font-size: 13; " +
                "-fx-padding: 8 30; " +
                "-fx-background-radius: 4;"
        );
        return btn;
    }

    /** Like confirmButton but fixed width and with a hover border effect. */
    private static Button wideButton(String text, String bg, String textColor, String hoverBg) {
        Button btn = new Button(text);
        String base =
                "-fx-background-color: " + bg + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-family: " + FONT_MONO + "; " +
                "-fx-font-size: 13; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 6; " +
                "-fx-pref-width: 300; " +
                "-fx-cursor: hand;";
        String hover =
                "-fx-background-color: " + hoverBg + "; " +
                "-fx-text-fill: " + textColor + "; " +
                "-fx-font-family: " + FONT_MONO + "; " +
                "-fx-font-size: 13; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 6; " +
                "-fx-pref-width: 300; " +
                "-fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private static DropShadow glow(String color, double radius) {
        return new DropShadow(radius, Color.web(color));
    }
}