package com.sabino.klox

import com.sabino.klox.Expr.Literal
import com.sabino.klox.TokenType.*


internal class Parser(val tokens: List<Token>) {

    internal class ParserError: RuntimeException {
        constructor(): super()
        constructor(message: String, ex: Exception?): super(message, ex)
        constructor(message: String): super(message)
        constructor(ex: Exception): super(ex)
    }

    /*
        expression     → equality ;
        equality       → comparison ( ( "!=" | "==" ) comparison )* ;
        comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
        addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
        multiplication → unary ( ( "/" | "*" ) unary )* ;
        unary          → ( "!" | "-" ) unary
                       | primary ;
        primary        → NUMBER | STRING | "false" | "true" | "nil"
                       | "(" expression ")" ;
    */

    /*
        Terminal	    Code to match and consume a token
        Nonterminal	    Call to that rule’s function
        |	            If or switch statement
        * or +	        While or for loop
        ?	            If statement
     */

    private var current = 0

    // expression     → equality ;
    private fun expression(): Expr {
        return equality()
    }

    // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private  fun equality() : Expr {
        var expr: Expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    // comparison     → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
    private fun comparison(): Expr {
        var expr: Expr = addition()
        while (match(TokenType.GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right: Expr = addition()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // addition       → multiplication ( ( "-" | "+" ) multiplication )* ;
    private fun addition(): Expr {
        var expr: Expr = multiplication()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right: Expr = multiplication()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // multiplication → unary ( ( "/" | "*" ) unary )* ;
    private fun multiplication(): Expr {
        var expr: Expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    // unary          → ( "!" | "-" ) unary
    //                | primary ;
    private fun unary(): Expr {
        if (match(TokenType.BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    // primary        → NUMBER | STRING | "false" | "true" | "nil"
    // | "(" expression ")" ;
    private fun primary(): Expr {
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr);
        }
        throw error(peek(), "Expected expression")
    }

    private fun consume(type: TokenType, message: String): Token {
        if(check(type)) return advance()

        throw error(peek(), message)
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type === type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type === EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun error(token: Token, message: String): Parser.ParserError {
        Klox.error(token, message)
        return ParserError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type === SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
            }
            advance()
        }
    }
}