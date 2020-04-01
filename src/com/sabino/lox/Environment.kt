package com.sabino.lox

import com.sabino.lox.Interpreter.RuntimeError
import java.util.*


internal class Environment {
    private val values = mutableMapOf<String, Optional<Any>>()
    private val enclosing: Optional<Environment>

    constructor() { this.enclosing = Optional.empty() }
    constructor(enclosing: Environment) { this.enclosing = Optional.of(enclosing) }

    fun define(name: String, value: Optional<Any>) {
        values[name] = value
    }

    fun get(token: Token): Optional<Any> {

        if (values.containsKey(token.lexeme)) {
            return values.getOrElse(token.lexeme, { fail(token) })
        }

        if (enclosing.isPresent) { return enclosing.get().get(token) }

        fail(token)
    }

    fun assign(token: Token, value: Optional<Any>) : Unit = when {
        values.containsKey(token.lexeme) -> values[token.lexeme] = value
        enclosing.isPresent -> enclosing.get().assign(token, value)
        else -> fail(token)
    }

    @Throws
    private fun fail(name: Token): Nothing {
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}