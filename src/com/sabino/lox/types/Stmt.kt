package com.sabino.lox.types

import java.util.Optional

internal abstract class Stmt {

  interface Visitor<R> {
    fun visitBlockStmt(stmt: Block): R
    fun visitClassStmt(stmt: Class): R
    fun visitExpressionStmt(stmt: Expression): R
    fun visitFunctionStmt(stmt: Function): R
    fun visitIfStmt(stmt: If): R
    fun visitPrintStmt(stmt: Print): R
    fun visitReturnStmt(stmt: Return): R
    fun visitVarStmt(stmt: Var): R
    fun visitWhileStmt(stmt: While): R
  }

    internal class Block(val statements: Iterable<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    internal class Class(val name: Token, val methods: Iterable<Stmt.Function>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitClassStmt(this)
        }
    }

    internal class Expression(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }

    internal class Function(val name: Token, val params: Iterable<Token>, val body: Iterable<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFunctionStmt(this)
        }
    }

    internal class If(val condition: Expr, val thenBranch: Stmt, val elseBranch: Optional<Stmt>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }

    internal class Print(val expression: Expr) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    internal class Return(val keyword: Token, val value: Optional<Expr>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitReturnStmt(this)
        }
    }

    internal class Var(val name: Token, val initializer: Optional<Expr>) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

    internal class While(val condition: Expr, val body: Stmt) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }

  abstract fun <R> accept(visitor: Visitor<R>): R
}
