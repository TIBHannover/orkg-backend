import java.io.File

/**
 * This file parses a spring boot log file for deprecated cypher query warnings,
 * deduplicates them and writes them into a separate file.
 *
 * Usage:
 *      CypherQueryDeprecationLogAnalyzer.kts /path/to/log.txt /path/to/output.txt
 */

val file = File(args[0])
val messages = mutableSetOf<String>()

file.useLines { sequence ->
    val iterator = sequence.iterator()
    while (iterator.hasNext()) {
        val line = iterator.next()
        if (line.contains("This feature is deprecated and will be removed in future versions")) {
            val buffer = mutableListOf<String>()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.startsWith("\t")) {
                    buffer.add(next)
                } else {
                    buffer.add(next)
                    break
                }
            }
            messages.add(buffer.joinToString("\n"))
        }
    }
}

File(args[1]).writeText(messages.joinToString("\n"))
