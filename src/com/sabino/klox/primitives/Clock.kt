package com.sabino.klox.primitives

import com.sabino.klox.Interpreter
import com.sabino.klox.KloxCallable
import java.util.Optional

internal class Clock : KloxCallable {
    override fun arity(): Int { return 0 }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        return Optional.of(System.currentTimeMillis() / 1000)
    }

    override fun toString(): String { return "<native fun>" }
}