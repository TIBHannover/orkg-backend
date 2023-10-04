package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.net.URI

class PaperPublicationInfoCreator(
    private val resourceService: ResourceUseCases,
    private val resourceRepository: ResourceRepository,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases
) : PaperAction {
    override operator fun invoke(command: CreatePaperCommand, state: PaperState): PaperState {
        if (command.publicationInfo != null) {
            if (command.publicationInfo.publishedMonth != null) {
                linkPublicationMonth(command, state, command.publicationInfo.publishedMonth)
            }
            if (command.publicationInfo.publishedYear != null) {
                linkPublicationYear(command, state, command.publicationInfo.publishedYear)
            }
            if (command.publicationInfo.publishedIn != null) {
                linkPublicationVenue(command, state, command.publicationInfo.publishedIn)
            }
            if (command.publicationInfo.url != null) {
                linkPublicationUrl(command, state, command.publicationInfo.url)
            }
        }
        return state
    }

    private fun linkPublicationMonth(
        command: CreatePaperCommand,
        state: PaperState,
        publishedMonth: Int
    ) {
        val publishedIn = literalService.create(
            userId = command.contributorId,
            label = publishedMonth.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        ).id
        statementService.add(
            userId = command.contributorId,
            subject = state.paperId!!,
            predicate = Predicates.monthPublished,
            `object` = publishedIn
        )
    }

    private fun linkPublicationYear(
        command: CreatePaperCommand,
        state: PaperState,
        publishedYear: Long
    ) {
        val publishedIn = literalService.create(
            userId = command.contributorId,
            label = publishedYear.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        ).id
        statementService.add(
            userId = command.contributorId,
            subject = state.paperId!!,
            predicate = Predicates.yearPublished,
            `object` = publishedIn
        )
    }

    private fun linkPublicationVenue(
        command: CreatePaperCommand,
        state: PaperState,
        publishedIn: String
    ) {
        val venueId = resourceRepository.findAllByClassAndLabel(
            Classes.venue,
            SearchString.of(publishedIn, exactMatch = true),
            PageRequests.SINGLE
        ).content.singleOrNull()?.id ?: resourceService.create(
            CreateResourceUseCase.CreateCommand(
                label = publishedIn,
                classes = setOf(Classes.venue),
                contributorId = command.contributorId
            )
        )
        statementService.add(
            userId = command.contributorId,
            subject = state.paperId!!,
            predicate = Predicates.hasVenue,
            `object` = venueId
        )
    }

    private fun linkPublicationUrl(
        command: CreatePaperCommand,
        state: PaperState,
        url: URI
    ) {
        val publishedIn = literalService.create(
            userId = command.contributorId,
            label = url.toString(),
            datatype = Literals.XSD.URI.prefixedUri // TODO: Is this correct?
        ).id
        statementService.add(
            userId = command.contributorId,
            subject = state.paperId!!,
            predicate = Predicates.hasURL,
            `object` = publishedIn
        )
    }
}
