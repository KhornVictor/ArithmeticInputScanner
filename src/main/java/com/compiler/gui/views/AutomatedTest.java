package com.compiler.gui.views;

import java.util.List;
import java.util.function.Consumer;

import com.compiler.model.TestCase;
import com.compiler.model.TestReport;
import com.compiler.service.TestRunnerService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * View 2: Automated test suite dashboard executing and verifying
 * the 16 benchmark tests with pass/fail badges.
 */
public class AutomatedTest extends VBox {

    private final TestRunnerService testRunnerService;
    private final ObservableList<TestReport> testReportsData = FXCollections.observableArrayList();
    private final TableView<TestReport> testTableView;
    private final Label testSummaryBadge;
    private Consumer<TestCase> onTestCaseSelected;

    public AutomatedTest(TestRunnerService testRunnerService) {
        super(16);
        this.testRunnerService = testRunnerService;
        getStyleClass().add("card");

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

        // Double-click row to load into scanner
        testTableView.setRowFactory(tv -> {
            TableRow<TestReport> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    if (onTestCaseSelected != null) {
                        onTestCaseSelected.accept(row.getItem().testCase());
                    }
                }
            });
            return row;
        });

        getChildren().addAll(header, testTableView);
    }

    public void setOnTestCaseSelected(Consumer<TestCase> callback) {
        this.onTestCaseSelected = callback;
    }

    public void runAllAutomatedTests() {
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

    public boolean hasReports() {
        return !testReportsData.isEmpty();
    }

    public ObservableList<TestReport> getTestReportsData() {
        return testReportsData;
    }

    public TableView<TestReport> getTestTableView() {
        return testTableView;
    }
}
