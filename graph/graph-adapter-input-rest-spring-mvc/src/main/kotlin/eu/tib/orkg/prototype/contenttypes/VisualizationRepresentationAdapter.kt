package eu.tib.orkg.prototype.contenttypes

import eu.tib.orkg.prototype.contenttypes.api.VisualizationRepresentation
import eu.tib.orkg.prototype.contenttypes.domain.model.Visualization
import java.util.*
import org.springframework.data.domain.Page

interface VisualizationRepresentationAdapter : AuthorRepresentationAdapter, LabeledObjectRepresentationAdapter,
    PublicationInfoRepresentationAdapter {

    fun Optional<Visualization>.mapToVisualizationRepresentation() : Optional<VisualizationRepresentation> =
        map { it.toVisualizationRepresentation() }

    fun Page<Visualization>.mapToVisualizationRepresentation() : Page<VisualizationRepresentation> =
        map { it.toVisualizationRepresentation() }

    fun Visualization.toVisualizationRepresentation() : VisualizationRepresentation =
        VisualizationRepresentation(
            id = id,
            title = title,
            description = description,
            authors = authors.mapToAuthorRepresentation(),
            observatories = observatories,
            organizations = organizations,
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = visibility
        )
}
