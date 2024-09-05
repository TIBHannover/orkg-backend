package org.orkg.graph.domain

sealed interface SearchString {
    val input: String

    companion object {
        fun of(string: String, exactMatch: Boolean = false): SearchString =
            if (exactMatch) ExactSearchString(string) else FuzzySearchString(string)
    }
}

class FuzzySearchString(value: String) : SearchString {
    override val input: String = value.normalize()
}

class ExactSearchString(value: String) : SearchString {
    override val input: String = value.normalize()
}

private fun String.normalize() = replace(Regex("""\s+"""), " ").lowercase().trim()
