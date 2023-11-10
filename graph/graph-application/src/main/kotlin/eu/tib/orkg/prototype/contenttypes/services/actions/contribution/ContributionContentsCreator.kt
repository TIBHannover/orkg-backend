package eu.tib.orkg.prototype.contenttypes.services.actions.contribution

import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionState
import eu.tib.orkg.prototype.contenttypes.services.actions.CreateContributionCommand
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.spi.StatementRepository

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
                contents = command,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            ).single()
        )
}
