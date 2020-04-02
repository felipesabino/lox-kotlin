package com.sabino.lox.types

import com.sabino.lox.Environment
import com.sabino.lox.Interpreter
import java.util.*

internal class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment
): LoxCallable {

    override fun arity(): Int {
        return declaration.params.count() // TODO: should use other collection class instead to avoid O(n) here?
    }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        val environment = Environment(closure)

        val argumentsList = arguments.toList() // TODO: how to avoid this and loop both iterables synchronously?
        declaration.params.forEachIndexed {
                index, item ->
            @Suppress("UNCHECKED_CAST")
            environment.define(item.lexeme, argumentsList[index] as Optional<Any>)
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }

        return Optional.empty()
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}