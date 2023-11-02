package eu.tib.orkg.prototype.statements.domain.model

import org.apache.lucene.queryparser.classic.QueryParser

sealed interface SearchString {
    val input: String
    val query: String

    companion object {
        fun of(string: String, exactMatch: Boolean = false): SearchString =
            if (exactMatch) ExactSearchString(string) else FuzzySearchString(string)
    }
}

class FuzzySearchString(value: String) : SearchString {
    override val input: String = value.normalize()
    override val query: String = input.toFuzzyQuery()

    private fun String.toFuzzyQuery(): String {
        val builder = StringBuilder()
        val reader = StringReader(this)

        while (reader.canRead()) {
            val c = reader.peek()
            if (c.isWordCharacter()) {
                if (builder.isNotEmpty()) {
                    builder.append(" AND ")
                }
                builder.append("*")
                builder.append(reader.readWord())
                builder.append("*")
                reader.skipWhitespace()
            } else if ((c == '+' || c == '-') && reader.canRead(1) && reader.peek(1).isWordCharacter() &&
                (builder.isEmpty() || reader.cursor > 0 && reader.peek(-1).isWhitespace())
            ) {
                reader.skip()
                if (builder.isNotEmpty()) {
                    builder.append(" AND ")
                }
                builder.append(c)
                builder.append(reader.readWord())
                reader.skipWhitespace()
            } else {
                reader.skip()
            }
        }

        return builder.toString().trim().ifBlank { "*" }
    }
}

class ExactSearchString(value: String) : SearchString {
    override val input: String = value.normalize()
    override val query: String = if (input.isBlank()) "*" else QueryParser.escape(input)
}

private data class StringReader(val string: String) {
    var cursor: Int = 0

    fun skip() { cursor++ }

    fun peek(offset: Int = 0): Char = string[cursor + offset]

    fun canRead(offset: Int = 0): Boolean = cursor + offset < string.length
}

private fun StringReader.readWord(): String {
    val start = cursor
    while (canRead()) {
        if (!peek().isWordCharacter()) {
            break
        }
        skip()
    }
    return string.substring(start, cursor)
}

private fun StringReader.skipWhitespace() {
    while (canRead()) {
        if (!peek().isWhitespace()) {
            return
        }
        skip()
    }
}

private fun Char.isWordCharacter(): Boolean = toString().matches(Regex("\\w"))

private fun String.normalize() = replace(Regex("""\s+"""), " ").lowercase().trim()
