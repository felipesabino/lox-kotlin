package com.sabino.lox.types

import java.util.*

internal class Token(val type: TokenType, val lexeme: String, val literal: Optional<Any>, val line: Int) {

    override fun toString(): String {
        return if (literal.isPresent) {
            "$type $lexeme $literal"
        } else {
            "$type $lexeme"
        }
    }
}