package com.interpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.interpreter.lox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private int start = 0;   // The start pos of the consuming lexeme.
    private int current = 0; // Current read cursor.
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        keywords.put("continue", CONTINUE);
    }

    Scanner (String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        // Well this *switch* stuff reminds me that we should have a better solution in CLox.

        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch(c) {
            // single-character lexemes
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ':': addToken(COLON); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            // single-character lexemes
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;

            // @fixme
            // - We should treat the newlines differently in File-run mode and Prompt-run mode.
            // - There should support stuff like '^' as a newline in Prompt-run mode.
            // Division or Comment
            case '/':
                if (match('/')) { // If it's a line-comment, consume the whole line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) { // Block comments
                    blockComment();
                } else {
                    addToken(SLASH);
                }
                break;

            // Other meaningless characters
            case ' ':
            case '\r':
            case '\t':
                break;

            // Finished current line.
            case '\n': line++; break;

            // Literals
            case '"': string(); break;

            default: {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character");
                }
            } break;
        }
    }

    private char advance() {
        // Consume the current character

        return source.charAt(current++);
    }

    private boolean match(char c) {
        // Conditional advance, consume the current character only when it matches.

        if (isAtEnd()) return false;
        if (source.charAt(current) != c) {
            return false;
        }

        current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }


    // @note character consumers
    private char peek() { // Lookahead
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void string() {
        // Consume a string, from a " til another ".
        // We support multi-line strings.

        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance(); // Consume the second quote

        String value = source.substring(start + 1, current - 1); // Get rid of those quotes
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance(); // Consume the integer part

        if (peek() == '.' && isDigit(peekNext())) { // Consume the fractal part
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void blockComment() {
        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
            if (peek() == '\n') {
                line += 1;
            }
            advance();
        }

        if (!isAtEnd()) {
            match('*');
            match('/');
        }
    }

    // @note Conditional statements...
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
