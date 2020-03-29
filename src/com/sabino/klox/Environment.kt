package com.sabino.klox

import java.util.Optional

internal class Environment {
    private val values = mutableMapOf<String, Optional<Any>>()

    fun define(name: String, value: Optional<Any>) {
        values[name] = value
    }

    fun get(token: Token) : Optional<Any> {
        return values.getOrElse(token.lexeme, {
            throw Interpreter.RuntimeError(token, "Undefined variable '${token.lexeme}'")
        })
    }
}