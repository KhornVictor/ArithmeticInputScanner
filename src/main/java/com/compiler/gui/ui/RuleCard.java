package com.compiler.gui.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Reusable card displaying an explanation of a competing rule conflict and resolution.
 */
public class RuleCard extends VBox {

    public RuleCard(String title, String body) {
        super(6);
        setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 10px; -fx-padding: 14px;");
        
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        
        Label b = new Label(body);
        b.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569; -fx-line-spacing: 2px;");
        b.setWrapText(true);
        
        getChildren().addAll(t, b);
    }
}
