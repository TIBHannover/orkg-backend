package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class RosettaStoneStatementThingsCommandCreateCreator(
    private val subgraphCreator: SubgraphCreator,
) : CreateRosettaStoneStatementAction {
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

    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
        val tempIdToThing: MutableMap<String, ThingId> = mutableMapOf()
        subgraphCreator.createThings(
            thingsCommand = command,
            validationCache = state.validationCache,
            contributorId = command.contributorId,
            extractionMethod = command.extractionMethod,
            lookup = tempIdToThing
        )
        return state.copy(tempIdToThing = tempIdToThing)
    }
}
