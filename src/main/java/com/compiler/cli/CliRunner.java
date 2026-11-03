package com.compiler.cli;

import com.compiler.model.LexicalError;
import com.compiler.model.ScanResult;
import com.compiler.model.Token;
import com.compiler.service.ScannerService;

import java.io.File;
import java.io.IOException;

/**
 * Command-line interface driver that prints tokens and errors matching the project brief specification.
 */
public class CliRunner {

    private final ScannerService scannerService = new ScannerService();

    public int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("Error: File not found: " + args[0]);
            return 1;
        }

        try {
            ScanResult result = scannerService.scanFile(file);
            printResult(result);
            return result.hasErrors() ? 2 : 0;
        } catch (IOException e) {
            System.err.println("I/O Error reading file: " + e.getMessage());
            return 1;
        }
    }

    public void printResult(ScanResult result) {
        System.out.printf("%-14s | %-16s | %-6s | %-6s%n", "Token type", "Lexeme", "Line", "Column");
        System.out.println("---------------------------------------------------------------");

        for (Token token : result.getTokens()) {
            if (!token.isError()) {
                System.out.printf("%-14s | %-16s | %-6d | %-6d%n",
                        token.getTypeName(),
                        escapeLexeme(token.getLexeme()),
                        token.getLine(),
                        token.getColumn());
            }
        }

        System.out.println("---------------------------------------------------------------");
        if (result.hasErrors()) {
            System.err.println("\nLEXICAL ERRORS FOUND (" + result.getErrorCount() + "):");
            for (LexicalError err : result.getErrors()) {
                System.err.println("  -> " + err);
            }
        }
        System.out.printf("Summary: %d tokens scanned, %d error(s) in %.3f ms.%n",
                result.getTokenCount() - result.getErrorCount(),
                result.getErrorCount(),
                result.getExecutionTimeMs());
    }

    private String escapeLexeme(String lexeme) {
        return lexeme.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void printUsage() {
        System.out.println("Arithmetic Input Scanner - JFlex & JavaFX");
        System.out.println("Usage:");
        System.out.println("  java -jar arithmetic-scanner.jar <input-file>   : Run scanner on file");
        System.out.println("  java -jar arithmetic-scanner.jar --gui          : Launch JavaFX GUI");
        System.out.println("  java -jar arithmetic-scanner.jar --test         : Run all 15+ automated tests");
    }
}
