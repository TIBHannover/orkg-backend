package org.orkg.contenttypes.domain.actions.templates.instances

import org.orkg.common.PageRequests
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.SubgraphCreator
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceCommand
import org.orkg.contenttypes.domain.actions.UpdateTemplateInstanceState
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.StatementRepository

class TemplateInstancePropertyValueUpdater(
    private val subgraphCreator: SubgraphCreator,
    private val statementService: StatementUseCases,
) : UpdateTemplateInstanceAction {
    constructor(
        classService: ClassUseCases,
        unsafeResourceUseCases: UnsafeResourceUseCases,
        statementService: StatementUseCases,
        unsafeStatementUseCases: UnsafeStatementUseCases,
        literalService: LiteralUseCases,
        predicateService: PredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        SubgraphCreator(
            classService = classService,
            unsafeResourceUseCases = unsafeResourceUseCases,
            unsafeStatementUseCases = unsafeStatementUseCases,
            literalService = literalService,
            predicateService = predicateService,
            statementRepository = statementRepository,
            listService = listService
        ),
        statementService
    )

    override fun invoke(
        command: UpdateTemplateInstanceCommand,
        state: UpdateTemplateInstanceState,
    ): UpdateTemplateInstanceState =
        state.apply {
            if (statementsToRemove.isNotEmpty()) {
                statementService.findAll(subjectId = command.subject, pageable = PageRequests.ALL)
                    .filter { BakedStatement(it.subject.id.value, it.predicate.id.value, it.`object`.id.value) in statementsToRemove }
                    .mapTo(mutableSetOf()) { it.id }
                    .takeIf { it.isNotEmpty() }
                    ?.let(statementService::deleteAllById)
            }
            if (statementsToAdd.isNotEmpty()) {
                subgraphCreator.createThingsAndStatements(
                    contributorId = command.contributorId,
                    extractionMethod = command.extractionMethod,
                    thingDefinitions = command.copy(literals = state.literals),
                    validatedIds = validatedIds,
                    bakedStatements = statementsToAdd
                )
            }
        }
}
