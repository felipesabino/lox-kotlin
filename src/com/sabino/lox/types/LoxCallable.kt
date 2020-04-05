package com.sabino.lox.types

import com.sabino.lox.Interpreter
import java.util.*

internal interface LoxCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any>
}