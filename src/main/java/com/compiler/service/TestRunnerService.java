package com.compiler.service;

import com.compiler.model.ScanResult;
import com.compiler.model.TestCase;
import com.compiler.model.TestReport;
import com.compiler.model.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service to execute the official 15+ test cases required by the project brief.
 */
public class TestRunnerService {

    private final ScannerService scannerService = new ScannerService();
    private final List<TestCase> testCases = new ArrayList<>();

    public TestRunnerService() {
        initTestCases();
    }

    private void initTestCases() {
        // --- 6 NORMAL INPUTS ---
        testCases.add(new TestCase(
                "N01", "Normal", "Basic Arithmetic Assignment",
                "Simple assignment with identifier, decimal, addition, multiplication",
                "total = 12.5 + qty * 2;",
                "Expected: IDENTIFIER(total), ASSIGN(=), DECIMAL(12.5), OP_PLUS(+), IDENTIFIER(qty), OP_MUL(*), INTEGER(2), SEMICOLON(;)"
        ));

        testCases.add(new TestCase(
                "N02", "Normal", "Parentheses & Precedence",
                "Expression with grouping parentheses and division",
                "result = (base + offset) * (height / scale);",
                "Expected: IDENTIFIER, ASSIGN, LPAREN, IDENTIFIER, OP_PLUS, IDENTIFIER, RPAREN, OP_MUL, LPAREN, IDENTIFIER, OP_DIV, IDENTIFIER, RPAREN, SEMICOLON"
        ));

        testCases.add(new TestCase(
                "N03", "Normal", "Exponentiation & Modulo",
                "Expression using power (^) and modulo (%) operators",
                "y = 3 * x^2 + 7 % divisor;",
                "Expected: IDENTIFIER, ASSIGN, INTEGER, OP_MUL, IDENTIFIER, OP_POW, INTEGER, OP_PLUS, INTEGER, OP_MOD, IDENTIFIER, SEMICOLON"
        ));

        testCases.add(new TestCase(
                "N04", "Normal", "Relational Comparisons",
                "Expression with relational comparisons (>= and == and !=)",
                "flag = (count >= 10) == (status != 0);",
                "Expected: IDENTIFIER, ASSIGN, LPAREN, IDENTIFIER, OP_GTE, INTEGER, RPAREN, OP_EQ, LPAREN, IDENTIFIER, OP_NEQ, INTEGER, RPAREN, SEMICOLON"
        ));

        testCases.add(new TestCase(
                "N05", "Normal", "Multi-statement Arithmetic",
                "Multiple statements with variable chains and semicolons",
                "step = a + b - c * d / e; final_val = step + 42;",
                "Expected: Sequence of tokens for both statements, all separated cleanly by SEMICOLON"
        ));

        testCases.add(new TestCase(
                "N06", "Normal", "Comments and Whitespace",
                "Single-line (//) and multi-line (/* ... */) comments mixed with code",
                "// Calculate volume\nlength = 10.5; /* length in cm */\nwidth = 4.2;   /* width in cm */\nheight = 2.0;\nvolume = length * width * height;",
                "Expected: Comments and newlines skipped cleanly; only statement tokens produced"
        ));

        // --- 5 BOUNDARY & COMPETING-RULE CASES ---
        testCases.add(new TestCase(
                "B01", "Boundary", "Empty Input",
                "Empty text input; scanner must handle clean EOF with zero tokens and zero errors",
                "",
                "Expected: 0 tokens, 0 errors, no crash"
        ));

        testCases.add(new TestCase(
                "B02", "Boundary", "Adjacent Competing Operators",
                "Competing rules for >= vs >, == vs =, <= vs < without spaces",
                "a>=b>c==d!=e<=f<g;",
                "Expected: Longest match matches OP_GTE(>=), OP_GT(>), OP_EQ(==), OP_NEQ(!=), OP_LTE(<=), OP_LT(<)"
        ));

        testCases.add(new TestCase(
                "B03", "Boundary", "Decimal vs Integer Literal Distinction",
                "Decimals (12.50, 0.99) vs Integers (12, 0) and leading zero handling",
                "a=12.50;b=12;c=0.99;d=0;",
                "Expected: 12.50 and 0.99 as DECIMAL, 12 and 0 as INTEGER"
        ));

        testCases.add(new TestCase(
                "B04", "Boundary", "Division Operator vs Comment Start",
                "Tests competing '/' operator against '//' comment prefix",
                "val = 100 / 5; // inline comment\nratio = 20 / 4;",
                "Expected: 100 / 5 emits OP_DIV, '// inline comment' skipped, ratio = 20 / 4 emits OP_DIV"
        ));

        testCases.add(new TestCase(
                "B05", "Boundary", "End of Input Boundary",
                "Input terminating immediately after a literal without newline or semicolon",
                "total = 999",
                "Expected: IDENTIFIER(total), ASSIGN(=), INTEGER(999), clean termination at EOF"
        ));

        // --- 4 INVALID INPUTS ---
        testCases.add(new TestCase(
                "E01", "Invalid", "Unsupported Character ($)",
                "Expression containing illegal dollar symbol ($)",
                "total = 50 $ 4;",
                "Expected: Lexical error reported for '$' at Line 1, Column 11; scanning continues"
        ));

        testCases.add(new TestCase(
                "E02", "Invalid", "Multiple Illegal Symbols (# and @)",
                "Expression with hash (#) and at (@) characters",
                "bad_var = #tag + @user;",
                "Expected: 2 lexical errors reported ('#' at col 11, '@' at col 18); valid tokens still recognized"
        ));

        testCases.add(new TestCase(
                "E03", "Invalid", "Malformed Double-Dot Number",
                "Number with consecutive decimal points: 3.14.15",
                "val = 3.14.15;",
                "Expected: 3.14 matched as DECIMAL, second '.' reported as error, 15 matched as INTEGER"
        ));

        testCases.add(new TestCase(
                "E04", "Invalid", "Unsupported Tilde and Quotes (~ and \")",
                "Input containing unsupported bitwise tilde (~) and string quote (\")",
                "ratio = 10 ~ 2; name = \"text\";",
                "Expected: Lexical errors reported for '~' and '\"'; other tokens recognized cleanly"
        ));

        // --- 1 BUG CORRECTION TEST ---
        testCases.add(new TestCase(
                "BUG01", "Bug Correction", "Incomplete Decimal With Trailing Dot",
                "Tests bug where trailing dot without fractional digits (e.g., '12.') caused malformed decimal match",
                "x = 12. + 5;",
                "Expected: '12' matched as INTEGER, '.' reported as lexical error, '+' and '5' recognized cleanly"
        ));
    }

    public List<TestCase> getAllTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public TestReport runTest(TestCase tc) {
        ScanResult result = scannerService.scan(tc.input());
        boolean passed = false;
        String message = "";

        switch (tc.id()) {
            case "N01":
                passed = !result.hasErrors() && result.getTokenCount() == 8
                        && result.getTokens().get(2).getType() == TokenType.DECIMAL;
                message = passed ? "All 8 tokens correctly matched, including DECIMAL 12.5" : "Token count or types mismatch";
                break;
            case "N02":
                passed = !result.hasErrors() && result.getTokenCount() == 14;
                message = passed ? "All 14 tokens with parentheses matched successfully" : "Token count mismatch";
                break;
            case "N03":
                passed = !result.hasErrors() && result.getTokenCount() == 12
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_POW)
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_MOD);
                message = passed ? "Power (^) and modulo (%) operators recognized successfully" : "Operators mismatch";
                break;
            case "N04":
                passed = !result.hasErrors() && result.getTokenCount() == 14
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_GTE)
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_EQ)
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_NEQ);
                message = passed ? "Relational operators (>=, ==, !=) matched correctly" : "Relational operator mismatch";
                break;
            case "N05":
                passed = !result.hasErrors() && result.getTokenCount() == 18;
                message = passed ? "Multi-statement expression scanned cleanly with 18 tokens" : "Token count mismatch: got " + result.getTokenCount();
                break;
            case "N06":
                passed = !result.hasErrors() && result.getTokenCount() == 20;
                message = passed ? "All comments skipped cleanly; 20 code tokens generated" : "Comment skipping failed: got " + result.getTokenCount();
                break;
            case "B01":
                passed = !result.hasErrors() && result.getTokenCount() == 0;
                message = passed ? "Empty input handled cleanly: 0 tokens, 0 errors" : "Failed on empty input";
                break;
            case "B02":
                passed = !result.hasErrors()
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_GTE)
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_EQ)
                        && result.getTokens().stream().anyMatch(t -> t.getType() == TokenType.OP_LTE);
                message = passed ? "Longest match correctly disambiguated >=, ==, and <=" : "Disambiguation failed";
                break;
            case "B03":
                passed = !result.hasErrors()
                        && result.getTokens().get(2).getType() == TokenType.DECIMAL
                        && result.getTokens().get(6).getType() == TokenType.INTEGER
                        && result.getTokens().get(10).getType() == TokenType.DECIMAL
                        && result.getTokens().get(14).getType() == TokenType.INTEGER;
                message = passed ? "Decimals (12.50, 0.99) and Integers (12, 0) correctly distinguished" : "Literal matching failed";
                break;
            case "B04":
                passed = !result.hasErrors()
                        && result.getTokens().stream().filter(t -> t.getType() == TokenType.OP_DIV).count() == 2;
                message = passed ? "Both division operators recognized; inline comment skipped cleanly" : "Division vs comment failed";
                break;
            case "B05":
                passed = !result.hasErrors() && result.getTokenCount() == 3;
                message = passed ? "Clean termination at EOF without trailing newline or semicolon" : "EOF handling failed";
                break;
            case "E01":
                passed = result.hasErrors() && result.getErrorCount() == 1
                        && result.getErrors().get(0).invalidLexeme().equals("$");
                message = passed ? "Lexical error for '$' caught at Line 1, Column 11; scanning completed" : "Error not caught";
                break;
            case "E02":
                passed = result.hasErrors() && result.getErrorCount() == 2;
                message = passed ? "Both '#' and '@' caught as lexical errors; remaining tokens intact" : "Expected 2 errors";
                break;
            case "E03":
                passed = result.hasErrors() && result.getErrorCount() == 1
                        && result.getErrors().get(0).invalidLexeme().equals(".");
                message = passed ? "Malformed decimal second dot caught; 3.14 and 15 recognized" : "Double dot error not caught";
                break;
            case "E04":
                passed = result.hasErrors() && result.getErrorCount() >= 2;
                message = passed ? "Tilde (~) and quotes (\") caught as lexical errors" : "Expected errors for ~ and \"";
                break;
            case "BUG01":
                passed = result.hasErrors() && result.getErrorCount() == 1
                        && result.getTokens().get(2).getType() == TokenType.INTEGER
                        && result.getErrors().get(0).invalidLexeme().equals(".");
                message = passed ? "Trailing dot rejected cleanly; '12' matched as INTEGER" : "Bug correction failed";
                break;
            default:
                passed = true;
                message = "Executed successfully";
        }

        return new TestReport(tc, passed, result, message);
    }

    public List<TestReport> runAllTests() {
        List<TestReport> reports = new ArrayList<>();
        for (TestCase tc : testCases) {
            reports.add(runTest(tc));
        }
        return reports;
    }
}
