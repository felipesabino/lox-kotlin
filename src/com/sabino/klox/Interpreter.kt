package com.sabino.klox

import java.lang.StringBuilder
import java.util.Optional

internal class Interpreter : Expr.Visitor<Optional<Any>> {

    override fun visitBinaryExpr(expr: Expr.Binary): Optional<Any> {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return Optional.ofNullable(
            when (expr.operator.type) {
                TokenType.MINUS -> left.get() - right.get() //what if it is not a number or any value is null?
                TokenType.SLASH -> left.get() / right.get()
                TokenType.STAR -> left.get() * right.get()
                TokenType.PLUS -> {
                    if (left.filter { v -> v is Double}.isPresent && right.filter { v -> v is Double}.isPresent) {
                        left.map { v -> v as Double }.get() + right.map { v -> v as Double }.get()
                    } else if (left.filter { v -> v is String}.isPresent && right.filter { v -> v is String}.isPresent) {
                        StringBuilder()
                            .append(left.map { v -> v as String }.get())
                            .append(right.map { v -> v as String }.get())
                            .toString()
                    } else {
                        null
                    }
                }
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
}