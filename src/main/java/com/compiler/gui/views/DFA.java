package com.compiler.gui.views;

import com.compiler.gui.DfaCanvas;
import com.compiler.gui.ui.RuleCard;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * View 3: Visual DFA state machine view with live input tracer
 * and cards explaining competing rules.
 */
public class DFA extends VBox {

    private final DfaCanvas dfaCanvas;
    private final TextField dfaSimulatorField;

    public DFA() {
        super(16);
        getStyleClass().add("card");

        HBox header = new HBox(12);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("DFA State Machine: DECIMAL = (0 | [1-9][0-9]*)\\.[0-9]+");
        title.getStyleClass().add("card-title");
        Label sub = new Label("Interactive visual state-transition path tracer");
        sub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox simBox = new HBox(8);
        simBox.setAlignment(Pos.CENTER_LEFT);
        Label tracerLabel = new Label("Trace Number:");
        tracerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        dfaSimulatorField = new TextField("12.5");
        dfaSimulatorField.getStyleClass().add("search-field");
        dfaSimulatorField.setPrefWidth(120);

        simBox.getChildren().addAll(tracerLabel, dfaSimulatorField);
        header.getChildren().addAll(titleBox, spacer, simBox);

        dfaCanvas = new DfaCanvas(720, 200);
        dfaCanvas.setDarkMode(false);

        dfaSimulatorField.textProperty().addListener((obs, oldV, newV) -> {
            dfaCanvas.setSimulatedInput(newV);
        });

        // Competing Rules Cards
        HBox rulesGrid = new HBox(16);
        RuleCard c1 = new RuleCard("Rule 1: Relational '>=' vs '>'",
                "Conflict: Input '>=' matches '>' (len 1) and '>=' (len 2).\nResolution: JFlex Longest Match chooses '>=' (OP_GTE).");
        RuleCard c2 = new RuleCard("Rule 2: Decimal '12.5' vs '12'",
                "Conflict: Input '12.5' matches '12' (Integer) and '12.5' (Decimal).\nResolution: Longest Match chooses 4 characters (DECIMAL).");
        RuleCard c3 = new RuleCard("Rule 3: Comment '//' vs Division '/'",
                "Conflict: Input '//' matches '/' (Division) or Comment line.\nResolution: Comment rule matches to newline and cleanly skips.");

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        rulesGrid.getChildren().addAll(c1, c2, c3);

        getChildren().addAll(header, dfaCanvas, rulesGrid);
    }

    public DfaCanvas getDfaCanvas() {
        return dfaCanvas;
    }

    public void setSimulatedInput(String input) {
        dfaSimulatorField.setText(input != null ? input : "");
        dfaCanvas.setSimulatedInput(input);
    }

    public String getSimulatedInput() {
        return dfaSimulatorField.getText();
    }
}
