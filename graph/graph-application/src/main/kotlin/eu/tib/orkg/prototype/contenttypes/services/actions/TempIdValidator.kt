package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.DuplicateTempIds
import eu.tib.orkg.prototype.contenttypes.application.InvalidTempId

class TempIdValidator : PaperAction, ContributionAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val ids = command.contents?.tempIds().orEmpty()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }

    override fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        val ids = command.tempIds()
        if (ids.isNotEmpty()) {
            validate(ids)
        }
        return state.copy(tempIds = ids.toSet())
    }

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

    private fun CreatePaperUseCase.CreateCommand.PaperContents.tempIds(): List<String> =
        listOf(resources.keys, literals.keys, predicates.keys, lists.keys).flatten()
}
