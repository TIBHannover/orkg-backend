package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.UpdateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.rosettastone.statements.UpdateRosettaStoneStatementAction.State
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class RosettaStoneStatementThingDefinitionUpdateCreator(
    private val subgraphCreator: SubgraphCreator,
) : UpdateRosettaStoneStatementAction {
    constructor(
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        unsafePredicateUseCases: UnsafePredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        SubgraphCreator(
            classService,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            literalService,
            unsafePredicateUseCases,
            statementRepository,
            listService
        )
    )

    override fun invoke(command: UpdateRosettaStoneStatementCommand, state: State): State {
        val tempId2Thing: MutableMap<String, ThingId> = mutableMapOf()
        subgraphCreator.createThings(
            thingDefinitions = command,
            validatedIds = state.validatedIds,
            contributorId = command.contributorId,
            extractionMethod = command.extractionMethod,
            lookup = tempId2Thing
        )
        return state.copy(tempId2Thing = tempId2Thing)
    }
}
