package com.sabino.lox.types

import com.sabino.lox.Environment
import com.sabino.lox.Interpreter
import java.util.*

internal class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean
) : LoxCallable {

    override fun arity(): Int {
        return declaration.params.count() // TODO: should use other collection class instead to avoid O(n) here?
    }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        val environment = Environment(closure)

        val argumentsList = arguments.toList() // TODO: how to avoid this and loop both iterables synchronously?
        declaration.params.forEachIndexed { index, item ->
            @Suppress("UNCHECKED_CAST")
            environment.define(item.lexeme, argumentsList[index] as Optional<Any>)
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return if (isInitializer) closure.getAt(0, "this")
            else returnValue.value
        }

        if (isInitializer) return closure.getAt(0, "this")
        return Optional.empty()
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", Optional.of(instance))
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}