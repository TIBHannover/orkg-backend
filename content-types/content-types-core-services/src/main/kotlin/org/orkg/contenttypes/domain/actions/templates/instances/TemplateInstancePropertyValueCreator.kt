package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.templates.instances.CreateTemplateInstanceAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class TemplateInstancePropertyValueCreator(
    private val subgraphCreator: SubgraphCreator,
) : CreateTemplateInstanceAction {
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
            listService,
        ),
    )

    override fun invoke(command: CreateTemplateInstanceCommand, state: State): State {
        if (state.statementsToAdd.isNotEmpty()) {
            val bakedStatements = state.statementsToAdd.mapTo(mutableSetOf()) { (predicateId, objectId) ->
                BakedStatement(state.templateInstanceId!!.value, predicateId, objectId)
            }
            subgraphCreator.createThingsAndStatements(
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = command.copy(literals = state.literals),
                validationCache = state.validationCache,
                bakedStatements = bakedStatements,
            )
        }
        return state
    }
}
