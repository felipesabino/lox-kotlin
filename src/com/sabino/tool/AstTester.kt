package com.sabino.tool

import com.sabino.lox.AstPrinter
import com.sabino.lox.types.Expr
import com.sabino.lox.types.Expr.Literal
import com.sabino.lox.types.Token
import com.sabino.lox.types.TokenType
import java.util.*


class AstTester {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val expression: Expr = Expr.Binary(
                Expr.Unary(
                    Token(TokenType.MINUS, "-", Optional.empty(), 1),
                    Literal(Optional.of(123))
                ),
                Token(TokenType.STAR, "*", Optional.empty(), 1),
                Expr.Grouping(
                    Literal(Optional.of(45.67))
                )
            )

            println(AstPrinter().print(expression))
        }
    }
}