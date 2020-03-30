package com.sabino.klox

import java.util.Optional

internal class Token(val type: TokenType, val lexeme: String, val literal: Optional<Any>, val line: Int) {

    override fun toString(): String {
        return if (literal != null) {
            "$type $lexeme $literal"
        } else {
            "$type $lexeme"
        }
    }
}