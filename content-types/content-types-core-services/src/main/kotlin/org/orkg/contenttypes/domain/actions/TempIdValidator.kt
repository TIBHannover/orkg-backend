package org.orkg.contenttypes.domain.actions

import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidTempId

class TempIdValidator<T, S>(
    private val valueSelector: (T) -> List<String>?,
) : Action<T, S> {
    override fun invoke(command: T, state: S): S {
        val ids = valueSelector(command) ?: return state
        ids.forEach {
            if (!it.startsWith('#') || it.length < 2) {
                throw InvalidTempId(it)
            }
        }
        val duplicates = ids.groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            throw DuplicateTempIds(duplicates)
        }
        return state
    }
}
