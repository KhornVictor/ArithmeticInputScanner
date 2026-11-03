package com.compiler.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the complete result of a lexical analysis run.
 */
public class ScanResult {
    private final List<Token> tokens;
    private final List<LexicalError> errors;
    private final long executionTimeNs;
    private final int totalLines;
    private final int totalCharacters;

    public ScanResult(List<Token> tokens, List<LexicalError> errors, long executionTimeNs, int totalLines, int totalCharacters) {
        this.tokens = Collections.unmodifiableList(tokens);
        this.errors = Collections.unmodifiableList(errors);
        this.executionTimeNs = executionTimeNs;
        this.totalLines = totalLines;
        this.totalCharacters = totalCharacters;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<LexicalError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int getTokenCount() {
        return tokens.size();
    }

    public int getErrorCount() {
        return errors.size();
    }

    public long getExecutionTimeNs() {
        return executionTimeNs;
    }

    public double getExecutionTimeMs() {
        return executionTimeNs / 1_000_000.0;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public int getTotalCharacters() {
        return totalCharacters;
    }
}
