#!/usr/bin/env kotlin

import java.io.File
import java.util.Stack

val capacity = 16 * 1024 // chars

val filename: String = args.firstOrNull() ?: error("A log file needs to be passed as argument.")

val file = File(filename)

if (!file.exists()) error("Cannot find file \"$filename\".")

val diagram = StringBuilder(capacity)

File(filename).useLines { lines: Sequence<String> ->
    diagram.appendLine("@startuml")
    // diagram.appendLine("!pragma teoz true") // Use new rendering engine

    diagram.appendLine("hide unlinked")

    diagram.appendLine("participant Test")
    diagram.appendLine("participant neo4jTransactionManager")
    diagram.appendLine("participant jpaTransactionManager")

    // State, used for calls
    val callStack: Stack<Pair<String, String>> = Stack()

    lines.forEachIndexed { idx, line ->
        val lineNumber = idx.inc()

        // Jump over lines that do not have a timestamp
        if (!line.matches(Regex("^[0-9].*"))) return@forEachIndexed

        // Split log lines into elements
        val (metadata, message: String) = line.split(" - ")
        val (time, _, _, level, logger) = metadata.split(" ")

        // TODO: Parse "No need to create transaction for"
        when {
            // Creating a transaction
            message.startsWith("Creating new transaction with name") -> {
                val regex = Regex("""Creating new transaction with name \[(?<name>.+?)]: (?<props>[^;]+)(; '(?<manager>.+?)')?""")
                val groups = regex.find(message)!!.groups
                val (clazz, method) = groups["name"]!!.value.split('.').takeLast(2)
                val properties = groups["props"]!!.value.split(',')
                var manager = groups["manager"]?.value ?: "" // from Simple*Repository

                if (manager.isEmpty()) {
                    manager =
                        when {
                            logger.endsWith("Neo4jTransactionManager") -> {
                                "neo4jTransactionManager"
                            }

                            else -> error("Unable to determine transaction manager")
                        }
                }

                val symbols = mutableListOf("plus")
                if ("readOnly" in properties) symbols += "lock-locked"

                val color = when(manager) {
                    "neo4jTransactionManager" -> "orange"
                    "jpaTransactionManager" -> "blue"
                    else -> ""
                }

                with(diagram) {
                    appendCall(lineNumber, "Test", manager, clazz, method, symbols)
                    append("activate $manager")
                    if (color.isNotEmpty()) {
                        append(" #$color")
                    }
                    appendLine()
                }

                callStack.push("Test" to manager)
            }
            // Closing a transaction
            message.startsWith("Initiating transaction commit") -> {
                val currentCall = callStack.pop()
                diagram.appendLine("deactivate ${currentCall.second}")
            }
            // Looking for an existing transaction
            message.startsWith("Getting transaction for") && logger.endsWith("TransactionInterceptor") -> {
                val regex = Regex("""Getting transaction for \[(?<name>.+?)]""")
                val groups = regex.find(message)!!.groups
                var (clazz, method) = groups["name"]!!.value.split('.').takeLast(2)

                val currentComponent = callStack.peek().second

                if (clazz == "SimpleNeo4jRepository") {
                    clazz = currentComponent.removePrefix("SpringDataNeo4j").removeSuffix("Adapter") + "InternalRepository"
                }

                diagram.appendCall(lineNumber, currentComponent, clazz, clazz, method)
                callStack.push(currentComponent to clazz)
            }
            // Completing running transaction
            message.startsWith("Completing transaction for") && logger.endsWith("TransactionInterceptor") -> {
                val regex = Regex("""Completing transaction for \[(?<name>.+?)]""")
                val groups = regex.find(message)!!.groups
                var (clazz, method) = groups["name"]!!.value.split('.').takeLast(2)

                val currentCall = callStack.pop()

                if (clazz == "SimpleNeo4jRepository") {
                    clazz = currentCall.second
                }

                // plausibility check; should never trigger
                require(currentCall.second == clazz) { "error with state: $callStack, $currentCall, $clazz" }

                diagram.appendLine("$clazz --> ${currentCall.first}")
                diagram.appendLine("deactivate $clazz")
            }
            // TODO: parse: No need to create transaction for
        }
    }
    require(callStack.isEmpty()) { "Not all calls were accounted for. There is a bug in the script." }
    diagram.appendLine("@enduml")
}

println(diagram.toString())

fun StringBuilder.appendCall(
    lineNo: Int,
    from: String,
    to: String,
    clazz: String,
    method: String,
    symbols: List<String> = listOf(),
): StringBuilder {
    val syms = if (symbols.isNotEmpty()) symbols.joinToString(separator = "", postfix = " ") { "<&$it>" } else ""
    return this.appendLine("$from -> $to: [$lineNo]\\n$syms$clazz\\n.$method")
}
