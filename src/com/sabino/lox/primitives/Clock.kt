package com.sabino.lox.primitives

import com.sabino.lox.Interpreter
import com.sabino.lox.types.LoxCallable
import java.util.Optional

internal class Clock : LoxCallable {
    override fun arity(): Int { return 0 }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        return Optional.of(System.currentTimeMillis() / 1000)
    }

    override fun toString(): String { return "<native fun>" }
}