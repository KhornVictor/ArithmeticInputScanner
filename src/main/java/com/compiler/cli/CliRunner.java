package com.compiler.cli;

import java.io.File;
import java.io.IOException;

import com.compiler.model.LexicalError;
import com.compiler.model.ScanResult;
import com.compiler.model.Token;
import com.compiler.model.TokenType;
import com.compiler.service.ScannerService;

/**
 * Command-line interface driver that prints tokens and errors matching the project brief specification,
 * enhanced with ANSI color formatting for rapid identification of tokens, errors, and bugs across all terminals.
 */
public class CliRunner {

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    private static final String BRIGHT_RED = "\u001B[91m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";
    private static final String BRIGHT_CYAN = "\u001B[96m";
    private static final String BRIGHT_WHITE = "\u001B[97m";

    private static final String BG_RED = "\u001B[41m";

    private final ScannerService scannerService = new ScannerService();

    public static void main(String[] args) {
        CliRunner runner = new CliRunner();
        int exitCode = runner.run(args);

        // If a display environment is available and user didn't request --cli only, launch GUI in parallel!
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("\n\u001B[1m\u001B[92m>>> Opening JavaFX GUI window simultaneously...\u001B[0m\n");
            try {
                com.compiler.gui.ScannerGuiApp.launchApp(args);
            } catch (Throwable ignored) {}
        } else {
            System.exit(exitCode);
        }
    }

    public int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println(BRIGHT_RED + BOLD + "[ERROR] File not found: " + RESET + args[0]);
            return 1;
        }

        try {
            ScanResult result = scannerService.scanFile(file);
            printResult(result);
            return result.hasErrors() ? 2 : 0;
        } catch (IOException e) {
            System.err.println(BRIGHT_RED + BOLD + "[ERROR] I/O Error reading file: " + RESET + e.getMessage());
            return 1;
        }
    }

    public void printResult(ScanResult result) {
        // Table Header
        
        System.out.println(BOLD + BRIGHT_CYAN + "================================================================================" + RESET);
        System.out.printf(BOLD + BRIGHT_WHITE + "%-14s | %-20s | %-8s | %-8s | %-12s%n" + RESET,
                "Token type", "Lexeme", "Line", "Column", "Category");
        System.out.println(DIM + "--------------------------------------------------------------------------------" + RESET);

        for (Token token : result.getTokens()) {
            if (token.isError()) {
                // Highlight error token directly in stream with bold red background
                System.out.printf(BG_RED + BRIGHT_WHITE + BOLD + "%-14s" + RESET + " | " +
                                BRIGHT_RED + BOLD + "%-20s" + RESET + " | " +
                                BRIGHT_YELLOW + "%-8d" + RESET + " | " +
                                BRIGHT_YELLOW + "%-8d" + RESET + " | " +
                                BRIGHT_RED + BOLD + "%-12s" + RESET + "%n",
                        token.getTypeName(),
                        escapeLexeme(token.getLexeme()),
                        token.getLine(),
                        token.getColumn(),
                        "[ERROR]");
            } else {
                String color = getColorForCategory(token.getType().getCategory());
                System.out.printf(color + BOLD + "%-14s" + RESET + " | " +
                                BRIGHT_WHITE + "%-20s" + RESET + " | " +
                                "%-8d | " +
                                "%-8d | " +
                                DIM + "%-12s" + RESET + "%n",
                        token.getTypeName(),
                        escapeLexeme(token.getLexeme()),
                        token.getLine(),
                        token.getColumn(),
                        token.getCategory());
            }
        }

        System.out.println(DIM + "--------------------------------------------------------------------------------" + RESET);

        // Lexical Errors Banner
        if (result.hasErrors()) {
            System.out.println();
            System.out.println(BOLD + BRIGHT_RED + "[!] LEXICAL ERRORS DETECTED (" + result.getErrorCount() + " error" + (result.getErrorCount() == 1 ? "" : "s") + "):" + RESET);
            System.out.println(RED + "--------------------------------------------------------------------------------" + RESET);
            for (LexicalError err : result.getErrors()) {
                System.out.printf("  " + BRIGHT_RED + ">>" + RESET +
                                " [Line " + BRIGHT_YELLOW + "%d" + RESET +
                                ", Col " + BRIGHT_YELLOW + "%d" + RESET +
                                "] Invalid character: " + BG_RED + BRIGHT_WHITE + BOLD + " %s " + RESET +
                                " -> %s%n",
                        err.line(),
                        err.column(),
                        escapeLexeme(err.invalidLexeme()),
                        err.message());
            }
            System.out.println(DIM + "  (Note: Scanner recovered cleanly and continued until EOF)" + RESET);
            System.out.println(RED + "--------------------------------------------------------------------------------" + RESET);
        }

        // Summary Footer
        int validTokens = result.getTokenCount() - result.getErrorCount();
        System.out.println();
        if (result.hasErrors()) {
            System.out.printf(BOLD + BRIGHT_RED + "[FAILED]" + RESET +
                            " %d valid token(s), " +
                            BOLD + BRIGHT_RED + "%d error(s)" + RESET +
                            " in " + BRIGHT_CYAN + "%.3f ms" + RESET +
                            " (" + result.getTotalLines() + " lines, " + result.getTotalCharacters() + " chars)%n",
                    validTokens,
                    result.getErrorCount(),
                    result.getExecutionTimeMs());
        } else {
            System.out.printf(BOLD + BRIGHT_GREEN + "[OK] SUCCESS:" + RESET +
                            " %d valid token(s), " +
                            BRIGHT_GREEN + "0 errors (Clean)" + RESET +
                            " in " + BRIGHT_CYAN + "%.3f ms" + RESET +
                            " (" + result.getTotalLines() + " lines, " + result.getTotalCharacters() + " chars)%n",
                    validTokens,
                    result.getExecutionTimeMs());
        }
        System.out.println(BOLD + BRIGHT_CYAN + "================================================================================" + RESET);
    }

    private String getColorForCategory(TokenType.Category category) {
        return switch (category) {
            case IDENTIFIER -> BRIGHT_BLUE;
            case LITERAL -> BRIGHT_GREEN;
            case OPERATOR -> BRIGHT_YELLOW;
            case COMPARISON -> BRIGHT_MAGENTA;
            case DELIMITER -> BRIGHT_CYAN;
            case ERROR -> BRIGHT_RED;
        };
    }

    private String escapeLexeme(String lexeme) {
        return lexeme.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void printUsage() {
        System.out.println(BOLD + BRIGHT_CYAN + "Arithmetic Input Scanner - JFlex & JavaFX" + RESET);
        System.out.println(DIM + "Compilation I4 GIC | Academic Year 2026-2027 | Lecturer: OL Phearun" + RESET);
        System.out.println();
        System.out.println(BOLD + "Usage:" + RESET);
        System.out.println("  " + BRIGHT_GREEN + "java -jar arithmetic-scanner.jar <input-file>" + RESET + "   : Run CLI scanner on file");
        System.out.println("  " + BRIGHT_GREEN + "java -jar arithmetic-scanner.jar --gui" + RESET + "          : Launch JavaFX Graphical Interface");
        System.out.println("  " + BRIGHT_GREEN + "java -jar arithmetic-scanner.jar --test" + RESET + "         : Run all 16 automated benchmark tests");
        System.out.println("  " + BRIGHT_GREEN + "java -jar arithmetic-scanner.jar --help" + RESET + "         : Display this help message");
        System.out.println();
        System.out.println(BOLD + "Maven shortcuts:" + RESET);
        System.out.println("  mvn javafx:run                                  : Run JavaFX GUI directly");
        System.out.println("  mvn exec:java -Dexec.args=\"<input-file>\"       : Run CLI with input file");
        System.out.println("  mvn exec:java -Dexec.args=\"--test\"             : Run test suite");
    }
}
