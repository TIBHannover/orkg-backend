package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.VisualizationRepresentation
import org.orkg.contenttypes.domain.Visualization
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
