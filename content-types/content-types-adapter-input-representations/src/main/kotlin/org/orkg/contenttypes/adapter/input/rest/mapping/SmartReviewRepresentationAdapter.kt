package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.SmartReviewComparisonSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewOntologySectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewPredicateSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewResourceSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewTextSectionRepresentation
import org.orkg.contenttypes.adapter.input.rest.SmartReviewVisualizationSectionRepresentation
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.SmartReviewComparisonSection
import org.orkg.contenttypes.domain.SmartReviewOntologySection
import org.orkg.contenttypes.domain.SmartReviewPredicateSection
import org.orkg.contenttypes.domain.SmartReviewResourceSection
import org.orkg.contenttypes.domain.SmartReviewSection
import org.orkg.contenttypes.domain.SmartReviewTextSection
import org.orkg.contenttypes.domain.SmartReviewVisualizationSection
import org.springframework.data.domain.Page
import java.util.Optional

interface SmartReviewRepresentationAdapter :
    AuthorRepresentationAdapter,
    VersionRepresentationAdapter,
    LabeledObjectRepresentationAdapter,
    ThingReferenceRepresentationAdapter {
    fun Optional<SmartReview>.mapToSmartReviewRepresentation(): Optional<SmartReviewRepresentation> =
        map { it.toSmartReviewRepresentation() }

    fun Page<SmartReview>.mapToSmartReviewRepresentation(): Page<SmartReviewRepresentation> =
        map { it.toSmartReviewRepresentation() }

    fun SmartReview.toSmartReviewRepresentation(): SmartReviewRepresentation =
        SmartReviewRepresentation(
            id = id,
            title = title,
            researchFields = researchFields,
            identifiers = identifiers,
            authors = authors.mapToAuthorRepresentation(),
            versions = versions.toVersionInfoRepresentation(),
            sustainableDevelopmentGoals = sustainableDevelopmentGoals.mapToLabeledObjectRepresentation(),
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = visibility,
            unlistedBy = unlistedBy,
            published = published,
            sections = sections.map { it.toSmartReviewSectionRepresentation() },
            references = references,
            acknowledgements = acknowledgements
        )

    private fun SmartReviewSection.toSmartReviewSectionRepresentation(): SmartReviewSectionRepresentation =
        when (this) {
            is SmartReviewComparisonSection -> SmartReviewComparisonSectionRepresentation(
                id = id,
                heading = heading,
                comparison = comparison?.toResourceReferenceRepresentation()
            )
            is SmartReviewVisualizationSection -> SmartReviewVisualizationSectionRepresentation(
                id = id,
                heading = heading,
                visualization = visualization?.toResourceReferenceRepresentation()
            )
            is SmartReviewResourceSection -> SmartReviewResourceSectionRepresentation(
                id = id,
                heading = heading,
                resource = resource?.toResourceReferenceRepresentation()
            )
            is SmartReviewPredicateSection -> SmartReviewPredicateSectionRepresentation(
                id = id,
                heading = heading,
                predicate = predicate?.toPredicateReferenceRepresentation()
            )
            is SmartReviewOntologySection -> SmartReviewOntologySectionRepresentation(
                id = id,
                heading = heading,
                entities = entities.map { it.toThingReferenceRepresentation() },
                predicates = predicates.map { it.toPredicateReferenceRepresentation() }
            )
            is SmartReviewTextSection -> SmartReviewTextSectionRepresentation(id, heading, classes, text)
        }
}
