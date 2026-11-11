package com.compiler.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.compiler.cli.CliRunner;
import com.compiler.gui.layouts.MainLayout;
import com.compiler.gui.layouts.TopNavBar;
import com.compiler.gui.ui.AlertHelper;
import com.compiler.gui.views.AutomatedTest;
import com.compiler.gui.views.DFA;
import com.compiler.gui.views.MainScanner;
import com.compiler.model.ScanResult;
import com.compiler.model.TestCase;
import com.compiler.service.ScannerService;
import com.compiler.service.TestRunnerService;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main JavaFX Desktop Application entry point for Arithmetic Input Scanner.
 * Coordinates modular layouts, views, and service interactions, while running
 * in parallel and synchronizing live scan results directly to the CLI terminal.
 */
public class ScannerGuiApp extends Application {

    private static String[] launchArgs = new String[0];

    private final ScannerService scannerService = new ScannerService();
    private final TestRunnerService testRunnerService = new TestRunnerService();
    private final CliRunner cliRunner = new CliRunner();

    // 400ms debounce for typing in the editor so CLI terminal isn't flooded on every keystroke
    private final PauseTransition cliDebounce = new PauseTransition(Duration.millis(400));
    private ScanResult latestScanResult;

    private Stage primaryStage;
    private MainScanner mainScannerView;
    private AutomatedTest automatedTestView;
    private DFA dfaView;
    private TopNavBar topNavBar;
    private MainLayout mainLayout;

    public static void launchApp(String[] args) {
        launchArgs = args != null ? args : new String[0];
        Application.launch(ScannerGuiApp.class, args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Arithmetic Scanner");

        // 1. Initialize Views
        mainScannerView = new MainScanner(scannerService);
        automatedTestView = new AutomatedTest(testRunnerService);
        dfaView = new DFA();

        // 2. Initialize Navigation and Layout
        topNavBar = new TopNavBar(testRunnerService.getAllTestCases());
        mainLayout = new MainLayout(topNavBar, mainScannerView, automatedTestView, dfaView);

        // 3. Wire navigation callbacks
        topNavBar.setOnTabSelected(this::handleTabSelected);
        topNavBar.setOnPresetSelected(this::handlePresetSelected);
        topNavBar.setOnOpenFileRequested(this::openFile);

        // Double-clicking a test in AutomatedTest view loads it into the scanner
        automatedTestView.setOnTestCaseSelected(this::handlePresetSelected);

        // 4. Synchronize GUI scanning with the CLI terminal output
        mainScannerView.setOnScanCompleted((result, isExplicit) -> {
            latestScanResult = result;
            if (isExplicit) {
                // Explicit button click, F5, or file load: print to CLI immediately!
                cliDebounce.stop();
                printSynchronizedCliOutput(result);
            } else {
                // User is actively typing: update GUI immediately, debounce CLI print
                cliDebounce.setOnFinished(e -> printSynchronizedCliOutput(latestScanResult));
                cliDebounce.playFromStart();
            }
        });

        // Set initial view
        topNavBar.selectTab(TopNavBar.NavTab.SCANNER);

        // 5. Build Scene & Keyboard Accelerators
        Scene scene = new Scene(mainLayout, 1220, 800);
        try {
            var css = getClass().getResource("/styles/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception ignored) {}

        // Global shortcuts: F5 or Ctrl+Enter to Scan
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F5), () -> mainScannerView.performScan(true));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), () -> mainScannerView.performScan(true));

        stage.setScene(scene);
        stage.show();

        // 6. Resolve initial source code (from CLI argument if provided, otherwise default sample)
        String initialCode = null;
        if (launchArgs.length > 0 && !launchArgs[0].startsWith("-")) {
            File argFile = new File(launchArgs[0]);
            if (argFile.exists()) {
                try {
                    initialCode = Files.readString(argFile.toPath());
                } catch (IOException ignored) {}
            }
        }
        if (initialCode == null) {
            initialCode = "// Sample Arithmetic Expression\ntotal = 12.5 + qty * 2;\nflag = count >= 10;\ninvalid_test = 50 $ 4;";
        }

        mainScannerView.setCode(initialCode);
        mainScannerView.performScan(true);
    }

    private void printSynchronizedCliOutput(ScanResult result) {
        if (result == null) return;
        System.out.println("""
                           
                           \u001b[1m\u001b[96m>>> [CLI & GUI Synchronized Scan Event]\u001b[0m \u001b[90m(""" + result.getTokenCount() + " tokens, " + result.getErrorCount() + " errors)\u001B[0m");
        cliRunner.printResult(result);
    }

    private void handleTabSelected(TopNavBar.NavTab tab) {
        switch (tab) {
            case SCANNER -> mainLayout.showView(mainScannerView);
            case TESTS -> {
                mainLayout.showView(automatedTestView);
                if (!automatedTestView.hasReports()) {
                    System.out.println("\n\u001B[1m\u001B[96m>>> [GUI Action] Running Automated Test Suite in parallel...\u001B[0m");
                    automatedTestView.runAllAutomatedTests();
                }
            }
            case DFA -> mainLayout.showView(dfaView);
        }
    }

    private void handlePresetSelected(TestCase testCase) {
        if (testCase != null) {
            topNavBar.selectTab(TopNavBar.NavTab.SCANNER);
            mainScannerView.setCode(testCase.input());
            mainScannerView.performScan(true);
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
                topNavBar.selectTab(TopNavBar.NavTab.SCANNER);
                mainScannerView.setCode(content);
                mainScannerView.performScan(true);
            } catch (IOException e) {
                AlertHelper.showAlert(primaryStage, "Error Opening File", e.getMessage());
            }
        }
    }
}
