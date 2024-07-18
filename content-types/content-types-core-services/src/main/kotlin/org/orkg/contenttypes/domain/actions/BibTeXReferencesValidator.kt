package org.orkg.contenttypes.domain.actions

import java.io.StringReader
import org.jbibtex.BibTeXParser
import org.orkg.contenttypes.domain.InvalidBibTeXReference

class BibTeXReferencesValidator<T, S>(
    private val newValueSelector: (T) -> List<String>?,
    private val oldValueSelector: (S) -> List<String> = { emptyList() },
    private val parser: BibTeXParser = BibTeXParser()
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val newReferences = newValueSelector(command)
        val oldReferences = oldValueSelector(state)
        if (newReferences != null && newReferences.toSet() != oldReferences.toSet()) {
            (newReferences.distinct() - oldReferences.toSet()).forEach {
                try {
                    parser.parse(StringReader(it))
                } catch (e: Exception) {
                    throw InvalidBibTeXReference(it)
                }
            }
        }
        return state
    }
}
