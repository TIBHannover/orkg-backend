package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ComparisonRepresentation
import org.orkg.contenttypes.domain.Comparison
import org.springframework.data.domain.Page
import java.util.Optional

interface ComparisonRepresentationAdapter :
    AuthorRepresentationAdapter,
    LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter,
    VersionRepresentationAdapter {
    fun Optional<Comparison>.mapToComparisonRepresentation(): Optional<ComparisonRepresentation> =
        map { it.toComparisonRepresentation() }

    fun Page<Comparison>.mapToComparisonRepresentation(): Page<ComparisonRepresentation> =
        map { it.toComparisonRepresentation() }

    fun Comparison.toComparisonRepresentation(): ComparisonRepresentation =
        ComparisonRepresentation(
            id = id,
            title = title,
            description = description,
            researchFields = researchFields.mapToLabeledObjectRepresentation(),
            identifiers = identifiers,
            publicationInfo = publicationInfo.toPublicationInfoRepresentation(),
            authors = authors.mapToAuthorRepresentation(),
            sustainableDevelopmentGoals = sustainableDevelopmentGoals.mapToLabeledObjectRepresentation(),
            contributions = contributions.mapToLabeledObjectRepresentation(),
            config = config,
            data = data,
            visualizations = visualizations.mapToLabeledObjectRepresentation(),
            relatedFigures = relatedFigures.mapToLabeledObjectRepresentation(),
            relatedResources = relatedResources.mapToLabeledObjectRepresentation(),
            references = references,
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            versions = versions.toVersionInfoRepresentation(),
            isAnonymized = isAnonymized,
            visibility = visibility,
            unlistedBy = unlistedBy,
            published = published
        )
}
