package com.sabino.tool

import java.io.IOException
import java.io.PrintWriter


class GenerateAst {

    companion object {

        @JvmStatic fun main(args: Array<String>) {
            if (args.size != 1) {
                System.err.println("Usage: generate_ast <output directory>")
                System.exit(1)
            }
            val outputDir = args[0]

            defineAst(outputDir, "Expr", listOf(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Any? value",
                "Unary    : Token operator, Expr right"
            ))


            defineAst(outputDir, "Stmt", listOf(
                "Expression : Expr expression",
                "Print      : Expr expression"
            ));
        }

        @Throws(IOException::class)
        @JvmStatic private fun defineAst(
            outputDir: String, baseName: String, typeList: List<String>
        ) {

            val types = typeList.map { Pair(
                it.split(':')[0].trim { it <= ' ' },
                it.split(':')[1]
                    .trim { it <= ' ' }
                    .split(", ")
                    .map { Pair(it.split(" ")[0], it.split(" ")[1]) }
            )}

            val path = "$outputDir/$baseName.kt"
            val writer = PrintWriter(path, "UTF-8")
            writer.println("package com.sabino.klox;")
            writer.println()
            writer.println("internal abstract class $baseName {")
            writer.println();

            // Visitor interface
            defineVisitor(writer, baseName, types.map { it.first });

            // The AST classes.
            for (type in types) {
                val className = type.first
                val fields = type.second
                defineType(writer, baseName, className, fields)
            }

            // The base accept() method.
            writer.println();
            writer.println("  abstract fun <R> accept(visitor: Visitor<R>): R");

            writer.println("}")
            writer.close()
        }

        private fun defineVisitor(
            writer: PrintWriter, baseName: String, types: List<String>
        ) {
            writer.println("  interface Visitor<R> {")
            types
                .map { "    fun visit${it}${baseName}(expr: ${it}): R" }
                .forEach(writer::println)

            writer.println("  }")
        }

        @JvmStatic  private fun defineType(
            writer: PrintWriter, baseName: String,
            className: String, fieldList: List<Pair<String, String>>
        ) {

            val fields = fieldList
                .map { "val ${it.second}: ${it.first}" }
                .joinToString(separator = ", ")

            writer.println("""
    internal class ${className}(${fields}) : ${baseName}() {
        override fun <R> accept(visitor: ${baseName}.Visitor<R>): R {
            return visitor.visit${className}${baseName}(this)
        }
    }""")

        }
    }
}