package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.SearchString

internal fun String.matches(searchString: SearchString): Boolean = when (searchString) {
    is ExactSearchString -> equals(searchString.input, ignoreCase = true)
    is FuzzySearchString -> {
        val searchWords = searchString.input
            .replace(Regex("""(\w)-(\w)"""), """$1 $2""")
            .split(" ")
        val words = split(" ", "-")
        searchWords.all { searchWord ->
            when {
                searchWord.startsWith("-") -> words.all {
                    !it.startsWith(searchWord.substring(1))
                }
                searchWord.startsWith("+") -> words.any {
                    it.startsWith(searchWord.substring(1))
                }
                else -> words.any { it.startsWith(searchWord) }
            }
        }
    }
}
