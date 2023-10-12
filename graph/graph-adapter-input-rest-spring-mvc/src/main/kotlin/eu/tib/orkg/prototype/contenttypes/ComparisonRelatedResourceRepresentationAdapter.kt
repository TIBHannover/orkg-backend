package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.ComparisonRelatedResourceRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedResource
import java.util.*
import org.springframework.data.domain.Page

interface ComparisonRelatedResourceRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<ComparisonRelatedResource>.mapToComparisonRelatedResourceRepresentation() : Optional<ComparisonRelatedResourceRepresentation> =
        map { it.toComparisonRelatedResourceRepresentation() }

    fun Page<ComparisonRelatedResource>.mapToComparisonRelatedResourceRepresentation() : Page<ComparisonRelatedResourceRepresentation> =
        map { it.toComparisonRelatedResourceRepresentation() }

    fun ComparisonRelatedResource.toComparisonRelatedResourceRepresentation() : ComparisonRelatedResourceRepresentation =
        ComparisonRelatedResourceRepresentation(id, label, image, url, description)
}
