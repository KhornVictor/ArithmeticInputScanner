package com.compiler.model;

/**
 * Enumeration of all token types recognized by the Arithmetic Input Scanner.
 */
public enum TokenType {
    // Identifiers and Literals
    IDENTIFIER("IDENTIFIER", "Identifier", Category.IDENTIFIER),
    INTEGER("INTEGER", "Integer Literal", Category.LITERAL),
    DECIMAL("DECIMAL", "Decimal Literal", Category.LITERAL),

    // Assignment Operators
    ASSIGN("ASSIGN", "Assignment Operator (=)", Category.OPERATOR),

    // Arithmetic Operators
    OP_PLUS("OP_PLUS", "Addition Operator (+)", Category.OPERATOR),
    OP_MINUS("OP_MINUS", "Subtraction Operator (-)", Category.OPERATOR),
    OP_MUL("OP_MUL", "Multiplication Operator (*)", Category.OPERATOR),
    OP_DIV("OP_DIV", "Division Operator (/)", Category.OPERATOR),
    OP_MOD("OP_MOD", "Modulo Operator (%)", Category.OPERATOR),
    OP_POW("OP_POW", "Exponentiation Operator (^)", Category.OPERATOR),

    // Relational / Comparison Operators
    OP_GT("OP_GT", "Greater Than (>)", Category.COMPARISON),
    OP_GTE("OP_GTE", "Greater Than or Equal (>=)", Category.COMPARISON),
    OP_LT("OP_LT", "Less Than (<)", Category.COMPARISON),
    OP_LTE("OP_LTE", "Less Than or Equal (<=)", Category.COMPARISON),
    OP_EQ("OP_EQ", "Equal To (==)", Category.COMPARISON),
    OP_NEQ("OP_NEQ", "Not Equal To (!=)", Category.COMPARISON),

    // Delimiters & Punctuation
    LPAREN("LPAREN", "Left Parenthesis", Category.DELIMITER),
    RPAREN("RPAREN", "Right Parenthesis", Category.DELIMITER),
    SEMICOLON("SEMICOLON", "Semicolon", Category.DELIMITER),
    COMMA("COMMA", "Comma", Category.DELIMITER),

    // Special / Error
    ERROR("ERROR", "Lexical Error", Category.ERROR);

    public enum Category {
        IDENTIFIER("Identifier", "#3b82f6"),
        LITERAL("Literal", "#10b981"),
        OPERATOR("Arithmetic Operator", "#f59e0b"),
        COMPARISON("Comparison Operator", "#8b5cf6"),
        DELIMITER("Delimiter", "#64748b"),
        ERROR("Lexical Error", "#ef4444");

        private final String label;
        private final String colorHex;

        Category(String label, String colorHex) {
            this.label = label;
            this.colorHex = colorHex;
        }

        public String getLabel() {
            return label;
        }

        public String getColorHex() {
            return colorHex;
        }
    }

    private final String typeName;
    private final String description;
    private final Category category;

    TokenType(String typeName, String description, Category category) {
        this.typeName = typeName;
        this.description = description;
        this.category = category;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
