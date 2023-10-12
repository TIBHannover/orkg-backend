package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.ComparisonRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import java.util.*
import org.springframework.data.domain.Page

interface ComparisonRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Comparison>.mapToComparisonRepresentation() : Optional<ComparisonRepresentation> =
        map { it.toComparisonRepresentation() }

    fun Page<Comparison>.mapToComparisonRepresentation() : Page<ComparisonRepresentation> =
        map { it.toComparisonRepresentation() }

    fun Comparison.toComparisonRepresentation() : ComparisonRepresentation =
        ComparisonRepresentation(
            id = id,
            title = title,
            description = description,
            researchFields = researchFields.mapToLabeledObjectRepresentation(),
            identifiers = identifiers,
            publicationInfo = publicationInfo.toPublicationInfoRepresentation(),
            authors = authors.mapToAuthorRepresentation(),
            contributions = contributions.mapToLabeledObjectRepresentation(),
            visualizations = visualizations.mapToLabeledObjectRepresentation(),
            relatedFigures = relatedFigures.mapToLabeledObjectRepresentation(),
            relatedResources = relatedResources.mapToLabeledObjectRepresentation(),
            references = references,
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            previousVersion = previousVersion,
            isAnonymized = isAnonymized,
            visibility = visibility
        )
}
