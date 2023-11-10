package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.DuplicateTempIds
import eu.tib.orkg.prototype.contenttypes.application.InvalidTempId

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

    protected fun CreatePaperUseCase.CreateCommand.PaperContents.tempIds(): List<String> =
        listOf(resources.keys, literals.keys, predicates.keys, lists.keys).flatten()
}
