package com.compiler;

import com.compiler.cli.CliRunner;
import com.compiler.gui.ScannerGuiApp;
import com.compiler.model.TestReport;
import com.compiler.service.TestRunnerService;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.List;

/**
 * Universal Entry Point for Arithmetic Input Scanner.
 * Automatically runs CLI and GUI simultaneously in parallel.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Explicit CLI Test Flag
        if (args.length > 0 && ("--test".equalsIgnoreCase(args[0]) || "-t".equalsIgnoreCase(args[0]))) {
            runAutomatedTestsCli();
            return;
        }

        // 2. Explicit Help Flag
        if (args.length > 0 && ("--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0]))) {
            printHelp();
            return;
        }

        // 3. Explicit CLI-Only Flag (--cli or -c)
        if (args.length > 0 && ("--cli".equalsIgnoreCase(args[0]) || "-c".equalsIgnoreCase(args[0]))) {
            String[] cliArgs = new String[args.length - 1];
            System.arraycopy(args, 1, cliArgs, 0, args.length - 1);
            CliRunner runner = new CliRunner();
            int exitCode = runner.run(cliArgs);
            System.exit(exitCode);
            return;
        }

        // 4. File provided as argument -> Run CLI scanner AND launch GUI simultaneously with the file pre-loaded!
        if (args.length > 0 && !args[0].startsWith("--")) {
            File file = new File(args[0]);
            if (file.exists()) {
                System.out.println("\u001B[1m\u001B[96m================================================================================\u001B[0m");
                System.out.println("\u001B[1m\u001B[96m>>> [CLI & GUI Simultaneous Mode] Initial CLI Scan:\u001B[0m " + file.getName());
                System.out.println("\u001B[1m\u001B[96m================================================================================\u001B[0m");
                CliRunner runner = new CliRunner();
                runner.run(new String[]{args[0]});
                System.out.println("\n\u001B[1m\u001B[92m>>> Launching JavaFX GUI in parallel (window opening)...\u001B[0m\n");
            }
            if (!GraphicsEnvironment.isHeadless()) {
                launchGui(args);
            }
            return;
        }

        // 5. Explicit GUI Flag or No arguments -> Launch GUI and stream synchronized CLI output simultaneously
        if (!GraphicsEnvironment.isHeadless()) {
            System.out.println("\u001B[1m\u001B[96m================================================================================\u001B[0m");
            System.out.println("\u001B[1m\u001B[96m>>> [CLI & GUI Simultaneous Mode] Launching GUI with live CLI synchronization...\u001B[0m");
            System.out.println("\u001B[1m\u001B[96m================================================================================\u001B[0m");
            launchGui(args);
        } else {
            System.out.println("Headless environment detected. Falling back to CLI mode.");
            CliRunner runner = new CliRunner();
            runner.run(args);
        }
    }

    private static void launchGui(String[] args) {
        try {
            ScannerGuiApp.launchApp(args);
        } catch (Throwable t) {
            System.err.println("Failed to initialize JavaFX GUI: " + t.getMessage());
            System.err.println("Falling back to CLI mode.");
            printHelp();
        }
    }

    private static void runAutomatedTestsCli() {
        System.out.println("================================================================================");
        System.out.println("            ARITHMETIC INPUT SCANNER - AUTOMATED TEST SUITE (16 TESTS)           ");
        System.out.println("================================================================================");
        System.out.printf("%-6s | %-15s | %-30s | %-8s%n", "ID", "Category", "Test Name", "Status");
        System.out.println("--------------------------------------------------------------------------------");

        TestRunnerService service = new TestRunnerService();
        List<TestReport> reports = service.runAllTests();
        int passed = 0;

        for (TestReport r : reports) {
            String status = r.passed() ? "\u001B[32mPASS\u001B[0m" : "\u001B[31mFAIL\u001B[0m";
            if (r.passed()) passed++;
            System.out.printf("%-6s | %-15s | %-30s | %-8s%n",
                    r.testCase().id(),
                    r.testCase().category(),
                    r.testCase().title(),
                    status);
            System.out.println("       Reason: " + r.message());
        }

        System.out.println("================================================================================");
        System.out.printf("SUMMARY: %d / %d Tests Passed (%.1f%%)%n",
                passed, reports.size(), (passed * 100.0) / reports.size());
        System.out.println("================================================================================");
    }

    private static void printHelp() {
        System.out.println("Arithmetic Input Scanner (Topic 4) - JFlex & JavaFX");
        System.out.println("Compilation I4 GIC | Academic Year 2026-2027 | Lecturer: OL Phearun");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar arithmetic-scanner.jar <input-file>   : Run CLI and GUI simultaneously");
        System.out.println("  java -jar arithmetic-scanner.jar --cli <file>   : Run CLI-only on file without GUI");
        System.out.println("  java -jar arithmetic-scanner.jar --gui          : Launch JavaFX GUI with CLI synchronization");
        System.out.println("  java -jar arithmetic-scanner.jar --test         : Run all 16 automated test cases");
        System.out.println("  java -jar arithmetic-scanner.jar --help         : Display this help message");
        System.out.println();
        System.out.println("Maven shortcuts:");
        System.out.println("  mvn javafx:run                                  : Run JavaFX GUI directly with synchronized CLI");
        System.out.println("  mvn exec:java -Dexec.args=\"<input-file>\"       : Run CLI & GUI simultaneously on input file");
        System.out.println("  mvn exec:java -Dexec.args=\"--test\"             : Run test suite in CLI");
    }
}
