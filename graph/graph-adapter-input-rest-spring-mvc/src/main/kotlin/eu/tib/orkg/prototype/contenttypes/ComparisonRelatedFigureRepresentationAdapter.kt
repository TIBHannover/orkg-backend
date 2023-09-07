package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.application.ComparisonRelatedFigureRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedFigure
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page

interface ComparisonRelatedFigureRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<ComparisonRelatedFigure>.mapToComparisonRelatedFigureRepresentation() : Optional<ComparisonRelatedFigureRepresentation> =
        map { it.toComparisonRelatedFigureRepresentation() }

    fun Page<ComparisonRelatedFigure>.mapToComparisonRelatedFigureRepresentation() : Page<ComparisonRelatedFigureRepresentation> =
        map { it.toComparisonRelatedFigureRepresentation() }

    fun ComparisonRelatedFigure.toComparisonRelatedFigureRepresentation() : ComparisonRelatedFigureRepresentation =
        object : ComparisonRelatedFigureRepresentation {
            override val id: ThingId = this@toComparisonRelatedFigureRepresentation.id
            override val label: String = this@toComparisonRelatedFigureRepresentation.label
            override val image: String? = this@toComparisonRelatedFigureRepresentation.image
            override val description: String? = this@toComparisonRelatedFigureRepresentation.description
        }
}
