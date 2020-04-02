package com.sabino.lox.types

import com.sabino.lox.Interpreter
import java.util.*

internal class LoxClass(val name: String) : LoxCallable {

    override fun arity(): Int {
        return 0;
    }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        return Optional.of(LoxInstance(this));
    }

    override fun toString(): String {
        return name
    }
}