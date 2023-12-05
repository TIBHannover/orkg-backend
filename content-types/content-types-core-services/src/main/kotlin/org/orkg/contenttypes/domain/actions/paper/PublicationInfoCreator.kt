package org.orkg.contenttypes.domain.actions.paper

import java.net.URI
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PublicationInfo
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository

abstract class PublicationInfoCreator(
    protected val resourceService: ResourceUseCases,
    protected val resourceRepository: ResourceRepository,
    protected val statementService: StatementUseCases,
    protected val literalService: LiteralUseCases
) {
    internal fun create(contributorId: ContributorId, publicationInfo: PublicationInfo, subjectId: ThingId) {
        if (publicationInfo.publishedMonth != null) {
            linkPublicationMonth(contributorId, subjectId, publicationInfo.publishedMonth!!)
        }
        if (publicationInfo.publishedYear != null) {
            linkPublicationYear(contributorId, subjectId, publicationInfo.publishedYear!!)
        }
        if (publicationInfo.publishedIn != null) {
            linkPublicationVenue(contributorId, subjectId, publicationInfo.publishedIn!!)
        }
        if (publicationInfo.url != null) {
            linkPublicationUrl(contributorId, subjectId, publicationInfo.url!!)
        }
    }

    protected fun linkPublicationMonth(
        contributorId: ContributorId,
        subjectId: ThingId,
        publishedMonth: Int
    ) {
        val publishedIn = literalService.create(
            userId = contributorId,
            label = publishedMonth.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        ).id
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.monthPublished,
            `object` = publishedIn
        )
    }

    protected fun linkPublicationYear(
        contributorId: ContributorId,
        subjectId: ThingId,
        publishedYear: Long
    ) {
        val publishedIn = literalService.create(
            userId = contributorId,
            label = publishedYear.toString(),
            datatype = Literals.XSD.INT.prefixedUri
        ).id
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.yearPublished,
            `object` = publishedIn
        )
    }

    protected fun linkPublicationVenue(
        contributorId: ContributorId,
        subjectId: ThingId,
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
                contributorId = contributorId
            )
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.hasVenue,
            `object` = venueId
        )
    }

    protected fun linkPublicationUrl(
        contributorId: ContributorId,
        subjectId: ThingId,
        url: URI
    ) {
        val publishedIn = literalService.create(
            userId = contributorId,
            label = url.toString(),
            datatype = Literals.XSD.URI.prefixedUri // TODO: Is this correct?
        ).id
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.hasURL,
            `object` = publishedIn
        )
    }
}
