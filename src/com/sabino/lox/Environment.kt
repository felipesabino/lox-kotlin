package com.sabino.lox

import com.sabino.lox.Interpreter.RuntimeError
import com.sabino.lox.types.Token
import java.util.*


internal class Environment {
    private val values = mutableMapOf<String, Optional<Any>>()
    internal val enclosing: Optional<Environment>

    constructor() {
        this.enclosing = Optional.empty()
    }

    constructor(enclosing: Environment) {
        this.enclosing = Optional.of(enclosing)
    }

    fun define(name: String, value: Optional<Any>) {
        values[name] = value
    }

    fun get(token: Token): Optional<Any> {
        return get(token.lexeme)
    }

    fun get(name: String): Optional<Any> {

        if (values.containsKey(name)) {
            return values.getOrElse(name, { fail(name) })
        }

        if (enclosing.isPresent) {
            return enclosing.get().get(name)
        }

        fail(name)
    }

    fun getAt(distance: Int, token: Token): Optional<Any> {
        return getAt(distance, token.lexeme)
    }

    fun getAt(distance: Int, name: String): Optional<Any> {
        val environment = ancestor(distance)
        if (environment.isPresent) {
            return environment.get().get(name)
        } else {
            throw Parser.ParserError() //TODO: Fix exception type, should it be a resolver error?
        }
    }

    fun assign(token: Token, value: Optional<Any>): Unit = when {
        values.containsKey(token.lexeme) -> values[token.lexeme] = value
        enclosing.isPresent -> enclosing.get().assign(token, value)
        else -> fail(token)
    }

    fun assignAt(distance: Int, token: Token, value: Optional<Any>) {
        val environment = ancestor(distance)
        if (environment.isPresent) {
            environment.get().values.put(token.lexeme, value)
        } else {
            fail(token) //TODO: Fix exception type, should it be a resolver error?
        }
    }


    @Throws
    private fun fail(name: String): Nothing {
        throw Parser.ParserError() //TODO: Fix exception type, should it be a resolver error?
    }

    @Throws
    private fun fail(token: Token): Nothing {
        throw RuntimeError(token, "Undefined variable '${token.lexeme}'.")
    }

    private fun ancestor(distance: Int): Optional<Environment> {
        var environment = this
        for (i in 0 until distance) {
            if (environment.enclosing.isPresent) {
                environment = environment.enclosing.get()
            } else {
                return Optional.empty()
            }
        }
        return Optional.of(environment)
    }

}