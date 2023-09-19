package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.application.ComparisonRelatedResourceRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedResource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page

interface ComparisonRelatedResourceRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<ComparisonRelatedResource>.mapToComparisonRelatedResourceRepresentation() : Optional<ComparisonRelatedResourceRepresentation> =
        map { it.toComparisonRelatedResourceRepresentation() }

    fun Page<ComparisonRelatedResource>.mapToComparisonRelatedResourceRepresentation() : Page<ComparisonRelatedResourceRepresentation> =
        map { it.toComparisonRelatedResourceRepresentation() }

    fun ComparisonRelatedResource.toComparisonRelatedResourceRepresentation() : ComparisonRelatedResourceRepresentation =
        object : ComparisonRelatedResourceRepresentation {
            override val id: ThingId = this@toComparisonRelatedResourceRepresentation.id
            override val label: String = this@toComparisonRelatedResourceRepresentation.label
            override val image: String? = this@toComparisonRelatedResourceRepresentation.image
            override val url: String? = this@toComparisonRelatedResourceRepresentation.url
            override val description: String? = this@toComparisonRelatedResourceRepresentation.description
        }
}
