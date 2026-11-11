package com.compiler.gui.layouts;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Top-level layout container organizing the top navigation bar and
 * the central view stack.
 */
public class MainLayout extends BorderPane {

    private final TopNavBar topNavBar;
    private final StackPane contentStack;

    public MainLayout(TopNavBar topNavBar, Node... views) {
        getStyleClass().add("root");
        this.topNavBar = topNavBar;
        setTop(topNavBar);

        VBox centerContainer = new VBox(20);
        centerContainer.setPadding(new Insets(24, 28, 24, 28));

        contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);
        contentStack.getChildren().addAll(views);

        centerContainer.getChildren().add(contentStack);
        setCenter(centerContainer);
    }

    public void showView(Node viewToShow) {
        for (Node child : contentStack.getChildren()) {
            child.setVisible(child == viewToShow);
        }
    }

    public TopNavBar getTopNavBar() {
        return topNavBar;
    }

    public StackPane getContentStack() {
        return contentStack;
    }
}
