package com.sabino.klox

import java.util.Optional

internal abstract class Stmt {

  interface Visitor<R> {
    fun visitBlockStmt(expr: Block): R
    fun visitExpressionStmt(expr: Expression): R
    fun visitFunctionStmt(expr: Function): R
    fun visitIfStmt(expr: If): R
    fun visitPrintStmt(expr: Print): R
    fun visitVarStmt(expr: Var): R
    fun visitWhileStmt(expr: While): R
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

    internal class Function(val name: Token, val params: Iterable<Token>, val body: Iterable<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitFunctionStmt(this)
        }
    }

    internal class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Optional<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }

    internal class Print(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    internal class Var(val name: Token, val initializer: Optional<Expr>) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

    internal class While(val condition: Expr, val body: Stmt) : Stmt() {
        override fun <R> accept(visitor: Stmt.Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }

  abstract fun <R> accept(visitor: Visitor<R>): R
}
