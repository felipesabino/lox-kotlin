package com.sabino.klox

import com.sabino.klox.Interpreter.RuntimeError
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
            return values.getOrElse(token.lexeme, { throw undefinedValiableError(token) })
        }

        if (enclosing.isPresent) { return enclosing.get().get(token) }

        throw undefinedValiableError(token)
    }

    fun assign(token: Token, value: Optional<Any>) {

        if (values.containsKey(token.lexeme)) {
            values[token.lexeme] = value
        } else if (enclosing.isPresent) {
            enclosing.get().assign(token, value)
        } else {
            throw undefinedValiableError(token)
        }
    }

    @Throws
    private fun undefinedValiableError(name: Token): RuntimeError {
        return RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }
}