package com.sabino.klox

import java.util.Optional

internal interface KloxCallable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any>
}