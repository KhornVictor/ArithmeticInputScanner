package com.compiler.gui.ui;

import com.compiler.model.LexicalError;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Component displaying a list of lexical error cards with line/column information
 * and an interactive "Jump to Error" action button.
 */
public class ErrorAlertBox extends VBox {

    private BiConsumer<Integer, Integer> jumpHandler;

    public ErrorAlertBox() {
        super(8);
        setVisible(false);
        setManaged(false);
    }

    public void setOnJumpToError(BiConsumer<Integer, Integer> jumpHandler) {
        this.jumpHandler = jumpHandler;
    }

    public void updateErrors(List<LexicalError> errors) {
        getChildren().clear();

        if (errors == null || errors.isEmpty()) {
            setVisible(false);
            setManaged(false);
            return;
        }

        setVisible(true);
        setManaged(true);

        for (LexicalError err : errors) {
            HBox alert = new HBox(12);
            alert.getStyleClass().add("alert-card-error");
            alert.setAlignment(Pos.CENTER_LEFT);

            Label icon = new Label("❌");
            icon.setStyle("-fx-font-size: 14px;");

            VBox info = new VBox(2);
            Label msg = new Label("Lexical Error: Unexpected character '" + err.invalidLexeme() + "'");
            msg.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #dc2626;");
            Label pos = new Label("At Line " + err.line() + ", Column " + err.column() + " • Scanning continued cleanly");
            pos.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f1d1d;");
            info.getChildren().addAll(msg, pos);

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            Button jumpBtn = new Button("Jump to Error");
            jumpBtn.getStyleClass().add("btn-pill");
            jumpBtn.setOnAction(e -> {
                if (jumpHandler != null) {
                    jumpHandler.accept(err.line(), err.column());
                }
            });

            alert.getChildren().addAll(icon, info, sp, jumpBtn);
            getChildren().add(alert);
        }
    }
}
