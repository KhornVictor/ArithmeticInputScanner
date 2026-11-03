package com.compiler.service;

import com.compiler.ArithmeticScanner;
import com.compiler.model.LexicalError;
import com.compiler.model.ScanResult;
import com.compiler.model.Token;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service providing high-level scanning methods for text inputs, files, and readers.
 */
public class ScannerService {

    public ScanResult scan(String sourceText) {
        if (sourceText == null) {
            sourceText = "";
        }
        int totalChars = sourceText.length();
        int totalLines = sourceText.isEmpty() ? 0 : sourceText.split("\r\n|\r|\n", -1).length;

        long startTime = System.nanoTime();
        List<Token> tokens = new ArrayList<>();
        List<LexicalError> errors = new ArrayList<>();

        try (StringReader reader = new StringReader(sourceText)) {
            ArithmeticScanner scanner = new ArithmeticScanner(reader);
            tokens = scanner.scanAll();
            errors = new ArrayList<>(scanner.getErrors());
        } catch (IOException e) {
            errors.add(new LexicalError("", 0, 0, "I/O Error: " + e.getMessage()));
        }
        long duration = System.nanoTime() - startTime;

        return new ScanResult(tokens, errors, duration, totalLines, totalChars);
    }

    public ScanResult scanFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return scan(sb.toString());
    }
}
