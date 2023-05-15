package eu.tib.orkg.prototype.statements.domain.model

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.queryparser.classic.QueryParser

sealed class SearchString(val value: String) {
    companion object {
        fun of(string: String, exactMatch: Boolean = false): SearchString =
            if (exactMatch) ExactSearchString(string) else FuzzySearchString(string)
    }
}

class FuzzySearchString(string: String) : SearchString(parseFuzzy(string))
class ExactSearchString(string: String) : SearchString(parseExact(string))

/**
 * Normalizes the given string and escapes any special characters according to [escapeFuzzySearchString].
 * If the given string cannot be parsed by Apache Lucene, the entire string will be escaped.
 */
private fun parseFuzzy(string: String): String {
    val result = string.normalize().escapeFuzzySearchString()
    try {
        QueryParser("", StandardAnalyzer()).parse(result)
    } catch (e: ParseException) {
        return QueryParser.escape(result)
    }
    return result
}

private fun parseExact(string: String): String = QueryParser.escape(string.normalize())

private fun String.normalize() = replace(Regex("""\s+"""), " ").trim()

/**
 * Escapes the following characters if they are not already escaped:
 *
 * | Symbol(s) | Function               |
 * | --------- | ---------------------- |
 * | ```~```   | Fuzzy/Proximity search |
 * | ```[]```  | Date range search      |
 * | ```{}```  | Non-date range search  |
 * | ```^```   | Term boosting          |
 * | ```:```   | Field                  |
 *
 * Also escapes leading ```*``` and ```?```.
 */
fun String.escapeFuzzySearchString(): String =
    replace(Regex("""(?<=^|[^\\])([~\[\]{}:^])"""), """\\$1""")
        .replace(Regex("""^([*?])"""), """\\$1""")
