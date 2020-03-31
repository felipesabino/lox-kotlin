package com.sabino.klox

import java.util.Optional

internal abstract class Expr {

  interface Visitor<R> {
    fun visitAssignExpr(expr: Assign): R
    fun visitBinaryExpr(expr: Binary): R
    fun visitCallExpr(expr: Call): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitLogicalExpr(expr: Logical): R
    fun visitUnaryExpr(expr: Unary): R
    fun visitVariableExpr(expr: Variable): R
  }

    internal class Assign(val name: Token, val value: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    internal class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    internal class Call(val callee: Expr, val paren: Token, val arguments: Iterable<Expr>) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitCallExpr(this)
        }
    }

    internal class Grouping(val expression: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    internal class Literal(val value: Optional<Any>) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    internal class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Expr.Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
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
