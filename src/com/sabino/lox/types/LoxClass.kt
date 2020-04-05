package com.sabino.lox.types

import com.sabino.lox.Interpreter
import java.util.*

internal class LoxClass(
    val name: String,
    val superclass: Optional<LoxClass>,
    private val methods: Map<String, LoxFunction>
) : LoxCallable {

    override fun arity(): Int {
        val initializer = findMethod("init")
        return if (initializer.isPresent) {
            initializer.get().arity()
        } else {
            0
        }
    }

    override fun call(interpreter: Interpreter, arguments: Iterable<Any>): Optional<Any> {

        val instance = LoxInstance(this)

        val initializer = findMethod("init")
        if (initializer.isPresent) {
            initializer.get().bind(instance).call(interpreter, arguments)
        }

        return Optional.of(instance)
    }

    fun findMethod(name: String): Optional<LoxFunction> {

        if (methods.containsKey(name)) {
            return Optional.ofNullable(methods[name])
        }

        if (superclass.isPresent) {
            return superclass.get().findMethod(name)
        }

        return Optional.empty()
    }

    override fun toString(): String {
        return name
    }
}