package org.orkg.contenttypes.domain.actions

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.PublicationInfoDefinition
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
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
    internal fun create(contributorId: ContributorId, publicationInfo: PublicationInfoDefinition, subjectId: ThingId) {
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
        val monthLiteralId = literalService.create(
            CreateCommand(
                contributorId = contributorId,
                label = publishedMonth.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.monthPublished,
            `object` = monthLiteralId
        )
    }

    protected fun linkPublicationYear(
        contributorId: ContributorId,
        subjectId: ThingId,
        publishedYear: Long
    ) {
        val yearLiteralId = literalService.create(
            CreateCommand(
                contributorId = contributorId,
                label = publishedYear.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.yearPublished,
            `object` = yearLiteralId
        )
    }

    protected fun linkPublicationVenue(
        contributorId: ContributorId,
        subjectId: ThingId,
        publishedIn: String
    ) {
        val venueId = resourceRepository.findAll(
            includeClasses = setOf(Classes.venue),
            label = SearchString.of(publishedIn, exactMatch = true),
            pageable = PageRequests.SINGLE
        ).content.singleOrNull()?.id ?: resourceService.createUnsafe(
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
        url: ParsedIRI
    ) {
        val urlLiteralId = literalService.create(
            CreateCommand(
                contributorId = contributorId,
                label = url.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        )
        statementService.add(
            userId = contributorId,
            subject = subjectId,
            predicate = Predicates.hasURL,
            `object` = urlLiteralId
        )
    }
}
