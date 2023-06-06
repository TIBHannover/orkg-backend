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
    override val query: String
    override val input: String

    init {
        this.input = value.normalize()
        this.query = QueryParser.escape(this.input)
            .replace(Regex("""(^|\W)\\([+-]\w)"""), "$1$2")
            .replace(Regex("""(\w)\\-(\w)"""), """$1 $2""")
            .replace(Regex("""(\w)(\s|$)"""), "$1*$2")
            .replace(Regex("""\s"""), " AND ")
    }
}

class ExactSearchString(value: String) : SearchString {
    override val query: String
    override val input: String

    init {
        this.input = value.normalize()
        this.query = QueryParser.escape(this.input)
    }
}

private fun String.normalize() = replace(Regex("""\s+"""), " ").lowercase().trim()
