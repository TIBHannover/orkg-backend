package org.orkg.contenttypes.domain.actions

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchString
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.input.UnsafeStatementUseCases
import org.orkg.graph.output.ResourceRepository

abstract class PublicationInfoCreator(
    protected val unsafeResourceUseCases: UnsafeResourceUseCases,
    protected val resourceRepository: ResourceRepository,
    protected val unsafeStatementUseCases: UnsafeStatementUseCases,
    protected val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) {
    internal fun create(contributorId: ContributorId, publicationInfo: PublicationInfoCommand, subjectId: ThingId) {
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
        publishedMonth: Int,
    ) {
        val monthLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = publishedMonth.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.monthPublished,
                objectId = monthLiteralId
            )
        )
    }

    protected fun linkPublicationYear(
        contributorId: ContributorId,
        subjectId: ThingId,
        publishedYear: Long,
    ) {
        val yearLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = publishedYear.toString(),
                datatype = Literals.XSD.INT.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.yearPublished,
                objectId = yearLiteralId
            )
        )
    }

    protected fun linkPublicationVenue(
        contributorId: ContributorId,
        subjectId: ThingId,
        publishedIn: String,
    ) {
        val venueId = resourceRepository.findAll(
            includeClasses = setOf(Classes.venue),
            label = SearchString.of(publishedIn, exactMatch = true),
            pageable = PageRequests.SINGLE
        ).content.singleOrNull()?.id ?: unsafeResourceUseCases.create(
            CreateResourceUseCase.CreateCommand(
                contributorId = contributorId,
                label = publishedIn,
                classes = setOf(Classes.venue)
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.hasVenue,
                objectId = venueId
            )
        )
    }

    protected fun linkPublicationUrl(
        contributorId: ContributorId,
        subjectId: ThingId,
        url: ParsedIRI,
    ) {
        val urlLiteralId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = contributorId,
                label = url.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        )
        unsafeStatementUseCases.create(
            CreateStatementUseCase.CreateCommand(
                contributorId = contributorId,
                subjectId = subjectId,
                predicateId = Predicates.hasURL,
                objectId = urlLiteralId
            )
        )
    }
}
