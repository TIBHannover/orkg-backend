package org.orkg.contenttypes.domain.actions.rosettastone.statements

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.CreateRosettaStoneStatementCommand
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction.State
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class RosettaStoneStatementThingDefinitionCreateCreator(
    private val subgraphCreator: SubgraphCreator
) : CreateRosettaStoneStatementAction {
    constructor(
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        predicateService: PredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases
    ) : this(
        SubgraphCreator(
            classService,
            unsafeResourceUseCases,
            unsafeStatementUseCases,
            literalService,
            predicateService,
            statementRepository,
            listService
        )
    )

    override fun invoke(command: CreateRosettaStoneStatementCommand, state: State): State {
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
