package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.domain.LiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.TemplateProperty
import java.util.*
import org.orkg.contenttypes.adapter.input.rest.LiteralTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.ResourceTemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.springframework.data.domain.Page

interface TemplatePropertyRelationRepresentationAdapter {

    fun Optional<TemplateProperty>.mapToTemplatePropertyRepresentation(): Optional<TemplatePropertyRepresentation> =
        map { it.toTemplatePropertyRepresentation() }

    fun Page<TemplateProperty>.mapToTemplatePropertyRepresentation(): Page<TemplatePropertyRepresentation> =
        map { it.toTemplatePropertyRepresentation() }

    fun TemplateProperty.toTemplatePropertyRepresentation(): TemplatePropertyRepresentation =
        when (this) {
            is LiteralTemplateProperty -> LiteralTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, pattern, path, createdAt, createdBy, datatype
            )
            is ResourceTemplateProperty -> ResourceTemplatePropertyRepresentation(
                id, label, placeholder, description, order, minCount, maxCount, pattern, path, createdAt, createdBy, `class`
            )
        }
}
