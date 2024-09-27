package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.NumberLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.OtherLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.ResourceTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.StringLiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.UntypedTemplatePropertyRepresentation
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.OtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.springframework.data.domain.Page

interface TemplatePropertyRepresentationAdapter : ThingReferenceRepresentationAdapter {

    fun Optional<TemplateProperty>.mapToTemplatePropertyRepresentation(): Optional<TemplatePropertyRepresentation> =
        map { it.toTemplatePropertyRepresentation() }

    fun Page<TemplateProperty>.mapToTemplatePropertyRepresentation(): Page<TemplatePropertyRepresentation> =
        map { it.toTemplatePropertyRepresentation() }

    fun TemplateProperty.toTemplatePropertyRepresentation(): TemplatePropertyRepresentation =
        when (this) {
            is UntypedTemplateProperty -> UntypedTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, path, createdAt, createdBy
            )
            is StringLiteralTemplateProperty -> StringLiteralTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, pattern, path, createdAt, createdBy, datatype.toClassReferenceRepresentation()
            )
            is NumberLiteralTemplateProperty -> NumberLiteralTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, minInclusive, maxInclusive, path, createdAt, createdBy, datatype.toClassReferenceRepresentation()
            )
            is OtherLiteralTemplateProperty -> OtherLiteralTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, path, createdAt, createdBy, datatype.toClassReferenceRepresentation()
            )
            is ResourceTemplateProperty -> ResourceTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, path, createdAt, createdBy, `class`
            )
        }
}
