package eu.tib.orkg.prototype.contenttypes.services.actions.paper

import eu.tib.orkg.prototype.contenttypes.services.actions.ContributionCreator
import eu.tib.orkg.prototype.contenttypes.services.actions.CreatePaperCommand
import eu.tib.orkg.prototype.contenttypes.services.actions.PaperState
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.PredicateUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.spi.StatementRepository

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
                contents = command.contents,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        }
        return state
    }
}
