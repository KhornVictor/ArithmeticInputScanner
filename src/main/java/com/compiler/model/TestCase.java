package com.compiler.model;

/**
 * Metadata and specification for a test case.
 */
public record TestCase(
        String id,
        String category, // "Normal", "Boundary", "Invalid", "Bug Correction"
        String title,
        String description,
        String input,
        String expectedSummary
) {}
