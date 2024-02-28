package org.orkg.contenttypes.domain.actions.paper

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository

class PaperPublicationInfoUpdater(
    resourceService: ResourceUseCases,
    resourceRepository: ResourceRepository,
    statementService: StatementUseCases,
    literalService: LiteralUseCases
) : PublicationInfoCreator(resourceService, resourceRepository, statementService, literalService), UpdatePaperAction {
    override operator fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.publicationInfo != null) {
            if (state.paper?.publicationInfo?.publishedMonth != command.publicationInfo!!.publishedMonth) {
                updateOrLinkPublicationMonth(
                    oldMonth = state.paper?.publicationInfo?.publishedMonth,
                    newMonth = command.publicationInfo!!.publishedMonth,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
            if (state.paper?.publicationInfo?.publishedYear != command.publicationInfo!!.publishedYear) {
                updateOrLinkPublicationYear(
                    oldYear = state.paper?.publicationInfo?.publishedYear,
                    newYear = command.publicationInfo!!.publishedYear,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
            if (state.paper?.publicationInfo?.publishedIn?.label != command.publicationInfo!!.publishedIn) {
                updateOrLinkPublicationIn(
                    oldVenue = state.paper?.publicationInfo?.publishedIn?.label,
                    newVenue = command.publicationInfo!!.publishedIn,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
            if (state.paper?.publicationInfo?.url != command.publicationInfo!!.url) {
                updateOrLinkPublicationUrl(
                    oldUrl = state.paper?.publicationInfo?.url,
                    newUrl = command.publicationInfo!!.url,
                    contributorId = command.contributorId,
                    subjectId = command.paperId
                )
            }
        }
        return state
    }

    private fun updateOrLinkPublicationMonth(
        oldMonth: Int?,
        newMonth: Int?,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        if (oldMonth != null) {
            val statement = statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.monthPublished,
                pageable = PageRequests.SINGLE
            ).single()
            statementService.delete(statement.id!!)
        }

        if (newMonth != null) {
            linkPublicationMonth(contributorId, subjectId, newMonth)
        }
    }

    private fun updateOrLinkPublicationYear(
        oldYear: Long?,
        newYear: Long?,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        if (oldYear != null) {
            val statement = statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.yearPublished,
                pageable = PageRequests.SINGLE
            ).single()
            statementService.delete(statement.id!!)
        }

        if (newYear != null) {
            linkPublicationYear(contributorId, subjectId, newYear)
        }
    }

    private fun updateOrLinkPublicationIn(
        oldVenue: String?,
        newVenue: String?,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        if (oldVenue != null) {
            val statement = statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasVenue,
                pageable = PageRequests.SINGLE
            ).single()
            statementService.delete(statement.id!!)
        }

        if (newVenue != null) {
            linkPublicationVenue(contributorId, subjectId, newVenue)
        }
    }

    private fun updateOrLinkPublicationUrl(
        oldUrl: URI?,
        newUrl: URI?,
        contributorId: ContributorId,
        subjectId: ThingId
    ) {
        if (oldUrl != null) {
            val statement = statementService.findAll(
                subjectId = subjectId,
                predicateId = Predicates.hasURL,
                pageable = PageRequests.SINGLE
            ).single { it.`object` is Literal }
            statementService.delete(statement.id!!)
        }

        if (newUrl != null) {
            linkPublicationUrl(contributorId, subjectId, newUrl)
        }
    }
}
