package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.UpdateTableCommand
import org.orkg.contenttypes.domain.actions.tables.UpdateTableAction.State
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class TableThingsCommandUpdateCreator(
    private val subgraphCreator: SubgraphCreator,
) : UpdateTableAction {
    constructor(
        unsafeClassUseCases: UnsafeClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        unsafeLiteralUseCases: UnsafeLiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        SubgraphCreator(
            unsafeClassUseCases,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            unsafeLiteralUseCases,
            unsafePredicateUseCases,
            statementRepository,
            listService
        )
    )

    override fun invoke(command: UpdateTableCommand, state: State): State {
        if (command.rows == null) {
            return state
        }
        val tempIdToThingId = mutableMapOf<String, ThingId>()
        subgraphCreator.createThings(
            thingsCommand = command,
            validationCache = state.validationCache,
            contributorId = command.contributorId,
            extractionMethod = command.extractionMethod ?: ExtractionMethod.UNKNOWN,
            lookup = tempIdToThingId
        )
        return state.copy(tempIdToThingId = tempIdToThingId)
    }
}
