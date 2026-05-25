package it.unicam.cs.mpgc.rpg126224.view;

import it.unicam.cs.mpgc.rpg126224.controller.GameController;
import it.unicam.cs.mpgc.rpg126224.model.HeroClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

/**
 * Character creation screen shown at the start of a new game.
 */
public class CharacterCreationView extends VBox {

    private final GameController controller;
    private final Runnable onGameStarted;
    private final TextField nameField = new TextField();
    private final ToggleGroup classGroup = new ToggleGroup();

    public CharacterCreationView(GameController controller, Runnable onGameStarted) {
        this.controller = controller;
        this.onGameStarted = onGameStarted;
        buildUI();
    }

    private void buildUI() {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("DUNGEON PROTOCOL");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#e94560"));

        Label subtitle = new Label("Create Your Hero");
        subtitle.setFont(Font.font("Monospace", 16));
        subtitle.setTextFill(Color.web("#a0a0c0"));

        Label nameLabel = styledLabel("Hero Name:");
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-background-color: #16213e; -fx-text-fill: #e0e0ff; " +
                "-fx-border-color: #e94560; -fx-border-radius: 4; -fx-font-family: Monospace;");
        nameField.setPromptText("Enter your name...");

        Label classLabel = styledLabel("Choose Class:");
        HBox classBox = new HBox(15);
        classBox.setAlignment(Pos.CENTER);

        for (HeroClass hc : HeroClass.values()) {
            RadioButton rb = new RadioButton(hc.name());
            rb.setToggleGroup(classGroup);
            rb.setUserData(hc);
            rb.setStyle("-fx-text-fill: #c0c0e0; -fx-font-family: Monospace; -fx-font-size: 13;");
            if (hc == HeroClass.WARRIOR) rb.setSelected(true);
            classBox.getChildren().add(rb);
        }

        Label classInfo = new Label(getClassDescription(HeroClass.WARRIOR));
        classInfo.setFont(Font.font("Monospace", 12));
        classInfo.setTextFill(Color.web("#80c0ff"));
        classInfo.setWrapText(true);
        classInfo.setMaxWidth(400);
        classInfo.setAlignment(Pos.CENTER);

        classGroup.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw != null) classInfo.setText(getClassDescription((HeroClass) nw.getUserData()));
        });

        Button startBtn = new Button("ENTER THE DUNGEON");
        startBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; " +
                "-fx-font-family: Monospace; -fx-font-size: 14; -fx-padding: 10 25; " +
                "-fx-background-radius: 4;");
        startBtn.setOnAction(e -> handleStart());

        getChildren().addAll(title, subtitle, new Separator(),
                nameLabel, nameField, classLabel, classBox, classInfo,
                new Separator(), startBtn);
    }

    private void handleStart() {
        String name = nameField.getText().trim();
        if (name.isBlank()) {
            nameField.setStyle(nameField.getStyle() + "-fx-border-color: red;");
            return;
        }
        Toggle selected = classGroup.getSelectedToggle();
        HeroClass heroClass = selected != null
                ? (HeroClass) selected.getUserData()
                : HeroClass.WARRIOR;
        controller.startNewGame(name, heroClass);
        onGameStarted.run();
    }

    private Label styledLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        l.setTextFill(Color.web("#c0c0ff"));
        return l;
    }

    private String getClassDescription(HeroClass hc) {
        return switch (hc) {
            case WARRIOR -> "Warrior - High HP & defense. Power Strike: 150% ATK damage.";
            case MAGE    -> "Mage - High magic power. Magic Blast: magic x2 damage.";
            case ARCHER  -> "Archer - Balanced stats. Precise Shot: ATK + magic damage.";
        };
    }
}