package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.PaperState
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.StatementRepository

class PaperContributionCreator(
    resourceService: ResourceUseCases,
    statementService: StatementUseCases,
    literalService: LiteralUseCases,
    predicateService: PredicateUseCases,
    statementRepository: StatementRepository,
    listService: ListUseCases
) : ContributionCreator(
    resourceService = resourceService,
    statementService = statementService,
    literalService = literalService,
    predicateService = predicateService,
    statementRepository = statementRepository,
    listService = listService
), PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        if (command.contents != null) {
            create(
                paperId = state.paperId!!,
                contributorId = command.contributorId,
                contents = command.contents!!,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        }
        return state
    }
}
