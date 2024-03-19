package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.RosettaTemplateRepresentation
import org.orkg.contenttypes.domain.RosettaTemplate
import org.springframework.data.domain.Page

interface RosettaTemplateRepresentationAdapter : TemplatePropertyRepresentationAdapter {

    fun Optional<RosettaTemplate>.mapToTemplateRepresentation(): Optional<RosettaTemplateRepresentation> =
        map { it.toRosettaTemplateRepresentation() }

    fun Page<RosettaTemplate>.mapToTemplateRepresentation(): Page<RosettaTemplateRepresentation> =
        map { it.toRosettaTemplateRepresentation() }

    fun RosettaTemplate.toRosettaTemplateRepresentation(): RosettaTemplateRepresentation =
        RosettaTemplateRepresentation(
            id = id,
            label = label,
            description = description,
            formattedLabel = formattedLabel,
            targetClass = targetClass,
            properties = properties.map { it.toTemplatePropertyRepresentation() },
            isClosed = isClosed,
            createdAt = createdAt,
            createdBy = createdBy,
            organizations = organizations,
            observatories = observatories,
            visibility = visibility,
            unlistedBy = unlistedBy
        )
}
