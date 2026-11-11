package com.compiler.gui.layouts;

import com.compiler.model.TestCase;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.function.Consumer;

/**
 * Top navigation bar containing view selection pills, preset examples dropdown,
 * and the "Open File" action.
 */
public class TopNavBar extends HBox {

    public enum NavTab {
        SCANNER,
        TESTS,
        DFA
    }

    private final Button navScannerBtn;
    private final Button navTestsBtn;
    private final Button navDfaBtn;
    private final ComboBox<TestCase> testSelector;
    private final Button openBtn;

    private Consumer<NavTab> onTabSelected;
    private Consumer<TestCase> onPresetSelected;
    private Runnable onOpenFileRequested;

    public TopNavBar(List<TestCase> presetTestCases) {
        super(16);
        getStyleClass().add("top-nav");
        setAlignment(Pos.CENTER_LEFT);

        // Center Nav Pills
        HBox navPills = new HBox(8);
        navPills.setAlignment(Pos.CENTER);

        navScannerBtn = new Button("Scanner");
        navScannerBtn.setOnAction(e -> selectTab(NavTab.SCANNER));

        navTestsBtn = new Button("Test Suite (16)");
        navTestsBtn.setOnAction(e -> selectTab(NavTab.TESTS));

        navDfaBtn = new Button("DFA Automaton");
        navDfaBtn.setOnAction(e -> selectTab(NavTab.DFA));

        navPills.getChildren().addAll(navScannerBtn, navTestsBtn, navDfaBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right Actions: Presets & Open File Button
        testSelector = new ComboBox<>();
        testSelector.setPromptText("Preset Examples...");
        if (presetTestCases != null) {
            testSelector.getItems().addAll(presetTestCases);
        }
        testSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TestCase item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("[%s] %s", item.id(), item.title()));
            }
        });
        testSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TestCase item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Preset Examples..." : String.format("[%s] %s", item.id(), item.title()));
            }
        });
        testSelector.setOnAction(e -> {
            TestCase selected = testSelector.getValue();
            if (selected != null && onPresetSelected != null) {
                onPresetSelected.accept(selected);
            }
        });

        openBtn = new Button("Open File");
        openBtn.getStyleClass().add("btn-secondary");
        openBtn.setOnAction(e -> {
            if (onOpenFileRequested != null) {
                onOpenFileRequested.run();
            }
        });

        getChildren().addAll(new Separator(), navPills, spacer, testSelector, openBtn);
    }

    public void selectTab(NavTab tab) {
        setActiveTabButton(tab);
        if (onTabSelected != null) {
            onTabSelected.accept(tab);
        }
    }

    public void setActiveTabButton(NavTab tab) {
        navScannerBtn.getStyleClass().removeAll("nav-pill-active", "nav-pill");
        navTestsBtn.getStyleClass().removeAll("nav-pill-active", "nav-pill");
        navDfaBtn.getStyleClass().removeAll("nav-pill-active", "nav-pill");

        navScannerBtn.getStyleClass().add(tab == NavTab.SCANNER ? "nav-pill-active" : "nav-pill");
        navTestsBtn.getStyleClass().add(tab == NavTab.TESTS ? "nav-pill-active" : "nav-pill");
        navDfaBtn.getStyleClass().add(tab == NavTab.DFA ? "nav-pill-active" : "nav-pill");
    }

    public void setOnTabSelected(Consumer<NavTab> handler) {
        this.onTabSelected = handler;
    }

    public void setOnPresetSelected(Consumer<TestCase> handler) {
        this.onPresetSelected = handler;
    }

    public void setOnOpenFileRequested(Runnable handler) {
        this.onOpenFileRequested = handler;
    }

    public ComboBox<TestCase> getTestSelector() {
        return testSelector;
    }
}
