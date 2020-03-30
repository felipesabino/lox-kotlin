package com.sabino.klox

import com.sabino.klox.Expr
import kotlin.math.exp

internal class AstPrinter : Expr.Visitor<String> {

    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.map { it.toString() }.orElse("nil")
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder
            .append("(")
            .append(name)

        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }

        builder.append(")")
        return builder.toString()
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return "(var ${expr.name.lexeme} ${expr.name.literal})"
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return parenthesize("assign ${expr.name}", expr.value)
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }
}