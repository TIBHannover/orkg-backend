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
    private val contributionCreator: ContributionCreator
) : ContributionAction {
    constructor(
        resourceService: ResourceUseCases,
        statementService: StatementUseCases,
        literalService: LiteralUseCases,
        predicateService: PredicateUseCases,
        statementRepository: StatementRepository,
        listService: ListUseCases,
    ) : this(
        ContributionCreator(
            resourceService = resourceService,
            statementService = statementService,
            literalService = literalService,
            predicateService = predicateService,
            statementRepository = statementRepository,
            listService = listService
        )
    )

    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState =
        state.copy(
            contributionId = contributionCreator.create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = command,
                contributionDefinitions = listOf(command.contribution),
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            ).single()
        )
}
