package com.sabino.klox

import java.util.*


internal class Interpreter : Expr.Visitor<Optional<Any>>, Stmt.Visitor<Unit> {

    class RuntimeError(val token: Token, override val message: String): RuntimeException() { }


    fun interpret(statements: Sequence<Stmt>) {
        try {
            statements.forEach { execute(it) }
        } catch (error: RuntimeError) {
            Klox.runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Optional<Any> {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return Optional.ofNullable(
            when (expr.operator.type) {
                TokenType.GREATER -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left.get() as Double) > (right.get() as Double)
                }
                TokenType.GREATER_EQUAL -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left.get() as Double) >= (right.get() as Double)
                }
                TokenType.LESS -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left.get() as Double) < (right.get() as Double)
                }
                TokenType.LESS_EQUAL -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left.get() as Double) <= (right.get() as Double)
                }
                TokenType.MINUS -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left.get() as Double) - (right.get() as Double)
                }
                TokenType.SLASH -> {
                    checkNumberOperands(expr.operator, left, right)
                    if ((right.get() as Double) == 0.0) { //how does kotlin handle double comparison?
                        throw RuntimeError(expr.operator, "Can not divide by zero")
                    }
                    (left.get() as Double) / (right.get() as Double)
                }
                TokenType.STAR -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left.get() as Double) * (right.get() as Double)
                }
                TokenType.PLUS -> {
                    if (left.filter { v -> v is Double}.isPresent && right.filter { v -> v is Double}.isPresent) {
                        left.map { v -> v as Double }.get() + right.map { v -> v as Double }.get()
                    } else if (left.filter { v -> v is String}.isPresent && right.filter { v -> v is String}.isPresent) {
                        StringBuilder()
                            .append(left.map { v -> v as String }.get())
                            .append(right.map { v -> v as String }.get())
                            .toString()
                    } else {
                        throw RuntimeError(expr.operator, "Operand must be a both Strings or Numbers.")
                    }
                }
                TokenType.BANG_EQUAL -> isEqual(left, right).not()
                TokenType.EQUAL_EQUAL -> isEqual(left, right)
                else -> null
            }
        )
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Optional<Any> {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Optional<Any> {
        return Optional.ofNullable(expr.value)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Optional<Any> {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> right.filter{ v -> v is Double }.map { v -> - (v as Double) }
            TokenType.BANG -> Optional.of(isTruthy(right).not())
            else -> Optional.empty()
        }
    }

    private fun evaluate(expr: Expr): Optional<Any> {
        return expr.accept(this);
    }

    private fun isTruthy(value: Optional<Any>): Boolean {
        if (value.isPresent.not()) { return false }
        if (value.get() is Boolean) { return value.get() as Boolean }
        return true
    }

    private fun isEqual(left: Optional<Any>, right: Optional<Any>): Boolean {
        if (left.isPresent.not() && right.isPresent.not()) { return true }
        if (left.isPresent.not()) { return false }
        return left == right
    }

    private fun checkNumberOperands(token: Token, vararg operands: Optional<Any>) {
        val anyCheckFailed = operands.any { op -> op.isPresent.not() || (op.get() is Double).not() }
        if (anyCheckFailed) {
            throw RuntimeError(token, "Operands must be a Number.")
        }
    }

    private fun stringify(value: Optional<Any>): String {
        if (value.isPresent.not() || value.get() == null) return "nil"
        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (value.get() is Double) {
            var text = value.get().toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return value.get().toString()
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }
}