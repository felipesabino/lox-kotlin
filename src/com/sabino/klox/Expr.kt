package com.sabino.klox;

internal abstract class Expr {

  interface Visitor<R> {
    fun visitBinaryExpr(expr: Binary): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitUnaryExpr(expr: Unary): R
    fun visitVariableExpr(expr: Variable): R
  }

    internal class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    internal class Grouping(val expression: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    internal class Literal(val value: Any?) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    internal class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    internal class Variable(val name: Token) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

  abstract fun <R> accept(visitor: Visitor<R>): R
}
