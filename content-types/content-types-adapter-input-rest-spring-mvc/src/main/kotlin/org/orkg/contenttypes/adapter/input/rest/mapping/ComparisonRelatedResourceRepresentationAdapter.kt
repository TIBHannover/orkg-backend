package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.ComparisonRelatedResourceRepresentation
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.springframework.data.domain.Page

interface ComparisonRelatedResourceRepresentationAdapter : AuthorRepresentationAdapter,
    LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<ComparisonRelatedResource>.mapToComparisonRelatedResourceRepresentation() : Optional<ComparisonRelatedResourceRepresentation> =
        map { it.toComparisonRelatedResourceRepresentation() }

    fun Page<ComparisonRelatedResource>.mapToComparisonRelatedResourceRepresentation() : Page<ComparisonRelatedResourceRepresentation> =
        map { it.toComparisonRelatedResourceRepresentation() }

    fun ComparisonRelatedResource.toComparisonRelatedResourceRepresentation() : ComparisonRelatedResourceRepresentation =
        ComparisonRelatedResourceRepresentation(id, label, image, url, description)
}
