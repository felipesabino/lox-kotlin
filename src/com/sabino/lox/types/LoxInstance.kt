package com.sabino.lox.types

import com.sabino.lox.Interpreter
import java.util.*

internal class LoxInstance(private val klass: LoxClass) {

    private val fields: MutableMap<String, Optional<Any>> = mutableMapOf()

    fun get(name: Token): Optional<Any> {

        if (fields.containsKey(name.lexeme)) {
            return fields.getOrDefault(name.lexeme, Optional.empty())
        }

        val method = klass.findMethod(name.lexeme)
        if (method.isPresent) { return method.map { it as Any } }

        throw Interpreter.RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Optional<Any>) {
        fields[name.lexeme] = value
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}