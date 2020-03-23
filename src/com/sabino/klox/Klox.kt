package com.sabino.klox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.System.exit
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Klox {
    companion object {
        private var hadError = false

        @JvmStatic fun main(args: Array<String>) {
            when (args.size) {
                1 -> runFile(args[0])
                0 -> runPrompt()
                else -> {
                    println("Usage: klox [script]")
                    exitProcess(64)
                }
            }
        }

        @Throws(IOException::class)
        private fun runFile(path: String) {
            val bytes = Files.readAllBytes(Paths.get(path))
            run(String(bytes, Charset.defaultCharset()))

            // Indicate an error in the exit code.
            if (hadError) exit(65)
        }

        @Throws(IOException::class)
        private fun runPrompt() {
            val input = InputStreamReader(System.`in`)
            val reader = BufferedReader(input)

            while (true) {
                print("> ")
                run(reader.readLine())
            }
        }

        private fun run(source: String) {
            hadError = false

            val scanner = Scanner(source)
            val tokens: List<Token> = scanner.scanTokens()

            val parser = Parser(tokens)
            val expr = parser.parse()

            // stop in case of a syntax error
            if (hadError || expr.isEmpty) { return }

            // For now, just print the AST
            println(AstPrinter().print(expr.get()))
        }

        internal fun error(line: Int, message: String) {
            report(line, "", message)
        }

        internal fun error(token: Token, message: String) {
            if(token.type == TokenType.EOF) {
                report(token.line, " at end ", message)
            } else {
                report(token.line, " at '${token.lexeme}'", message)
            }
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println(
                "[line $line] Error$where: $message"
            )
            hadError = true
        }
    }
}