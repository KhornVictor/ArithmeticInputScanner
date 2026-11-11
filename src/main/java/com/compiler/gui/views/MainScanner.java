package com.compiler.gui.views;

import java.util.function.BiConsumer;

import com.compiler.gui.ui.ErrorAlertBox;
import com.compiler.gui.ui.TokenTable;
import com.compiler.model.ScanResult;
import com.compiler.service.ScannerService;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * View 1: Main clean scanner consisting of the source code editor on the left
 * and the recognized tokens table + error notifications on the right.
 */
public class MainScanner extends HBox {

    private final TextArea codeEditor;
    private final TokenTable tokenTable;
    private final ErrorAlertBox errorNotificationBox;
    private final ScannerService scannerService;
    private BiConsumer<ScanResult, Boolean> onScanCompleted;

    public MainScanner(ScannerService scannerService) {
        super(20);
        this.scannerService = scannerService;

        // Initialize code editor early for closures
        codeEditor = new TextArea();
        codeEditor.getStyleClass().add("code-editor");
        codeEditor.setWrapText(false);
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        // Keystroke typing triggers passive scan (isExplicit = false)
        codeEditor.textProperty().addListener((observable, oldValue, newValue) -> performScan(false));

        // --- Left Card: Source Code Editor ---
        VBox editorCard = new VBox(14);
        editorCard.getStyleClass().add("card");
        HBox.setHgrow(editorCard, Priority.ALWAYS);
        editorCard.setPrefWidth(540);

        HBox editorHeader = new HBox(10);
        editorHeader.getStyleClass().add("card-header");
        editorHeader.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label cardTitle = new Label("Source Expression");
        cardTitle.getStyleClass().add("card-title");
        titleBox.getChildren().addAll(cardTitle);

        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);

        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> {
            codeEditor.clear();
            performScan(true);
        });

        Button scanBtn = new Button("Scan Expression");
        scanBtn.getStyleClass().add("btn-primary");
        scanBtn.setOnAction(e -> performScan(true));

        editorHeader.getChildren().addAll(titleBox, sp1, clearBtn, scanBtn);
        editorCard.getChildren().addAll(editorHeader, codeEditor);

        // --- Right Card: Token Stream Table ---
        VBox tableCard = new VBox(14);
        tableCard.getStyleClass().add("card");
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        tableCard.setPrefWidth(620);

        HBox tableHeader = new HBox(12);
        tableHeader.getStyleClass().add("card-header");
        tableHeader.setAlignment(Pos.CENTER_LEFT);

        VBox tableTitleBox = new VBox(2);
        Label streamTitle = new Label("Recognized Tokens");
        tableTitleBox.getChildren().addAll(streamTitle);

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Filter tokens...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(160);

        tableHeader.getChildren().addAll(tableTitleBox, sp2, searchField);

        tokenTable = new TokenTable();
        VBox.setVgrow(tokenTable, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldV, query) -> tokenTable.filter(query));

        errorNotificationBox = new ErrorAlertBox();
        errorNotificationBox.setOnJumpToError(this::highlightPosition);

        tableCard.getChildren().addAll(tableHeader, tokenTable, errorNotificationBox);

        getChildren().addAll(editorCard, tableCard);
    }

    public void setOnScanCompleted(BiConsumer<ScanResult, Boolean> listener) {
        this.onScanCompleted = listener;
    }

    public void performScan() {
        performScan(true);
    }

    public void performScan(boolean isExplicit) {
        String text = codeEditor.getText();
        ScanResult result = scannerService.scan(text);

        tokenTable.setTokens(result.getTokens());
        errorNotificationBox.updateErrors(result.getErrors());

        if (onScanCompleted != null) {
            onScanCompleted.accept(result, isExplicit);
        }
    }

    public void setCode(String code) {
        codeEditor.setText(code != null ? code : "");
    }

    public String getCode() {
        return codeEditor.getText();
    }

    public TextArea getCodeEditor() {
        return codeEditor;
    }

    public TokenTable getTokenTable() {
        return tokenTable;
    }

    public ErrorAlertBox getErrorNotificationBox() {
        return errorNotificationBox;
    }

    public void highlightPosition(int targetLine, int targetCol) {
        String text = codeEditor.getText();
        String[] lines = text.split("\r\n|\r|\n", -1);
        int offset = 0;
        for (int i = 0; i < targetLine - 1 && i < lines.length; i++) {
            offset += lines[i].length() + 1;
        }
        offset += Math.max(0, targetCol - 1);
        if (offset < text.length()) {
            codeEditor.requestFocus();
            codeEditor.selectRange(offset, offset + 1);
        }
    }
}
