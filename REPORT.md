# Lab01 JFlex Group Project Report: Arithmetic Input Scanner

**Course:** Compilation I4 GIC  
**Academic Year:** 2026–2027  
**Lecturer:** OL Phearun  
**Topic:** Topic 4 — Arithmetic Input Scanner  
**Project Title:** Lexical Analyzer and JavaFX Interactive Environment for Arithmetic Expressions  

---

## 1. Project Purpose & Scope

### 1.1 Purpose
The purpose of this project is to design, implement, and verify a lexical analyzer (scanner) using **JFlex** and **Java** that processes arithmetic expressions. Lexical analysis is the initial phase of a compiler, converting a raw stream of characters from a source text file into a structured sequence of lexical tokens while tracking 1-based line and column positions, discarding comments and whitespace, and reporting lexical errors without terminating the scanning process prematurely.

In addition to standard console operation, a complete **JavaFX Graphical User Interface (GUI)** was developed to provide live visualization, automated test verification, token filtering, and interactive DFA state-machine tracing during classroom demonstration.

### 1.2 Language Specification and Scope Choices
Our arithmetic scanner supports arithmetic statements, identifiers, numeric literals, operators, and grouping delimiters.

#### In-Scope Constructs:
- **Identifiers:** Variable names starting with a letter or underscore, followed by alphanumeric characters or underscores: `[a-zA-Z_][a-zA-Z0-9_]*`.
- **Numeric Literals:**
  - **Integer Literals:** Zero (`0`) or non-zero numbers without leading zeros: `0 | [1-9][0-9]*`.
  - **Decimal Literals:** Decimal numbers requiring at least one digit before and after the period: `(0 | [1-9][0-9]*)\.[0-9]+`.
- **Assignment Operator:** Single equal sign `=`.
- **Arithmetic Operators:** Addition (`+`), subtraction (`-`), multiplication (`*`), division (`/`), modulo (`%`), and exponentiation (`^`).
- **Relational / Comparison Operators:** Greater than (`>`), greater than or equal (`>=`), less than (`<`), less than or equal (`<=`), equal to (`==`), not equal to (`!=`).
- **Delimiters & Grouping:** Parentheses `(` and `)`, statement terminators `;`, and commas `,`.
- **Comments & Whitespace:**
  - Single-line comments starting with `//` and extending to the end of the line.
  - Multi-line block comments enclosed between `/*` and `*/`.
  - Whitespaces (spaces, horizontal tabs, carriage returns, newlines) are skipped while tracking positions.
- **Error Handling Strategy:** Unexpected characters that do not match any defined rule (such as `$`, `#`, `@`, `~`, `"`) are captured by a fallback rule `[^]`. The scanner logs an explicit error with 1-based line and column numbers and **continues scanning** so that all errors across the source file can be identified in a single pass.

#### Out-of-Scope Constructs:
- Syntax parsing and checking if token sequences form valid grammar trees (e.g. `total = + * 2;` produces valid tokens, though syntactically invalid).
- Mathematical expression evaluation or semantic variable binding.
- Advanced scientific notation with exponents (`1e-5`), binary/hexadecimal notation, or string literals.

---

## 2. Token Definitions & Regular Expression Specification

The scanner specifies **20 distinct token types**, exceeding the minimum project requirement of 6:

| Token Type | Category | Regular Expression Macro | Example Match | Description |
| :--- | :--- | :--- | :--- | :--- |
| `IDENTIFIER` | Identifier | `[a-zA-Z_][a-zA-Z0-9_]*` | `total`, `qty`, `base_val` | Alphanumeric identifier |
| `INTEGER` | Literal | `0 \| [1-9][0-9]*` | `0`, `42`, `1000` | Integer numeric literal |
| `DECIMAL` | Literal | `{Integer}\.[0-9]+` | `12.5`, `0.75`, `3.14159` | Fixed-point decimal literal |
| `ASSIGN` | Assignment | `"="` | `=` | Assignment sign |
| `OP_PLUS` | Operator | `"+"` | `+` | Addition operator |
| `OP_MINUS` | Operator | `"-"` | `-` | Subtraction operator |
| `OP_MUL` | Operator | `"*"` | `*` | Multiplication operator |
| `OP_DIV` | Operator | `"/"` | `/` | Division operator |
| `OP_MOD` | Operator | `"%"` | `%` | Modulo operator |
| `OP_POW` | Operator | `"^"` | `^` | Exponentiation operator |
| `OP_GT` | Comparison | `">"` | `>` | Greater-than relational operator |
| `OP_GTE` | Comparison | `">="` | `>=` | Greater-than-or-equal operator |
| `OP_LT` | Comparison | `"<"` | `<` | Less-than relational operator |
| `OP_LTE` | Comparison | `"<="` | `<=` | Less-than-or-equal operator |
| `OP_EQ` | Comparison | `"=="` | `==` | Equality relational operator |
| `OP_NEQ` | Comparison | `"!="` | `!=` | Inequality relational operator |
| `LPAREN` | Delimiter | `"("` | `(` | Left grouping parenthesis |
| `RPAREN` | Delimiter | `")"` | `)` | Right grouping parenthesis |
| `SEMICOLON` | Delimiter | `";"` | `;` | Semicolon delimiter |
| `COMMA` | Delimiter | `","` | `,` | Argument separator |

---

## 3. Competing Rules and JFlex Disambiguation Analysis

When writing lexical specifications, multiple regular expression rules can potentially match the same input prefix. JFlex resolves such competition using two standardized compiler principles:
1. **Longest Match (Maximal Munch):** The rule that matches the longest string of input characters takes precedence.
2. **Rule Priority (First Rule):** If two rules match the exact same number of characters, the rule appearing earliest in the specification file wins.

### Case 1: Relational Operator `>=` vs `>` and `=`
- **Conflict Scenario:** Consider the input `count >= 10;`. When the scanner reaches the character `>`, both the rule `">="` and the rule `">"` are candidate matches.
- **Competing Rules:**
  ```jflex
  ">="   { return token(TokenType.OP_GTE); }
  ">"    { return token(TokenType.OP_GT); }
  "="    { return token(TokenType.ASSIGN); }
  ```
- **Disambiguation:** If the scanner took the shortest match, it would match `>` as `OP_GT` and subsequently match `=` as `ASSIGN`. However, because JFlex adheres to the Maximal Munch rule, the scanner peeks ahead at the next character `=`. Matching `>=` consumes 2 characters, whereas `>` consumes only 1. Therefore, JFlex unambiguously generates a single `OP_GTE` token.

### Case 2: Numeric Literals: Decimal `12.5` vs Integer `12`
- **Conflict Scenario:** Consider the input `total = 12.5;`. When parsing `12.5`, the digits `12` match the `{Integer}` macro (`0 | [1-9][0-9]*`).
- **Competing Rules:**
  ```jflex
  {Decimal}  { return token(TokenType.DECIMAL); }
  {Integer}  { return token(TokenType.INTEGER); }
  ```
- **Disambiguation:** The `{Integer}` pattern would match `12` (length 2). The `{Decimal}` pattern matches `12.5` (length 4). Under the Longest Match principle, JFlex consumes all 4 characters and emits `DECIMAL`, preventing `12` from being prematurely emitted as an integer followed by an unexpected dot.

### Case 3 (Bonus): Division Operator `/` vs Single-Line Comment `//`
- **Conflict Scenario:** In `100 / 5; // comment`, encountering `//` could potentially be treated as two consecutive division operators.
- **Disambiguation:** The comment rule `"//" [^\r\n]*` matches from `//` until the newline (length > 2), whereas `"/"` matches length 1. The Longest Match principle ensures the entire comment is recognized and cleanly discarded.

---

## 4. Finite Automaton (DFA) Specification

We formalize the Deterministic Finite Automaton (DFA) corresponding to the `DECIMAL` regular expression macro:
$$\text{DECIMAL} = (0 \mid [1-9][0-9]*)\.[0-9]+$$

### 4.1 Formal 5-Tuple Definition
The DFA is defined as $M = (Q, \Sigma, \delta, q_0, F)$:
- $Q = \{S_0, S_1, S_2, S_3, S_4\}$
- $\Sigma = \{'0', '1'\dots'9', '.', \text{other}\}$
- $q_0 = S_0$
- $F = \{S_4\}$ (Accepting state for `DECIMAL`)
- Transition function $\delta$ given below:

### 4.2 State Transition Table
| State | $'0'$ | $'1'\dots'9'$ | $'.'$ | Other Characters | Accepting? |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **$S_0$** (Start) | $S_1$ | $S_2$ | Reject | Reject | No |
| **$S_1$** (Leading zero) | Reject | Reject | $S_3$ | Reject | No |
| **$S_2$** (Non-zero integer digits) | $S_2$ | $S_2$ | $S_3$ | Reject | No |
| **$S_3$** (Decimal point read) | $S_4$ | $S_4$ | Reject | Reject | No |
| **$S_4$** (Fractional digits) | $S_4$ | $S_4$ | Reject | Reject | **YES** |

### 4.3 State Transition Diagram
```
             '0'
       (S0) ----> (S1) --'.'----\
         |                       \
         | '1'-'9'       '.'      v       '0'-'9'         '0'-'9'
         +------> (S2) ---------> (S3) -----------> ((S4)) <----+
                   ^               |                   |        |
                   \---'0'-'9'-----/                   \--------/
```
- **Example Trace for `12.5`:**
  1. Input starts at $S_0$.
  2. Character `'1'` ($\in '1'\dots'9'$) transitions $S_0 \to S_2$.
  3. Character `'2'` ($\in '0'\dots'9'$) loops $S_2 \to S_2$.
  4. Character `'.'` transitions $S_2 \to S_3$.
  5. Character `'5'` ($\in '0'\dots'9'$) transitions $S_3 \to S_4$.
  6. End of literal reached at accepting state $S_4$. Successfully matches `DECIMAL`.

---

## 5. Bug Discovery, Root Cause & Correction

### 5.1 Bug Description
During initial testing of edge cases, we analyzed inputs with incomplete decimal notation such as:
```text
x = 12. + 5;
```
In our first implementation draft, the decimal macro was loosely defined as:
```jflex
Decimal = {Integer} \. {Digit}*   /* BUG: Digit* allows 0 digits after dot */
```
Under this flawed pattern, the scanner consumed `12.` as a complete `DECIMAL` token, despite having no fractional digits. This violates arithmetic syntax where a decimal point must be accompanied by digits.

### 5.2 Root Cause Analysis
Using `*` (zero or more occurrences) allowed the state machine to transition to an accepting state immediately upon reading the dot character without enforcing at least one trailing digit.

### 5.3 Correction & Verification
We corrected the rule to require one or more digits using `+`:
```jflex
Decimal = {Integer} \. {Digit}+   /* CORRECTED: Requires >= 1 digit */
```
With this fix:
1. Given `12. + 5;`, `{Decimal}` fails to match `12.` because no digit follows `.`.
2. `{Integer}` matches `12` as a valid integer token.
3. The dangling period `.` falls through to the unexpected character rule `[^]` and is reported as a lexical error at Line 1, Column 7.
4. Scanning continues seamlessly, correctly matching `+` and `5`.
This was preserved as automated test case **`BUG01`**, verifying that regression is prevented.

---

## 6. Test Suite and Experimental Evidence

A comprehensive suite of **16 automated test cases** was constructed covering normal inputs, edge/competing-rule boundaries, invalid inputs, and bug regression.

| Test ID | Category | Test Input | Expected Behavior | Observed Result | Status |
| :--- | :--- | :--- | :--- | :--- | :---: |
| **N01** | Normal | `total = 12.5 + qty * 2;` | 8 tokens: ID, ASSIGN, DECIMAL, OP_PLUS, ID, OP_MUL, INT, SEMICOLON | 8 tokens matched cleanly | **PASS** |
| **N02** | Normal | `result = (base + offset) * (height / scale);` | 14 tokens verifying parentheses grouping | 14 tokens matched cleanly | **PASS** |
| **N03** | Normal | `y = 3 * x^2 + 7 % divisor;` | Power (`^`) and modulo (`%`) operators | 12 tokens matched cleanly | **PASS** |
| **N04** | Normal | `flag = (count >= 10) == (status != 0);` | Relational operators (`>=`, `==`, `!=`) | 14 tokens matched cleanly | **PASS** |
| **N05** | Normal | `step = a + b - c * d / e; final_val = step + 42;` | Multi-statement semicolon separation | 18 tokens matched cleanly | **PASS** |
| **N06** | Normal | Code mixed with `//` and `/* ... */` comments | All comments skipped; statements parsed | 20 tokens matched cleanly | **PASS** |
| **B01** | Boundary | `""` (Empty input text) | Clean termination with 0 tokens, 0 errors | Handled cleanly | **PASS** |
| **B02** | Boundary | `a>=b>c==d!=e<=f<g;` | Longest match on contiguous operators | Disambiguated cleanly | **PASS** |
| **B03** | Boundary | `a=12.50;b=12;c=0.99;d=0;` | Distinction between DECIMAL and INTEGER | Distinct literals verified | **PASS** |
| **B04** | Boundary | `val = 100 / 5; // inline comment` | Division operator emitted; comment skipped | 2 OP_DIV tokens found | **PASS** |
| **B05** | Boundary | `total = 999` (No newline/semicolon at EOF) | Clean EOF handling on literal | 3 tokens matched | **PASS** |
| **E01** | Invalid | `total = 50 $ 4;` | Lexical error reported for `$` at Col 12 | Error caught; scan continued | **PASS** |
| **E02** | Invalid | `bad_var = #tag + @user;` | 2 lexical errors (`#` and `@`) reported at positions | Both errors caught cleanly | **PASS** |
| **E03** | Invalid | `val = 3.14.15;` | Decimal `3.14`, error on second `.`, integer `15` | Error on second `.` caught | **PASS** |
| **E04** | Invalid | `ratio = 10 ~ 2; name = "test";` | Errors on unsupported `~` and `"` symbols | Errors reported accurately | **PASS** |
| **BUG01**| Bug Fix | `x = 12. + 5;` | Incomplete decimal trailing dot rejected cleanly | `12` is INT, `.` is ERROR | **PASS** |

**Summary:** 16 / 16 Tests Passed (100.0% Pass Rate).

---

## 7. JavaFX GUI Architecture & Implementation Highlights

The JavaFX interface (`ScannerGuiApp`) was built with modern developer ergonomics:
1. **Model-View Separation:** The scanning engine (`ScannerService`) is decoupled from the GUI, allowing identical scanning logic in CLI and GUI modes.
2. **Interactive Code Editor:** Includes line/character counters, quick-insert snippets for fast demonstration, and shortcut bindings (`F5` / `Ctrl+Enter`).
3. **Token TableView with Live Search:** Color-coded category badges (Blue for Identifier, Green for Literal, Amber for Operator, Slate for Delimiter, Red for Error) with instant text filtering.
4. **Diagnostic Error Inspector:** Summarizes all lexical errors with 1-click "Jump to Error" navigation that highlights the offending character directly in the editor.
5. **Interactive DFA Canvas:** Renders the 5-state automaton for decimals with dynamic state-path highlighting as users type numbers in real time.
6. **Built-in 16-Case Test Runner:** Allows one-click testing of all benchmark cases during live class demonstrations.
7. **Token Export:** Enables exporting token tables to CSV, JSON, and formatted plain text.

---

## 8. Group Contributions & Lessons Learned

### 8.1 Contribution Record
| Member Name | Student ID | Specific Responsibility |
| :--- | :--- | :--- |
| **Member 1** | `p202X001` | Specification Design: Regular expression macros, token types, and JFlex rules in `ArithmeticScanner.flex`. |
| **Member 2** | `p202X002` | Scanner Integration: Java driver classes (`Main`, `CliRunner`, `ScannerService`), Maven configuration and JAR assembly. |
| **Member 3** | `p202X003` | User Interface: JavaFX GUI layout, styling (`app.css`), token table filtering, and interactive DFA canvas visualizer. |
| **Member 4** | `p202X004` | Quality Assurance & Documentation: Designing the 16 test cases, expected outputs, bug test case (`BUG01`), and project report. |

### 8.2 Lessons Learned
- **Maximal Munch Precision:** Lexical ambiguity between multi-character operators (`>=`, `==`) and single-character operators (`>`, `=`) is elegantly resolved by JFlex's longest match principle without requiring manual lookahead logic.
- **Robust Error Recovery:** Reporting lexical errors while allowing the scanner to proceed to EOF is crucial for compiler usability, enabling developers to see all lexical issues in a single compilation run.
- **Strict Regular Expressions:** Small oversights in regular expression quantifiers (such as `*` vs `+`) can cause subtle lexical bugs that pass casual inspection but fail boundary tests.
