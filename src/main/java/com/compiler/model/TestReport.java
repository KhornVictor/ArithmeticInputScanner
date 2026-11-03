package com.compiler.model;

/**
 * Execution result for a single test case.
 */
public record TestReport(
        TestCase testCase,
        boolean passed,
        ScanResult scanResult,
        String message
) {}
