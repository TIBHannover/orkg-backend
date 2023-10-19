package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.application.PaperAlreadyExists
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.identifiers.domain.parse
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

        val identifiers = Identifiers.paper.parse(command.identifiers)
        identifiers.forEach { (identifier, value) ->
            val papers = statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = identifier.predicateId,
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
