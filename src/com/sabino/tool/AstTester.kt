package com.sabino.tool

import com.sabino.klox.AstPrinter
import com.sabino.klox.Expr
import com.sabino.klox.Expr.Literal
import com.sabino.klox.Token
import com.sabino.klox.TokenType


class AstTester {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val expression: Expr = Expr.Binary(
                Expr.Unary(
                    Token(TokenType.MINUS, "-", null, 1),
                    Literal(123)
                ),
                Token(TokenType.STAR, "*", null, 1),
                Expr.Grouping(
                    Literal(45.67)
                )
            )

            println(AstPrinter().print(expression))
        }
    }
}