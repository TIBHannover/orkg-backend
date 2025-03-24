package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.templates.instances.UpdateTemplateInstanceAction.State
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class TemplateInstancePropertyValueUpdater(
    private val subgraphCreator: SubgraphCreator,
    private val statementService: StatementUseCases,
) : UpdateTemplateInstanceAction {
    constructor(
        unsafeClassUseCases: UnsafeClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
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
        ),
        statementService
    )

    override fun invoke(command: UpdateTemplateInstanceCommand, state: State): State {
        if (state.statementsToRemove.isNotEmpty()) {
            statementService.findAll(subjectId = command.subject, pageable = PageRequests.ALL)
                .filter { BakedStatement(it.subject.id.value, it.predicate.id.value, it.`object`.id.value) in state.statementsToRemove }
                .mapTo(mutableSetOf()) { it.id }
                .takeIf { it.isNotEmpty() }
                ?.let(statementService::deleteAllById)
        }
        if (state.statementsToAdd.isNotEmpty()) {
            subgraphCreator.createThingsAndStatements(
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = command.copy(literals = state.literals),
                validationCache = state.validationCache,
                bakedStatements = state.statementsToAdd
            )
        }
        return state
    }
}
