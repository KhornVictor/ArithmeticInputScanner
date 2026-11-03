package com.compiler.model;

/**
 * Record representing a lexical error identified during scanning.
 */
public record LexicalError(String invalidLexeme, int line, int column, String message) {
    @Override
    public String toString() {
        return String.format("LEXICAL ERROR: %s at Line %d, Column %d", message, line, column);
    }
}
