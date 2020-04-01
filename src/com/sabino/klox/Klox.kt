package com.sabino.klox

import com.sabino.klox.Interpreter.RuntimeError
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.System.exit
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.exp
import kotlin.system.exitProcess


class Klox {
    companion object {
        private var hadError = false
        private var hadRuntimeError = false

        private val interpreter = Interpreter()

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
            if (hadRuntimeError) exit(70)
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
            hadRuntimeError = false

            val scanner = Scanner(source)
            val tokens: List<Token> = scanner.scanTokens()

            val parser = Parser(tokens)
            val statements = parser.parse().toList()

            // stop in case of a syntax error
            if (hadError ) { return }

            interpreter.interpret(statements);
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

        internal fun runtimeError(error: RuntimeError) {
            System.err.println(
                error.message +
                        "\n[line " + error.token.line + "]"
            )
            hadRuntimeError = true
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println(
                "[line $line] Error$where: $message"
            )
            hadError = true
        }
    }
}