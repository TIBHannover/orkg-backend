package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.ComparisonRelatedFigureRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedFigure
import java.util.*
import org.springframework.data.domain.Page

interface ComparisonRelatedFigureRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<ComparisonRelatedFigure>.mapToComparisonRelatedFigureRepresentation() : Optional<ComparisonRelatedFigureRepresentation> =
        map { it.toComparisonRelatedFigureRepresentation() }

    fun Page<ComparisonRelatedFigure>.mapToComparisonRelatedFigureRepresentation() : Page<ComparisonRelatedFigureRepresentation> =
        map { it.toComparisonRelatedFigureRepresentation() }

    fun ComparisonRelatedFigure.toComparisonRelatedFigureRepresentation() : ComparisonRelatedFigureRepresentation =
        ComparisonRelatedFigureRepresentation(id, label, image, description)
}
