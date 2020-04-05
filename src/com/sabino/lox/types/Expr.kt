package com.sabino.lox.types

import java.util.Optional

internal abstract class Expr {

  interface Visitor<R> {
    fun visitAssignExpr(expr: Assign): R
    fun visitBinaryExpr(expr: Binary): R
    fun visitCallExpr(expr: Call): R
    fun visitGetExpr(expr: Get): R
    fun visitGroupingExpr(expr: Grouping): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitLogicalExpr(expr: Logical): R
    fun visitSetExpr(expr: Set): R
    fun visitSuperExpr(expr: Super): R
    fun visitThisExpr(expr: This): R
    fun visitUnaryExpr(expr: Unary): R
    fun visitVariableExpr(expr: Variable): R
  }

    internal class Assign(val name: Token, val value: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    internal class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    internal class Call(val callee: Expr, val paren: Token, val arguments: Iterable<Expr>) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitCallExpr(this)
        }
    }

    internal class Get(val obj: Expr, val name: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGetExpr(this)
        }
    }

    internal class Grouping(val expression: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    internal class Literal(val value: Optional<Any>) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    internal class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
        }
    }

    internal class Set(val obj: Expr, val name: Token, val value: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitSetExpr(this)
        }
    }

    internal class Super(val keyword: Token, val method: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitSuperExpr(this)
        }
    }

    internal class This(val keyword: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitThisExpr(this)
        }
    }

    internal class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    internal class Variable(val name: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

  abstract fun <R> accept(visitor: Visitor<R>): R
}
