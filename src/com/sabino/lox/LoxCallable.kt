package com.sabino.lox

import java.util.Optional

internal interface LoxCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any>
}