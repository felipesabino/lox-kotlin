package com.sabino.klox

import com.sabino.klox.Interpreter.RuntimeError
import java.util.*


internal class Environment {
    private val values = mutableMapOf<String, Optional<Any>>()

    fun define(name: String, value: Optional<Any>) {
        values[name] = value
    }

    fun get(token: Token) : Optional<Any> {
        return values.getOrElse(token.lexeme, {
            throw Interpreter.RuntimeError(token, "Undefined variable '${token.lexeme}'.")
        })
    }

    fun assign(name: Token, value: Optional<Any>) {

        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
        }
    }
}