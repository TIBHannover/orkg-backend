package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.domain.model.ExactSearchString
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.SearchString

internal fun String.matches(searchString: SearchString): Boolean =
    when (searchString) {
    is ExactSearchString -> equals(searchString.value, ignoreCase = true)
    is FuzzySearchString -> {
        val unescaped = searchString.value.replace(Regex("""\\([~\[\]{}:^])"""), "$1")
        contains(unescaped, ignoreCase = true)
    }
}
