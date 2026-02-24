package org.orkg.contenttypes.domain.actions.comparisons

import org.orkg.contenttypes.domain.ComparisonDataSource
import org.orkg.contenttypes.domain.DuplicateComparisonDataSources
import org.orkg.contenttypes.domain.actions.Action

class ComparisonDataSourcesValidator<T, S>(
    private val valueSelector: (T) -> List<ComparisonDataSource>?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        valueSelector(command)?.also { sources ->
            val duplicates = sources.groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
            if (duplicates.isNotEmpty()) {
                throw DuplicateComparisonDataSources(duplicates)
            }
        }
        return state
    }
}
