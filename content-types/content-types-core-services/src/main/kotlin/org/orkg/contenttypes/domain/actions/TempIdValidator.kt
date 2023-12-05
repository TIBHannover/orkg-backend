package org.orkg.contenttypes.domain.actions

import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.input.CreatePaperUseCase.CreateCommand.PaperContents

abstract class TempIdValidator {
    internal fun validate(ids: List<String>) {
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
    }

    protected fun PaperContents.tempIds(): List<String> =
        listOf(resources.keys, literals.keys, predicates.keys, lists.keys).flatten()
}
