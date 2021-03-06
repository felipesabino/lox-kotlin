package com.sabino.lox

import com.sabino.lox.primitives.Clock
import com.sabino.lox.types.*
import java.util.*


internal class Interpreter : Expr.Visitor<Optional<Any>>, Stmt.Visitor<Unit> {

    class RuntimeError(val token: Token, override val message: String) : RuntimeException()

    val globals = Environment()
    private var environment = globals
    private var locals: MutableMap<Expr, Int> = mutableMapOf()

    init {
        globals.define("clock", Optional.of(Clock()))
    }

    fun interpret(statements: Iterable<Stmt>) {
        try {
            statements.forEach { execute(it) }
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Optional<Any> {
        val value: Optional<Any> = evaluate(expr.value)

        val distance = Optional.ofNullable(locals[expr])
        if (distance.isPresent) {
            environment.assignAt(distance.get(), expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
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
                    if (left.filter { v -> v is Double }.isPresent && right.filter { v -> v is Double }.isPresent) {
                        left.map { v -> v as Double }.get() + right.map { v -> v as Double }.get()
                    } else if (left.filter { v -> v is String }.isPresent && right.filter { v -> v is String }.isPresent) {
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

    override fun visitCallExpr(expr: Expr.Call): Optional<Any> {
        val callee = evaluate(expr.callee)

        val arguments = expr.arguments.map { evaluate(it) }

        if (callee.filter { it is LoxCallable }.isPresent.not()) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }

        val function = callee.get() as LoxCallable

        if (arguments.size != function.arity()) {
            throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }

        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Optional<Any> {
        val obj = evaluate(expr.obj)
        if (obj.filter { it is LoxInstance }.isPresent) {
            val instance = obj.get() as LoxInstance
            return instance.get(expr.name)
        }
        throw RuntimeError(expr.name, "Only instances have properties.")
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Optional<Any> {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Optional<Any> {
        return expr.value
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Optional<Any> {
        val left = evaluate(expr.left)

        if (expr.operator.type === TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Optional<Any> {
        val obj = evaluate(expr.obj)
        if (obj.filter { it is LoxInstance }.isPresent.not()) {
            throw RuntimeError(expr.name, "Only instances have properties.")
        }
        val value = evaluate(expr.value)

        val instance = obj.get() as LoxInstance
        instance.set(expr.name, value)

        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Optional<Any> {
        val distance = Optional.ofNullable(locals.get(expr))

        if (distance.isPresent) {
            val superclass = environment.getAt(distance.get(), "super")

            // "this" is always one level nearer than "super"'s environment.
            val instance = environment.getAt(distance.get() - 1, "this")

            val method = superclass.map { it as LoxClass }.get().findMethod(expr.method.lexeme)

            if (instance.filter { it is LoxInstance }.isPresent.not()) {
                throw RuntimeError(expr.keyword, "Could not map class inheritance scope.")
            }

            if (method.isPresent) {
                return Optional.of(method.get().bind(instance.map { it as LoxInstance }.get()))
            } else {
                throw RuntimeError(expr.keyword, "Undefined property ${expr.method.lexeme}'")
            }
        }

        return Optional.empty()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Optional<Any> {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> right.filter { v -> v is Double }.map { v -> -(v as Double) }
            TokenType.BANG -> Optional.of(isTruthy(right).not())
            else -> Optional.empty()
        }
    }

    override fun visitThisExpr(expr: Expr.This): Optional<Any> {
        return lookupVariable(expr.keyword, expr)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Optional<Any> {
        return lookupVariable(expr.name, expr)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {

        var superclass: Optional<Any> = Optional.empty()
        if (stmt.superclass.isPresent) {
            superclass = evaluate(stmt.superclass.get())
            if (superclass.filter { it is LoxClass }.isPresent.not()) {
                throw RuntimeError(stmt.superclass.get().name, "Superclass must be a class.")
            }
        }

        environment.define(stmt.name.lexeme, Optional.empty())

        if (stmt.superclass.isPresent) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods: Map<String, LoxFunction> = stmt.methods
            .associateBy({ it.name.lexeme }, { it })
            .mapValues { LoxFunction(it.value, environment, it.value.name.lexeme == "init") }

        val klass = LoxClass(stmt.name.lexeme, superclass.map { it as LoxClass }, methods)

        if (stmt.superclass.isPresent) {
            environment = environment.enclosing.get()
        }

        environment.assign(stmt.name, Optional.of(klass))
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, Optional.of(function))
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch.isPresent) {
            execute(stmt.elseBranch.get())
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        throw Return(if (stmt.value.isPresent) evaluate(stmt.value.get()) else Optional.empty())
    }

    override fun visitVarStmt(stmt: Stmt.Var) {

        var value: Optional<Any> = Optional.empty()
        if (stmt.initializer.isPresent) {
            value = evaluate(stmt.initializer.get())
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    private fun evaluate(expr: Expr): Optional<Any> {
        return expr.accept(this)
    }

    private fun isTruthy(value: Optional<Any>): Boolean {
        if (value.isPresent.not()) {
            return false
        }
        if (value.get() is Boolean) {
            return value.get() as Boolean
        }
        return true
    }

    private fun isEqual(left: Optional<Any>, right: Optional<Any>): Boolean {
        if (left.isPresent.not() && right.isPresent.not()) {
            return true
        }
        if (left.isPresent.not()) {
            return false
        }
        return left == right
    }

    private fun checkNumberOperands(token: Token, vararg operands: Optional<Any>) {
        val anyCheckFailed = operands.any { op -> op.isPresent.not() || (op.get() is Double).not() }
        if (anyCheckFailed) {
            throw RuntimeError(token, "Operands must be a Number.")
        }
    }

    private fun stringify(value: Optional<Any>): String {
        if (value.isPresent.not()) return "nil"
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

    private fun lookupVariable(token: Token, expr: Expr): Optional<Any> {
        val distance = Optional.ofNullable(locals[expr])
        return if (distance.isPresent) {
            environment.getAt(distance.get(), token)
        } else {
            globals.get(token)
        }
    }

    fun executeBlock(statements: Iterable<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
    }

}