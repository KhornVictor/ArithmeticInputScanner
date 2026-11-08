package com.compiler.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.compiler.model.LexicalError;
import com.compiler.model.ScanResult;
import com.compiler.model.TestCase;
import com.compiler.model.TestReport;
import com.compiler.model.Token;
import com.compiler.service.ScannerService;
import com.compiler.service.TestRunnerService;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ScannerGuiApp extends Application {

    private final ScannerService scannerService = new ScannerService();
    private final TestRunnerService testRunnerService = new TestRunnerService();

    // Data Models
    private final ObservableList<Token> tokenMasterData = FXCollections.observableArrayList();
    private FilteredList<Token> filteredTokens;
    private final ObservableList<TestReport> testReportsData = FXCollections.observableArrayList();

    // Metric Stat Labels
    private Label statTokensValue;
    private Label statErrorsValue;
    private Label statErrorsTag;
    private Label statDurationValue;
    private Label statLinesValue;

    // View Components
    private TextArea codeEditor;
    private TableView<Token> tokenTable;
    private VBox errorNotificationBox;

    // View Switching
    private StackPane contentStack;
    private Node scannerView;
    private Node testSuiteView;
    private Node dfaView;

    private Button navScannerBtn;
    private Button navTestsBtn;
    private Button navDfaBtn;

    // DFA & Tests
    private DfaCanvas dfaCanvas;
    private TextField dfaSimulatorField;
    private TableView<TestReport> testTableView;
    private Label testSummaryBadge;

    private Stage primaryStage;

    public static void launchApp(String[] args) {
        Application.launch(ScannerGuiApp.class, args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Arithmetic Scanner ");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Top Navigation Bar
        root.setTop(createTopNavBar());

        // Center Content Area: Stat Cards + Stack of Views
        VBox centerContainer = new VBox(20);
        centerContainer.setPadding(new Insets(24, 28, 24, 28));


        // 2. Main Views (Scanner, Tests, DFA)
        contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        scannerView = createScannerView();
        testSuiteView = createTestSuiteView();
        dfaView = createDfaView();

        contentStack.getChildren().addAll(scannerView, testSuiteView, dfaView);
        showView(scannerView, navScannerBtn);

        centerContainer.getChildren().add(contentStack);
        root.setCenter(centerContainer);

        Scene scene = new Scene(root, 1220, 800);
        try {
            var css = getClass().getResource("/styles/app.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}

        // Keyboard Shortcuts: F5 or Ctrl+Enter to Scan
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), this::performScan);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), this::performScan);

        stage.setScene(scene);
        stage.show();

        // Initial default input
        codeEditor.setText("// Sample Arithmetic Expression\ntotal = 12.5 + qty * 2;\nflag = count >= 10;\ninvalid_test = 50 $ 4;");
        performScan();
    }

    // =========================================================================
    // Top Navigation Bar
    // =========================================================================
    private Node createTopNavBar() {
        HBox bar = new HBox(16);
        bar.getStyleClass().add("top-nav");
        bar.setAlignment(Pos.CENTER_LEFT);

        // Brand Icon & Title

        // Center Nav Pills
        HBox navPills = new HBox(8);
        navPills.setAlignment(Pos.CENTER);

        navScannerBtn = new Button("Scanner");
        navScannerBtn.setOnAction(e -> showView(scannerView, navScannerBtn));

        navTestsBtn = new Button("Test Suite (16)");
        navTestsBtn.setOnAction(e -> {
            showView(testSuiteView, navTestsBtn);
            if (testReportsData.isEmpty()) runAllAutomatedTests();
        });

        navDfaBtn = new Button("DFA Automaton");
        navDfaBtn.setOnAction(e -> showView(dfaView, navDfaBtn));

        navPills.getChildren().addAll(navScannerBtn, navTestsBtn, navDfaBtn);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Right Actions: Presets & Scan Button
        ComboBox<TestCase> testSelector = new ComboBox<>();
        testSelector.setPromptText("Preset Examples...");
        testSelector.getItems().addAll(testRunnerService.getAllTestCases());
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
            if (selected != null) {
                showView(scannerView, navScannerBtn);
                codeEditor.setText(selected.input());
                performScan();
            }
        });

        Button openBtn = new Button("Open File");
        openBtn.getStyleClass().add("btn-secondary");
        openBtn.setOnAction(e -> openFile());

        Button scanBtn = new Button("▶  Scan Expression");
        scanBtn.getStyleClass().add("btn-primary");
        scanBtn.setOnAction(e -> performScan());

        bar.getChildren().addAll(new Separator(), navPills, spacer, testSelector, openBtn, scanBtn);
        return bar;
    }

    private void showView(Node viewToShow, Button activeButton) {
        scannerView.setVisible(viewToShow == scannerView);
        testSuiteView.setVisible(viewToShow == testSuiteView);
        dfaView.setVisible(viewToShow == dfaView);

        navScannerBtn.getStyleClass().removeAll("nav-pill-active", "nav-pill");
        navTestsBtn.getStyleClass().removeAll("nav-pill-active", "nav-pill");
        navDfaBtn.getStyleClass().removeAll("nav-pill-active", "nav-pill");

        navScannerBtn.getStyleClass().add(activeButton == navScannerBtn ? "nav-pill-active" : "nav-pill");
        navTestsBtn.getStyleClass().add(activeButton == navTestsBtn ? "nav-pill-active" : "nav-pill");
        navDfaBtn.getStyleClass().add(activeButton == navDfaBtn ? "nav-pill-active" : "nav-pill");
    }


    

    // =========================================================================
    // View 1: Main Clean Scanner (Editor + Tokens Table)
    // =========================================================================
    private Node createScannerView() {
        HBox grid = new HBox(20);
        VBox.setVgrow(grid, Priority.ALWAYS);

        // Left Card: Source Code Editor
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
            performScan();
        });

        editorHeader.getChildren().addAll(titleBox, sp1, clearBtn);

        codeEditor = new TextArea();
        codeEditor.getStyleClass().add("code-editor");
        codeEditor.setWrapText(false);
        VBox.setVgrow(codeEditor, Priority.ALWAYS);
        codeEditor.textProperty().addListener((observable, oldValue, newValue) -> performScan());

        
        editorCard.getChildren().addAll(editorHeader, codeEditor);

        // Right Card: Token Stream Table
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

        // Table
        tokenTable = new TableView<>();
        tokenTable.getStyleClass().add("table-view");
        VBox.setVgrow(tokenTable, Priority.ALWAYS);

        TableColumn<Token, String> colType = new TableColumn<>("TOKEN TYPE");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeName()));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Token token = getTableView().getItems().get(getIndex());
                    Label pill = new Label(item);
                    pill.getStyleClass().add("token-badge");
                    String color = token.getCategoryColor();
                    pill.setStyle(String.format("-fx-background-color: %s18; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-radius: 20px;", color, color, color));
                    setGraphic(pill);
                    setText(null);
                }
            }
        });
        colType.setPrefWidth(140);

        TableColumn<Token, String> colLexeme = new TableColumn<>("LEXEME");
        colLexeme.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLexeme()));
        colLexeme.setStyle("-fx-font-family: monospace; -fx-font-weight: 800; -fx-font-size: 13px;");
        colLexeme.setPrefWidth(130);

        TableColumn<Token, String> colPos = new TableColumn<>("LINE : COL");
        colPos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLine() + " : " + c.getValue().getColumn()));
        colPos.setPrefWidth(90);

        TableColumn<Token, String> colCategory = new TableColumn<>("CATEGORY");
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colCategory.setPrefWidth(120);

        tokenTable.getColumns().addAll(colType, colLexeme, colPos, colCategory);

        filteredTokens = new FilteredList<>(tokenMasterData, p -> true);
        searchField.textProperty().addListener((obs, oldV, query) -> {
            filteredTokens.setPredicate(token -> {
                if (query == null || query.isBlank()) return true;
                String lower = query.toLowerCase();
                return token.getTypeName().toLowerCase().contains(lower)
                        || token.getLexeme().toLowerCase().contains(lower)
                        || token.getCategory().toLowerCase().contains(lower);
            });
        });
        tokenTable.setItems(filteredTokens);

        // Error notification box (shown only if errors exist)
        errorNotificationBox = new VBox(8);
        errorNotificationBox.setVisible(false);
        errorNotificationBox.setManaged(false);

        tableCard.getChildren().addAll(tableHeader, tokenTable, errorNotificationBox);

        grid.getChildren().addAll(editorCard, tableCard);
        return grid;
    }

    // =========================================================================
    // View 2: Automated Test Suite (16 Cases)
    // =========================================================================
    private Node createTestSuiteView() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(12);
        header.getStyleClass().add("card-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label title = new Label("Automated Test Suite (16 Benchmark Tests)");
        title.getStyleClass().add("card-title");
        Label sub = new Label("6 Normal, 5 Boundary/Competing, 4 Invalid, 1 Bug Correction");
        sub.getStyleClass().add("card-subtitle");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        testSummaryBadge = new Label("Not yet run");
        testSummaryBadge.getStyleClass().add("stat-tag-blue");

        Button runAllBtn = new Button("▶  Run All Tests");
        runAllBtn.getStyleClass().add("btn-primary");
        runAllBtn.setOnAction(e -> runAllAutomatedTests());

        header.getChildren().addAll(titleBox, spacer, testSummaryBadge, runAllBtn);

        testTableView = new TableView<>();
        testTableView.getStyleClass().add("table-view");
        VBox.setVgrow(testTableView, Priority.ALWAYS);

        TableColumn<TestReport, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().testCase().id()));
        colId.setPrefWidth(60);

        TableColumn<TestReport, String> colCat = new TableColumn<>("CATEGORY");
        colCat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().testCase().category()));
        colCat.setPrefWidth(120);

        TableColumn<TestReport, String> colName = new TableColumn<>("TEST NAME");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().testCase().title()));
        colName.setPrefWidth(220);

        TableColumn<TestReport, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().passed() ? "PASS" : "FAIL"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label pill = new Label(item);
                    pill.getStyleClass().add("token-badge");
                    if ("PASS".equals(item)) {
                        pill.setStyle("-fx-background-color: #1eff00; -fx-text-fill: #ffffff; -fx-border-color: #bbf7d0; -fx-border-radius: 20px;");
                    } else {
                        pill.setStyle("-fx-background-color: #f50909; -fx-text-fill: #ffffff; -fx-border-color: #fecaca; -fx-border-radius: 20px;");
                    }
                    setGraphic(pill);
                    setText(null);
                }
            }
        });
        colStatus.setPrefWidth(90);

        TableColumn<TestReport, String> colMsg = new TableColumn<>("VERIFICATION");
        colMsg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().message()));
        colMsg.setPrefWidth(380);

        testTableView.getColumns().addAll(colId, colCat, colName, colStatus, colMsg);
        testTableView.setItems(testReportsData);

        // Double-click to load into scanner
        testTableView.setRowFactory(tv -> {
            TableRow<TestReport> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showView(scannerView, navScannerBtn);
                    codeEditor.setText(row.getItem().testCase().input());
                    performScan();
                }
            });
            return row;
        });

        card.getChildren().addAll(header, testTableView);
        return card;
    }

    // =========================================================================
    // View 3: DFA Automaton & Competing Rules
    // =========================================================================
    private Node createDfaView() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

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
        dfaSimulatorField.textProperty().addListener((obs, oldV, newV) -> {
            if (dfaCanvas != null) dfaCanvas.setSimulatedInput(newV);
        });
        simBox.getChildren().addAll(tracerLabel, dfaSimulatorField);

        header.getChildren().addAll(titleBox, spacer, simBox);

        dfaCanvas = new DfaCanvas(720, 200);
        dfaCanvas.setDarkMode(false);

        // Competing Rules Cards
        HBox rulesGrid = new HBox(16);
        VBox c1 = createMiniRuleCard("Rule 1: Relational '>=' vs '>'",
                "Conflict: Input '>=' matches '>' (len 1) and '>=' (len 2).\nResolution: JFlex Longest Match chooses '>=' (OP_GTE).");
        VBox c2 = createMiniRuleCard("Rule 2: Decimal '12.5' vs '12'",
                "Conflict: Input '12.5' matches '12' (Integer) and '12.5' (Decimal).\nResolution: Longest Match chooses 4 characters (DECIMAL).");
        VBox c3 = createMiniRuleCard("Rule 3: Comment '//' vs Division '/'",
                "Conflict: Input '//' matches '/' (Division) or Comment line.\nResolution: Comment rule matches to newline and cleanly skips.");

        HBox.setHgrow(c1, Priority.ALWAYS);
        HBox.setHgrow(c2, Priority.ALWAYS);
        HBox.setHgrow(c3, Priority.ALWAYS);
        rulesGrid.getChildren().addAll(c1, c2, c3);

        card.getChildren().addAll(header, dfaCanvas, rulesGrid);
        return card;
    }

    private VBox createMiniRuleCard(String title, String body) {
        VBox box = new VBox(6);
        box.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 10px; -fx-padding: 14px;");
        Label t = new Label(title);
        t.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #0f172a;");
        Label b = new Label(body);
        b.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569; -fx-line-spacing: 2px;");
        b.setWrapText(true);
        box.getChildren().addAll(t, b);
        return box;
    }

    // =========================================================================
    // Core Actions: Scan, Error Handling, File Operations
    // =========================================================================
    private void performScan() {
        String text = codeEditor.getText();
        ScanResult result = scannerService.scan(text);

        tokenMasterData.clear();
        tokenMasterData.addAll(result.getTokens());

        updateDashboardMetrics(result);
        updateErrorsNotification(result);
    }

    private void updateDashboardMetrics(ScanResult result) {
        if (statTokensValue == null || statErrorsValue == null || statDurationValue == null || statLinesValue == null) {
            return;
        }
        int validTokens = (int) result.getTokens().stream().filter(t -> !t.isError()).count();
        statTokensValue.setText(String.valueOf(validTokens));

        int errorCount = result.getErrorCount();
        if (errorCount == 0) {
            statErrorsValue.setText("0 Errors");
            statErrorsTag.setText("✓ Clean");
            statErrorsTag.getStyleClass().removeAll("stat-tag-red", "stat-tag-green");
            statErrorsTag.getStyleClass().add("stat-tag-green");
        } else {
            statErrorsValue.setText(errorCount + (errorCount == 1 ? " Error" : " Errors"));
            statErrorsTag.setText("⚠ Attention");
            statErrorsTag.getStyleClass().removeAll("stat-tag-red", "stat-tag-green");
            statErrorsTag.getStyleClass().add("stat-tag-red");
        }

        statDurationValue.setText(String.format("%.2f ms", result.getExecutionTimeMs()));
        statLinesValue.setText(result.getTotalLines() + (result.getTotalLines() == 1 ? " Line" : " Lines"));
    }

    private void updateErrorsNotification(ScanResult result) {
        errorNotificationBox.getChildren().clear();

        if (!result.hasErrors()) {
            errorNotificationBox.setVisible(false);
            errorNotificationBox.setManaged(false);
            return;
        }

        errorNotificationBox.setVisible(true);
        errorNotificationBox.setManaged(true);

        for (LexicalError err : result.getErrors()) {
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
            jumpBtn.setOnAction(e -> highlightPosition(err.line(), err.column()));

            alert.getChildren().addAll(icon, info, sp, jumpBtn);
            errorNotificationBox.getChildren().add(alert);
        }
    }

    private void highlightPosition(int targetLine, int targetCol) {
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

    private void runAllAutomatedTests() {
        List<TestReport> reports = testRunnerService.runAllTests();
        testReportsData.clear();
        testReportsData.addAll(reports);

        long passedCount = reports.stream().filter(TestReport::passed).count();
        int total = reports.size();

        if (passedCount == total) {
            testSummaryBadge.setText(String.format("🎉 %d / %d PASSED (100%%)", passedCount, total));
            testSummaryBadge.getStyleClass().removeAll("stat-tag-blue", "stat-tag-red");
            testSummaryBadge.getStyleClass().add("stat-tag-green");
        } else {
            testSummaryBadge.setText(String.format("⚠ %d / %d Passed", passedCount, total));
            testSummaryBadge.getStyleClass().removeAll("stat-tag-blue", "stat-tag-green");
            testSummaryBadge.getStyleClass().add("stat-tag-red");
        }
    }

    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Arithmetic Expression File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text / Calc Files", "*.txt", "*.calc", "*.math"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                showView(scannerView, navScannerBtn);
                codeEditor.setText(content);
                performScan();
            } catch (IOException e) {
                showAlert("Error Opening File", e.getMessage());
            }
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
