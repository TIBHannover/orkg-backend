package org.orkg.contenttypes.domain.actions

import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.input.CreateThingsCommand

open class TempIdValidator {
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

    internal fun CreateThingsCommand.tempIds(): List<String> =
        listOf(resources.keys, literals.keys, predicates.keys, classes.keys, lists.keys).flatten()
}
