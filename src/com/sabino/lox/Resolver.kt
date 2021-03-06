package com.sabino.lox

import com.sabino.lox.types.*
import java.util.*


internal class Resolver(
    val interpreter: Interpreter
) : Expr.Visitor<Optional<Any>>, Stmt.Visitor<Unit> {

    fun resolve(statements: Iterable<Stmt>) {
        statements.forEach { resolve(it) }
    }

    // key: variable name
    // value: is finished being initialized?
    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()
    private var currentFunctionType = FunctionType.NONE
    private var currentClassType = ClassType.NONE

    override fun visitAssignExpr(expr: Expr.Assign): Optional<Any> {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return Optional.empty()
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Optional<Any> {
        resolve(expr.left)
        resolve(expr.right)
        return Optional.empty()
    }

    override fun visitCallExpr(expr: Expr.Call): Optional<Any> {
        resolve(expr.callee)
        expr.arguments.forEach { resolve(it) }
        return Optional.empty()
    }

    override fun visitGetExpr(expr: Expr.Get): Optional<Any> {
        resolve(expr.obj)
        return Optional.empty()
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Optional<Any> {
        resolve(expr.expression)
        return Optional.empty()
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Optional<Any> {
        return Optional.empty()
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Optional<Any> {
        resolve(expr.left)
        resolve(expr.right)
        return Optional.empty()
    }

    override fun visitSetExpr(expr: Expr.Set): Optional<Any> {
        resolve(expr.obj)
        resolve(expr.value)
        return Optional.empty()
    }

    override fun visitSuperExpr(expr: Expr.Super): Optional<Any> {

        if (currentClassType == ClassType.NONE) {
            Lox.error(expr.keyword, "Cannot use 'super' oustide of a class")
        } else if (currentClassType != ClassType.SUBCLASS) {
            Lox.error(expr.keyword, "Cannot use 'super' in a class with no superclass")
        }

        resolveLocal(expr, expr.keyword)
        return Optional.empty()
    }

    override fun visitThisExpr(expr: Expr.This): Optional<Any> {

        if (currentClassType == ClassType.NONE) {
            Lox.error(expr.keyword, "Cannot use 'this' outside of a class.")
        } else {
            resolveLocal(expr, expr.keyword)
        }
        return Optional.empty()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Optional<Any> {
        resolve(expr.right)
        return Optional.empty()
    }

    override fun visitVariableExpr(expr: Expr.Variable): Optional<Any> {
        if (!scopes.empty() && scopes.peek()[expr.name.lexeme] == false) {
            Lox.error(
                expr.name,
                "Cannot read local variable in its own initializer."
            )
        }
        resolveLocal(expr, expr.name)
        return Optional.empty()
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitClassStmt(stmt: Stmt.Class) {

        val enclosingClasstype = currentClassType
        currentClassType = ClassType.CLASS

        declare(stmt.name)
        define(stmt.name)

        if (stmt.superclass.isPresent &&
            stmt.name.lexeme == stmt.superclass.get().name.lexeme
        ) {
            Lox.error(stmt.superclass.get().name, "A class cannot inherit from itself.")
        }

        if (stmt.superclass.isPresent) {
            currentClassType = ClassType.SUBCLASS
            resolve(stmt.superclass.get())

            beginScope()
            scopes.peek().put("super", true)
        }

        beginScope()

        scopes.peek()["this"] = true
        stmt.methods.forEach {
            val declaration =
                if (it.name.lexeme == "init") FunctionType.INITIALIZER else FunctionType.METHOD
            resolveFunction(it, declaration)
        }

        endScope()
        if (stmt.superclass.isPresent) {
            endScope()
        }

        currentClassType = enclosingClasstype
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch.isPresent) {
            resolve(stmt.elseBranch.get())
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        resolve(stmt.expression)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {

        if (currentFunctionType == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Cannot return from top-level code.")
        }

        if (stmt.value.isPresent) {
            if (currentFunctionType == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword, "Cannot return a value from an initializer.")
            }
            resolve(stmt.value.get())
        }
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        if (stmt.initializer.isPresent) {
            resolve(stmt.initializer.get())
        }
        define(stmt.name)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(token: Token) {
        if (scopes.empty()) return
        scopes.peek()[token.lexeme] = false
    }

    private fun define(token: Token) {
        if (scopes.empty()) return
        scopes.peek()[token.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, token: Token) {
        for (i in scopes.indices.reversed()) {
            if (scopes[i].containsKey(token.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }

        // Not found. Assume it is global.
    }

    private fun resolveFunction(stmt: Stmt.Function, type: FunctionType) {

        val enclosingFunctionType = currentFunctionType
        currentFunctionType = type

        beginScope()

        stmt.params.forEach { param ->
            declare(param)
            define(param)
        }
        resolve(stmt.body)
        endScope()

        currentFunctionType = enclosingFunctionType
    }

}