package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.ContentTypeRepresentation
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ContentType
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.SmartReview
import org.orkg.contenttypes.domain.Template
import org.orkg.contenttypes.domain.Visualization
import org.springframework.data.domain.Page

interface ContentTypeRepresentationAdapter : PaperRepresentationAdapter, ComparisonRepresentationAdapter,
    VisualizationRepresentationAdapter, TemplateRepresentationAdapter, LiteratureListRepresentationAdapter,
    SmartReviewRepresentationAdapter {

    fun Optional<ContentType>.mapToContentTypeRepresentation(): Optional<ContentTypeRepresentation> =
        map { it.toContentTypeRepresentation() }

    fun Page<ContentType>.mapToContentTypeRepresentation(): Page<ContentTypeRepresentation> =
        map { it.toContentTypeRepresentation() }

    fun ContentType.toContentTypeRepresentation(): ContentTypeRepresentation =
        when (this) {
            is Paper -> toPaperRepresentation()
            is Comparison -> toComparisonRepresentation()
            is Visualization -> toVisualizationRepresentation()
            is Template -> toTemplateRepresentation()
            is LiteratureList -> toLiteratureListRepresentation()
            is SmartReview -> toSmartReviewRepresentation()
        }
}
