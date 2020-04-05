package com.sabino.lox

import com.sabino.lox.types.Expr
import com.sabino.lox.types.Expr.Literal
import com.sabino.lox.types.Stmt
import com.sabino.lox.types.Token
import com.sabino.lox.types.TokenType
import com.sabino.lox.types.TokenType.*
import java.util.*


internal class Parser(private val tokens: List<Token>) {

    internal class ParserError : RuntimeException {
        constructor() : super()
    }

    /*

        program         → declaration* EOF ;

        declaration     → classDecl
                        | funDecl
                        | varDecl
                        | statement ;

        classDecl       → "class" IDENTIFIER ( "<" IDENTIFIER )? "{" function* "}" ;
        funDecl         → "fun" function ;
        function        → IDENTIFIER "(" parameters? ")" block ;
        parameters      → IDENTIFIER ( "," IDENTIFIER )* ;

        varDecl         → "var" IDENTIFIER ( "=" expression )? ";" ;

        statement       → exprStmt
                        | forStmt
                        | ifStmt
                        | printStmt
                        | returnStmt
                        | whileStmt
                        | block ;

        exprStmt        → expression ";" ;
        forStmt         → "for" "(" ( varDecl | exprStmt | ";" )
                        expression? ";"
                        expression? ")" statement ;
        ifStmt          → "if" "(" expression ")" statement ( "else" statement )? ;
        printStmt       → "print" expression ";" ;
        returnStmt      → "return" expression? ";" ;
        whileStmt       → "while" "(" expression ")" statement ;
        block           → "{" declaration* "}" ;

        expression      → assignment ;
        assignment      → ( call "." )? IDENTIFIER "=" assignment
                        | logic_or;

        logic_or        → logic_and ( "or" logic_and )* ;
        logic_and       → equality ( "and" equality )* ;

        equality        → comparison ( ( "!=" | "==" ) comparison )* ;
        comparison      → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
        addition        → multiplication ( ( "-" | "+" ) multiplication )* ;
        multiplication  → unary ( ( "/" | "*" ) unary )* ;
        unary           → ( "!" | "-" ) unary | call ;

        call            → primary ( "(" arguments? ")" | "." IDENTIFIER )* ;
        arguments       → expression ( "," expression )* ;

        primary         → "false" | "true" | "nil"
                        | NUMBER | STRING | IDENTIFIER
                        | "(" expression ")"
                        | "super" "." IDENTIFIER;
    */

    /*
        Terminal	    Code to match and consume a token
        Nonterminal	    Call to that rule’s function
        |	            If or switch statement
        * or +	        While or for loop
        ?	            If statement
     */

    private var current = 0

    fun parse() = sequence {
        while (isAtEnd().not()) {
            val stmt = declaration()
            if (stmt.isPresent) yield(stmt.get())
        }
    }.asIterable()

    private fun declaration(): Optional<Stmt> {
        return Optional.ofNullable(
            try {
                when {
                    match(CLASS) -> classDeclaration()
                    match(FUN) -> function("function")
                    match(VAR) -> varDeclaration()
                    else -> statement()
                }
            } catch (e: ParserError) {
                synchronize()
                null
            }
        )
    }

    private fun classDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expected class name")

        var superclass: Optional<Expr.Variable> = Optional.empty()
        if (match(LESS)) {
            consume(IDENTIFIER, "Expected superclass name")
            superclass = Optional.of(Expr.Variable(previous()))
        }

        consume(LEFT_BRACE, "Expected '{' before class body'")

        val methods: MutableList<Stmt.Function> = mutableListOf()
        while (!check(RIGHT_BRACE) && isAtEnd().not()) {
            methods.add(function("method"))
        }

        consume(RIGHT_BRACE, "Expected '}' after class body")
        return Stmt.Class(name, superclass, methods)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name")

        var initializer: Optional<Expr> = Optional.empty()
        if (match(EQUAL)) {
            initializer = Optional.of(expression())
        }

        consume(SEMICOLON, "Expect '; after variable declaration")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        return when {
            match(FOR) -> {
                forStatement()
            }
            match(IF) -> {
                ifStatement()
            }
            match(PRINT) -> {
                printStatement()
            }
            match(RETURN) -> {
                returnStatement()
            }
            match(WHILE) -> {
                whileStatement()
            }
            match(LEFT_BRACE) -> {
                Stmt.Block(block())
            }
            else -> {
                expressionStatement()
            }
        }
    }

    private fun forStatement(): Stmt {
        /*

        {
          initializer
          while (condition) {
            body
            increment
          }
        }

         */


        consume(LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer = when {
            match(SEMICOLON) -> Optional.empty()
            match(VAR) -> Optional.of(varDeclaration())
            else -> Optional.of(expressionStatement())
        }

        var condition: Optional<Expr> = Optional.empty()
        if (!check(SEMICOLON)) {
            condition = Optional.of(expression())
        }
        consume(SEMICOLON, "Expect ';' after loop condition.")

        var increment: Optional<Expr> = Optional.empty()
        if (!check(RIGHT_PAREN)) {
            increment = Optional.of(expression())
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()

        if (increment.isPresent) {
            body = Stmt.Block(
                listOf(
                    body,
                    Stmt.Expression(increment.get())
                )
            )
        }

        if (condition.isPresent.not()) {
            condition = Optional.of(Literal(Optional.of(true)))
        }
        body = Stmt.While(condition.get(), body)

        if (initializer.isPresent) {
            body = Stmt.Block(
                listOf(
                    initializer.get(),
                    body
                )
            )
        }

        return body

    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Stmt.Print(value)
    }

    private fun returnStatement(): Stmt {
        val keyword = previous()

        var value: Optional<Expr> = Optional.empty()
        if (!check(SEMICOLON)) {
            value = Optional.of(expression())
        }

        consume(SEMICOLON, "Expect ';' after return value.")
        return Stmt.Return(keyword, value)
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after value")
        return Stmt.Expression(expr)
    }

    private fun function(kind: String): Stmt.Function {
        val name = consume(IDENTIFIER, "Expected $kind name")

        consume(LEFT_PAREN, "Expect '(' after $kind name.")

        val parameters: MutableList<Token> = mutableListOf()
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size >= 255) {
                    error(peek(), "Cannot have more than 255 parameters.")
                }
                parameters.add(consume(IDENTIFIER, "Expect parameter name."))
            } while (match(COMMA))
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.")

        consume(LEFT_BRACE, "Expect '{' before $kind body.")

        val body = block()
        return Stmt.Function(name, parameters, body)

    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        return Stmt.While(condition, body)
    }

    private fun block(): Iterable<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (check(RIGHT_BRACE).not() && isAtEnd().not()) {
            val stmt = declaration()
            if (stmt.isPresent) statements.add(stmt.get())
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        var elseBranch: Optional<Stmt> = Optional.empty()
        if (match(ELSE)) {
            elseBranch = Optional.of(statement())
        }
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = or()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            } else if (expr is Expr.Get) {
                val get = expr as Expr.Get
                return Expr.Set(get.obj, get.name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()

        while (match(OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()

        while (match(AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr: Expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator: Token = previous()
            val right: Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr: Expr = addition()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right: Expr = addition()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun addition(): Expr {
        var expr: Expr = multiplication()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right: Expr = multiplication()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun multiplication(): Expr {
        var expr: Expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        return call()
    }

    private fun call(): Expr {
        var expr: Expr = primary()
        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(DOT)) {
                val name = consume(IDENTIFIER, "Expect property name after '.'.")
                expr = Expr.Get(expr, name)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments: MutableList<Expr> = mutableListOf()
        if (check(RIGHT_PAREN).not()) {
            do {
                if (arguments.count() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Literal(Optional.of(false))
        if (match(TRUE)) return Literal(Optional.of(true))
        if (match(NIL)) return Literal(Optional.empty())

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(SUPER)) {
            val keyword = previous()
            consume(DOT, "Expect '.' after super.")
            val method = consume(IDENTIFIER, "Expect superclass method name.")
            return Expr.Super(keyword, method)
        }

        if (match(THIS)) {
            return Expr.This(previous())
        }

        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
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
        if (check(type)) return advance()

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

    private fun error(token: Token, message: String): ParserError {
        Lox.error(token, message)
        return ParserError()
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type === SEMICOLON) return
            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> {
                }
            }
            advance()
        }
    }
}