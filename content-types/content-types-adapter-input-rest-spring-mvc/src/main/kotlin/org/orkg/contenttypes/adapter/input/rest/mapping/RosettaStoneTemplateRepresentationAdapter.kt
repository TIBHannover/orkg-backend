package org.orkg.contenttypes.adapter.input.rest.mapping

import java.util.*
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneTemplateRepresentation
import org.orkg.contenttypes.domain.RosettaStoneTemplate
import org.springframework.data.domain.Page

interface RosettaStoneTemplateRepresentationAdapter : TemplatePropertyRepresentationAdapter {

    fun Optional<RosettaStoneTemplate>.mapToRosettaStoneTemplateRepresentation(): Optional<RosettaStoneTemplateRepresentation> =
        map { it.toRosettaStoneTemplateRepresentation() }

    fun Page<RosettaStoneTemplate>.mapToRosettaStoneTemplateRepresentation(): Page<RosettaStoneTemplateRepresentation> =
        map { it.toRosettaStoneTemplateRepresentation() }

    fun RosettaStoneTemplate.toRosettaStoneTemplateRepresentation(): RosettaStoneTemplateRepresentation =
        RosettaStoneTemplateRepresentation(
            id = id,
            label = label,
            description = description,
            formattedLabel = formattedLabel?.value,
            targetClass = targetClass,
            properties = properties.map { it.toTemplatePropertyRepresentation() },
            createdAt = createdAt,
            createdBy = createdBy,
            organizations = organizations,
            observatories = observatories,
            visibility = visibility,
            unlistedBy = unlistedBy
        )
}
