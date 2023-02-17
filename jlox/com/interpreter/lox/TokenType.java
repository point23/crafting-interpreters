package com.interpreter.lox;

enum TokenType {
    // Single-character tokens
    LEFT_PAREN,     // (
    RIGHT_PAREN,    // _
    LEFT_BRACE,     // {
    RIGHT_BRACE,    // }
    COMMA,          // ,
    DOT,            // .
    MINUS,          // -
    PLUS,           // +
    SEMICOLON,      // ;
    COLON,          // :
    SLASH,          // _
    STAR,           // *

    // Related one or two character tokens
    BANG,           // !
    BANG_EQUAL,     // !=
    EQUAL,          // =
    EQUAL_EQUAL,    // ==
    GREATER,        // >
    GREATER_EQUAL,  // >=
    LESS,           // <
    LESS_EQUAL,     // <=

    IDENTIFIER,

    // Literals
    STRING,
    NUMBER,

    // Keywords
    AND,
    CLASS,
    ELSE,
    FALSE,
    FOR,
    FUN,
    IF,
    NIL,
    OR,
    PRINT,
    RETURN,
    SUPER,
    THIS,
    TRUE,
    VAR,
    WHILE,
    CONTINUE,

    EOF,
}