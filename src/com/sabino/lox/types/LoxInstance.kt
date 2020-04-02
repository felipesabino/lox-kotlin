package com.sabino.lox.types

internal class LoxInstance(private val klass: LoxClass) {

    override fun toString(): String {
        return "${klass.name} instance"
    }
}