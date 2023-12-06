package org.orkg.contenttypes.domain.actions.contribution

import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.StatementRepository

class ContributionContentsCreator(
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
), ContributionAction {
    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState =
        state.copy(
            contributionId = create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                contents = command,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            ).single()
        )
}
