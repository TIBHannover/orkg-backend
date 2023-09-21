package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreateContributionUseCase
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.application.PaperAlreadyExists
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository

class PaperExistenceValidator(
    private val resourceRepository: ResourceRepository,
    private val resourceService: ResourceUseCases,
    private val statementRepository: StatementRepository
) : PaperAction, ContributionAction {
    override fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        val resource = resourceService.findAllByTitle(command.title).firstOrNull()
        if (resource != null) {
            throw PaperAlreadyExists.withTitle(resource.label)
        }

        val identifiers = Identifiers.paper associateWith command.identifiers
        // TODO: Do we want to validate identifier values structurally?
        identifiers.forEach { (predicate, value) ->
            val papers = statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = predicate,
                literal = value,
                subjectClass = Classes.paper,
                pageable = PageRequests.SINGLE
            )
            if (papers.content.isNotEmpty()) {
                throw PaperAlreadyExists.withIdentifier(value)
            }
        }
        return state
    }

    override operator fun invoke(command: CreateContributionCommand, state: ContributionState): ContributionState {
        resourceRepository.findPaperById(command.paperId)
            .orElseThrow { PaperNotFound(command.paperId) }
        return state
    }
}