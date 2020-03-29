package com.sabino.klox

import com.sabino.klox.Expr.Assign
import com.sabino.klox.Expr.Literal
import com.sabino.klox.TokenType.*
import java.util.*


internal class Parser(val tokens: List<Token>) {

    internal class ParserError: RuntimeException {
        constructor(): super()
        constructor(message: String, ex: Exception?): super(message, ex)
        constructor(message: String): super(message)
        constructor(ex: Exception): super(ex)
    }

    // program         → declaration* EOF ;
    fun parse() = sequence {
        while (!isAtEnd()) {
            val stmt = declaration()
            if (stmt.isPresent) yield(stmt.get())
        }
    }.asIterable()



    /*

        program         → declaration* EOF ;

        declaration     → varDecl
                        | statement ;

        varDecl         → "var" IDENTIFIER ( "=" expression )? ";" ;

        statement       → exprStmt
                        | printStmt ;

        exprStmt        → expression ";" ;
        printStmt       → "print" expression ";" ;

        expression      → assignment ;
        assignment      → IDENTIFIER "=" assignment
                        | equality ;

        equality        → comparison ( ( "!=" | "==" ) comparison )* ;
        comparison      → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
        addition        → multiplication ( ( "-" | "+" ) multiplication )* ;
        multiplication  → unary ( ( "/" | "*" ) unary )* ;
        unary           → ( "!" | "-" ) unary
                        | primary ;
        primary         → "false" | "true" | "nil"
                        | NUMBER | STRING |
                        | "(" expression ")"
                        | IDENTIFIER;
    */

    /*
        Terminal	    Code to match and consume a token
        Nonterminal	    Call to that rule’s function
        |	            If or switch statement
        * or +	        While or for loop
        ?	            If statement
     */

    private var current = 0
    // declaration     → varDecl
    //                 | statement ;
    private fun declaration(): Optional<Stmt> {
        return Optional.ofNullable(try {
            if (match(VAR)) varDeclaration()
            else statement()
        } catch (e: ParserError) {
            synchronize()
            null
        })
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect '; after variable declaration")
        return Stmt.Var(name, initializer)
    }

    // statement → exprStmt | printStmt ;
    private fun statement(): Stmt {
        return if (match(TokenType.PRINT)) {
            printStatement()
        } else {
            expressionStatement()
        }
    }

    // printStmt → "print" expression ";" ;
    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Stmt.Print(value)
    }

    // exprStmt  → expression ";" ;
    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Stmt.Expression(expr)
    }

    // expression     → assignment ;
    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = equality()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
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

    // primary         → "false" | "true" | "nil"
    //                | NUMBER | STRING |
    //                | "(" expression ")"
    //                | IDENTIFIER;
    private fun primary(): Expr {
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr);
        }
        throw error(peek(), "Expected expression")
    }

    /**
     * Verifies if next token matches a type,
     * throws if not
     * {@see advance} if it does
     *
     * @param type: token type to match
     * @param message: error message added to exception raised when it does not match
     * @return Token after matching
     *
     */
    private fun consume(type: TokenType, message: String): Token {
        if(check(type)) return advance()

        throw error(peek(), message)
    }

    /**
     * Verifies if next token matches a set of types
     * If true, it also {@see advance}
     *
     * @param types: types of token to match\
     * @return Returns result of matching
     */
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