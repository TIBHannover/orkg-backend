package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing

class TableUpdateValidationCacheInitializer : UpdateTableAction {
    override fun invoke(command: UpdateTableCommand, state: State): State {
        val validationCache = state.statements.values
            .flatten()
            .flatMap { statement -> listOf(statement.subject, statement.predicate, statement.`object`) }
            .toSet()
            .associate { thing -> thing.id.value to Either.right<CreateThingCommandPart, Thing>(thing) }
        return state.copy(validationCache = state.validationCache + validationCache)
    }
}
