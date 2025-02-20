package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.ComparisonRelatedFigureRepresentation
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.springframework.data.domain.Page
import java.util.Optional

interface ComparisonRelatedFigureRepresentationAdapter :
    AuthorRepresentationAdapter,
    LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {
    fun Optional<ComparisonRelatedFigure>.mapToComparisonRelatedFigureRepresentation(): Optional<ComparisonRelatedFigureRepresentation> =
        map { it.toComparisonRelatedFigureRepresentation() }

    fun Page<ComparisonRelatedFigure>.mapToComparisonRelatedFigureRepresentation(): Page<ComparisonRelatedFigureRepresentation> =
        map { it.toComparisonRelatedFigureRepresentation() }

    fun ComparisonRelatedFigure.toComparisonRelatedFigureRepresentation(): ComparisonRelatedFigureRepresentation =
        ComparisonRelatedFigureRepresentation(id, label, image, description, createdAt, createdBy)
}
