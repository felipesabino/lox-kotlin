package com.sabino.klox

import java.util.*

internal class KloxFunction(val declaration: Stmt.Function): KloxCallable {

    override fun arity(): Int {
        TODO("Not yet implemented")
    }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {
        TODO("Not yet implemented")
    }
}