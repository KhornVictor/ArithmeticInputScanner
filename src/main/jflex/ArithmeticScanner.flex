package com.compiler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.compiler.model.Token;
import com.compiler.model.TokenType;
import com.compiler.model.LexicalError;

%%

%class ArithmeticScanner
%public
%unicode
%line
%column
%type Token

%{
  private final List<LexicalError> errors = new ArrayList<>();

  public List<LexicalError> getErrors() {
    return errors;
  }

  public void resetErrors() {
    errors.clear();
  }

  private Token token(TokenType type) {
    // 1-based indexing for line and column positions
    return new Token(type, yytext(), yyline + 1, yycolumn + 1);
  }

  private Token errorToken() {
    String lexeme = yytext();
    int line = yyline + 1;
    int col = yycolumn + 1;
    String message = "Unexpected character '" + lexeme + "'";
    LexicalError err = new LexicalError(lexeme, line, col, message);
    errors.add(err);
    return new Token(TokenType.ERROR, lexeme, line, col, message);
  }

  /**
   * Scans the entire stream and returns all tokens until EOF.
   * Scanning continues through errors so all tokens and errors are collected.
   */
  public List<Token> scanAll() throws IOException {
    List<Token> tokenList = new ArrayList<>();
    Token tok;
    while ((tok = yylex()) != null) {
      tokenList.add(tok);
    }
    return tokenList;
  }
%}

/* Macro Definitions */
LineTerminator     = \r|\n|\r\n
Whitespace         = {LineTerminator} | [ \t\f]
InputCharacter     = [^\r\n]

/* Comments: Single-line and Multi-line */
SingleLineComment  = "//" {InputCharacter}*
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
Comment            = {SingleLineComment} | {TraditionalComment}

/* Identifiers & Numbers */
Letter             = [a-zA-Z_]
Digit              = [0-9]
Identifier         = {Letter} ({Letter} | {Digit})*
Integer            = 0 | [1-9]{Digit}*
Decimal            = {Integer} \. {Digit}+

%%

/* 1. Skip Whitespace and Comments */
{Whitespace}       { /* Skip whitespace cleanly */ }
{Comment}          { /* Skip comments cleanly */ }

/* 2. Relational / Comparison Operators (Longest match prevents confusion with '=' or '>') */
">="               { return token(TokenType.OP_GTE); }
"<="               { return token(TokenType.OP_LTE); }
"=="               { return token(TokenType.OP_EQ); }
"!="               { return token(TokenType.OP_NEQ); }
">"                { return token(TokenType.OP_GT); }
"<"                { return token(TokenType.OP_LT); }

/* 3. Assignment and Arithmetic Operators */
"="                { return token(TokenType.ASSIGN); }
"+"                { return token(TokenType.OP_PLUS); }
"-"                { return token(TokenType.OP_MINUS); }
"*"                { return token(TokenType.OP_MUL); }
"/"                { return token(TokenType.OP_DIV); }
"%"                { return token(TokenType.OP_MOD); }
"^"                { return token(TokenType.OP_POW); }

/* 4. Delimiters & Punctuation */
"("                { return token(TokenType.LPAREN); }
")"                { return token(TokenType.RPAREN); }
";"                { return token(TokenType.SEMICOLON); }
","                { return token(TokenType.COMMA); }

/* 5. Literals and Identifiers (Longest match prefers Decimal over Integer) */
{Decimal}          { return token(TokenType.DECIMAL); }
{Integer}          { return token(TokenType.INTEGER); }
{Identifier}       { return token(TokenType.IDENTIFIER); }

/* 6. Error Handling: Any character not matched above is reported, scanning continues */
[^]                { return errorToken(); }
