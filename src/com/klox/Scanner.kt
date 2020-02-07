package com.klox

import com.klox.TokenType.*


internal class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = ArrayList()
    private var start = 0;
    private var current = 0;
    private var line = 1;

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) { // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        val c = advance();
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) { BANG_EQUAL } else { BANG })
            '=' -> addToken(if (match('=')) { EQUAL_EQUAL } else { EQUAL })
            '<' -> addToken(if (match('=')) { LESS_EQUAL } else { LESS })
            '>' -> addToken(if (match('=')) { GREATER_EQUAL } else { GREATER })
            '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(SLASH)
                }
            }
            ' ', '\r', '\t' -> { }

            '\n' -> line++
            '"' -> string()
            else -> Klox.error(line, "Unexpected character '$c'.");
        }
    } 

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        // Unterminated string.
        if (isAtEnd()) {
            Klox.error(line, "Unterminated string.")
            return
        }
        // The closing ".
        advance()
        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }
}