package com.sabino.tool

import java.io.IOException
import java.io.PrintWriter
import kotlin.system.exitProcess


class GenerateAst {

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            if (args.size != 1) {
                System.err.println("Usage: generate_ast <output directory>")
                exitProcess(1)
            }
            val outputDir = args[0]

            defineAst(outputDir, "Expr", listOf(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, Iterable<Expr> arguments",
                "Get      : Expr obj, Token name",
                "Grouping : Expr expression",
                "Literal  : Optional<Any> value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr obj, Token name, Expr value",
                "Unary    : Token operator, Expr right",
                "Variable : Token name"
            ))


            defineAst(outputDir, "Stmt", listOf(
                "Block      : Iterable<Stmt> statements",
                "Class      : Token name, Iterable<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, Iterable<Token> params, Iterable<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Optional<Stmt> elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Optional<Expr> value",
                "Var        : Token name, Optional<Expr> initializer",
                "While      : Expr condition, Stmt body"
            ))
        }

        @Throws(IOException::class)
        @JvmStatic private fun defineAst(
            outputDir: String, baseName: String, typeList: List<String>
        ) {

            val types = typeList.map { it -> Pair(
                it.split(':')[0].trim { it <= ' ' },
                it.split(':')[1]
                    .trim { it <= ' ' }
                    .split(", ")
                    .map { Pair(it.split(" ")[0], it.split(" ")[1]) }
            )}

            val path = "$outputDir/$baseName.kt"
            val writer = PrintWriter(path, "UTF-8")
            writer.println("package com.sabino.lox.types")
            writer.println()
            writer.println("import java.util.Optional")
            writer.println()
            writer.println("internal abstract class $baseName {")
            writer.println()

            // Visitor interface
            defineVisitor(writer, baseName, types.map { it.first })

            // The AST classes.
            for (type in types) {
                val className = type.first
                val fields = type.second
                defineType(writer, baseName, className, fields)
            }

            // The base accept() method.
            writer.println()
            writer.println("  abstract fun <R> accept(visitor: Visitor<R>): R")

            writer.println("}")
            writer.close()
        }

        private fun defineVisitor(
            writer: PrintWriter, baseName: String, types: List<String>
        ) {
            writer.println("  interface Visitor<R> {")
            types
                .map { "    fun visit${it}${baseName}(${baseName.toLowerCase()}: ${it}): R" }
                .forEach(writer::println)

            writer.println("  }")
        }

        @JvmStatic  private fun defineType(
            writer: PrintWriter, baseName: String,
            className: String, fieldList: List<Pair<String, String>>
        ) {

            val fields = fieldList.joinToString(separator = ", ") { "val ${it.second}: ${it.first}" }

            writer.println("""
    internal class ${className}(${fields}) : ${baseName}() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visit${className}${baseName}(this)
        }
    }""")

        }
    }
}