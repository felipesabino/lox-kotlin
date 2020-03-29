package com.sabino.klox;

internal abstract class Stmt {

  interface Visitor<R> {
    fun visitBlockStmt(expr: Block): R
    fun visitExpressionStmt(expr: Expression): R
    fun visitPrintStmt(expr: Print): R
    fun visitVarStmt(expr: Var): R
  }

    internal class Block(val statements: Iterable<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    internal class Expression(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }

    internal class Print(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    internal class Var(val name: Token, val initializer: Expr?) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

  abstract fun <R> accept(visitor: Visitor<R>): R
}
