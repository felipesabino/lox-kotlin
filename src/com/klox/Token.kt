package com.klox

internal class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int) {

    override fun toString(): String {
        return if (literal != null) {
            "$type $lexeme $literal"
        } else {
            "$type $lexeme"
        }
    }
}