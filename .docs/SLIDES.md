# Lab01 JFlex Group Project: Presentation Slides

**Course:** Compilation I4 GIC (Academic Year 2026–2027)  
**Lecturer:** OL Phearun  
**Topic 4:** Arithmetic Input Scanner  
**Presentation Duration:** ~10 Minutes (including Live Demo)

---

## 📽️ Slide 1: Project and Team

### Title: Arithmetic Input Scanner with JFlex & JavaFX
**Purpose:**  
To build a high-performance lexical analyzer using JFlex and Java that recognizes arithmetic expressions, numeric literals, relational operators, and identifiers, packaged with both a standards-compliant CLI and an interactive JavaFX GUI for live demonstration.

### Team Members & Student IDs:
- **[Member 1 Name]** (ID: `p202X001`) — JFlex Specification & Regular Expressions
- **[Member 2 Name]** (ID: `p202X002`) — Java Architecture, Driver & CLI
- **[Member 3 Name]** (ID: `p202X003`) — JavaFX GUI Application & DFA Canvas
- **[Member 4 Name]** (ID: `p202X004`) — 16-Case Test Suite & Documentation

---

## 📽️ Slide 2: Input and Scope

### Input Example:
```text
// Sample Arithmetic Input
total = 12.5 + qty * 2;
flag = count >= 10;
invalid_test = 50 $ 4;
```

### Expected Output Summary:
- Emits 1-based line & column positions:
  - `total` $\to$ `IDENTIFIER` (Line 2, Col 1)
  - `=` $\to$ `ASSIGN` (Line 2, Col 7)
  - `12.5` $\to$ `DECIMAL` (Line 2, Col 9)
  - `+` $\to$ `OP_PLUS` (Line 2, Col 14)
  - `qty` $\to$ `IDENTIFIER` (Line 2, Col 16)
  - `*` $\to$ `OP_MUL` (Line 2, Col 20)
  - `2` $\to$ `INTEGER` (Line 2, Col 22)
  - `;` $\to$ `SEMICOLON` (Line 2, Col 23)
- Comments (`// ...` and `/* ... */`) cleanly skipped.
- Illegal character `$` flagged at Line 4, Col 16; scanning continues to EOF!

### Clear Limits (Outside Scope):
- Syntax validation (AST parsing) is left outside scope.
- Semantic arithmetic evaluation (computing mathematical answers) is left outside scope.

---

## 📽️ Slide 3: Token Definitions & Competing Rules

### Compact Token Summary (20 Types Defined):
| Token Type | Macro Pattern | Example |
| :--- | :--- | :--- |
| `IDENTIFIER` | `[a-zA-Z_][a-zA-Z0-9_]*` | `total`, `qty`, `count` |
| `INTEGER` | `0 \| [1-9][0-9]*` | `0`, `42`, `100` |
| `DECIMAL` | `{Integer}\.[0-9]+` | `12.5`, `0.75` |
| `ASSIGN` | `"="` | `=` |
| `OP_PLUS`, `OP_MINUS`, `OP_MUL`, `OP_DIV`, `OP_MOD`, `OP_POW` | `"+"`, `"-"`, `"*"`, `"/"`, `"%"`, `"^"` | `+`, `-`, `*`, `/`, `%`, `^` |
| `OP_GT`, `OP_GTE`, `OP_LT`, `OP_LTE`, `OP_EQ`, `OP_NEQ` | `">"`, `">="`, `"<"`, `"<="`, `"=="`, `"!="` | `>`, `>=`, `<`, `<=`, `==`, `!=` |
| `LPAREN`, `RPAREN`, `SEMICOLON`, `COMMA` | `"("`, `")"`, `";"`, `","` | `(`, `)`, `;`, `,` |

### Two Competing-Rule Cases:
1. **Relational Operator `>=` vs `>` and `=`:**
   - *Conflict:* In `>= `, both `>` and `>=` could prefix-match.
   - *Resolution:* JFlex **Longest Match (Maximal Munch)** matches 2 chars (`>=`) over 1 char (`>`), producing `OP_GTE`.
2. **Decimal `12.5` vs Integer `12`:**
   - *Conflict:* In `12.5`, `12` matches `{Integer}`.
   - *Resolution:* JFlex matches all 4 characters as `{Decimal}`, emitting a single `DECIMAL` token.

---

## 📽️ Slide 4: Scanner Design & Automaton (DFA)

### End-to-End Scanner Architecture:
```
[ UTF-8 Text Source ]
        │
        ▼
[ JFlex Lexical Analyzer (ArithmeticScanner.flex) ]
  • Macro Definitions: Identifiers, Integers, Decimals
  • Position Tracking (%line, %column)
  • Longest-match DFA State Engine
        │
        ▼
[ Token Stream / Result Model ]
  • Tokens: (Type, Lexeme, Line, Column, Category)
  • Lexical Errors: (Char, Line, Column, Message)
        │
   ┌────┴──────────────────────────┐
   ▼                               ▼
[ Console CLI Runner ]    [ JavaFX Interactive GUI ]
```

### DFA Automaton for `DECIMAL` = `(0 | [1-9][0-9]*)\.[0-9]+`:
- **State S0 (Start):** `'0'` $\to$ S1, `'1'-'9'` $\to$ S2
- **State S1 (Leading zero):** `'.'` $\to$ S3
- **State S2 (Integer digits):** loop on `'0'-'9'`, `'.'` $\to$ S3
- **State S3 (Saw decimal point):** `'0'-'9'` $\to$ S4
- **State S4 (Accepting state for DECIMAL):** loop on `'0'-'9'`

---

## 📽️ Slide 5: Test Evidence & Bug Correction

### Recorded Tests: 16 Cases (100% Pass Rate)
- **6 Normal Inputs (N01–N06):** Basic expressions, precedence parentheses, exponents & modulo, comparisons, multi-statement arithmetic, single & block comments.
- **5 Boundary & Competing Cases (B01–B05):** Empty input file, adjacent operators without spaces (`a>=b>c==d`), decimal vs integer literals, division `/` vs comment `//`, EOF boundary.
- **4 Invalid Inputs (E01–E04):** Unexpected `$`, multiple illegal `#` and `@`, double-dot decimal `3.14.15`, tilde `~` and quotes `"`.
- **1 Bug Correction Test (BUG01):** Dangling decimal point `12. + 5;`.

### Bug Found & Corrected:
- *Initial Bug:* Decimal defined as `{Integer}\.{Digit}*` allowed `12.` with zero fractional digits to be accepted as a Decimal.
- *Fix:* Changed quantifier to `{Integer}\.{Digit}+`. Now `12` is scanned as `INTEGER`, `.` is rejected as a lexical error, and `+` and `5` continue scanning cleanly.

---

## 📽️ Slide 6: Live Demonstration Walkthrough

### Demonstration Steps (1-Click Presets in GUI):
1. **Normal Input (N01):** Run `total = 12.5 + qty * 2;`
   - Point out 1-based line & column positions: `total` (1,1), `=` (1,7), `12.5` (1,9).
   - Show color badges for token categories (Blue, Green, Amber).
2. **Boundary / Competing Input (B02):** Run `a>=b>c==d!=e<=f<g;`
   - Demonstrate how JFlex disambiguates `>=` (OP_GTE) vs `>` (OP_GT) and `==` (OP_EQ).
3. **Invalid Input (E01 & E02):** Run `total = 50 $ 4;`
   - Show error console: `LEXICAL ERROR: Unexpected character '$' at Line 1, Column 12`.
   - Click "Jump to Error" in GUI to immediately highlight the exact position in the editor!
4. **Automaton Tab:**
   - Type `12.5` in the interactive DFA tracer to watch states S0 $\to$ S2 $\to$ S3 $\to$ S4 highlight dynamically.

---

## 📽️ Slide 7: Results, Contributions & Main Lesson

### Summary of Results:
- Fully compliant with Lab 01 project specifications.
- 20 token types recognized, dual-mode CLI and JavaFX GUI execution.
- 16 / 16 automated benchmark tests passing (100%).

### Known Limitations:
- Syntax parsing is intentionally outside scope (e.g. `12 + * 4` produces valid tokens).
- Scientific $E$-notation (`1.5e-3`) is not supported in this basic arithmetic subset.

### Main Lesson Learned:
> "Writing precise regular expressions and leveraging compiler generator principles (Maximal Munch) simplifies lexical analysis, while non-fatal error recovery creates a vastly superior developer experience."
