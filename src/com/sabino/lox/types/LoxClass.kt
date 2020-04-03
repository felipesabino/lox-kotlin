package com.sabino.lox.types

import com.sabino.lox.Interpreter
import java.util.*

internal class LoxClass(val name: String, private val methods: Map<String, LoxFunction>) : LoxCallable {

    override fun arity(): Int {
        return 0;
    }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        return Optional.of(LoxInstance(this));
    }

    fun findMethod(name: String): Optional<LoxFunction> {
        return Optional.ofNullable(methods.get(name))
    }

    override fun toString(): String {
        return name
    }
}