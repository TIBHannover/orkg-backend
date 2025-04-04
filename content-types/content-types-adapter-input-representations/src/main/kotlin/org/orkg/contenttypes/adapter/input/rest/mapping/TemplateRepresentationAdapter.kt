package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.adapter.input.rest.TemplateRepresentation
import org.orkg.contenttypes.domain.Template
import org.springframework.data.domain.Page
import java.util.Optional

interface TemplateRepresentationAdapter :
    TemplateRelationRepresentationAdapter,
    TemplatePropertyRepresentationAdapter {
    fun Optional<Template>.mapToTemplateRepresentation(): Optional<TemplateRepresentation> =
        map { it.toTemplateRepresentation() }

    fun Page<Template>.mapToTemplateRepresentation(): Page<TemplateRepresentation> =
        map { it.toTemplateRepresentation() }

    fun Template.toTemplateRepresentation(): TemplateRepresentation =
        TemplateRepresentation(
            id = id,
            label = label,
            description = description,
            formattedLabel = formattedLabel?.value,
            targetClass = targetClass.toClassReferenceRepresentation(),
            relations = relations.toTemplateRelationRepresentation(),
            properties = properties.map { it.toTemplatePropertyRepresentation() },
            isClosed = isClosed,
            createdAt = createdAt,
            createdBy = createdBy,
            organizations = organizations,
            observatories = observatories,
            extractionMethod = extractionMethod,
            visibility = visibility,
            unlistedBy = unlistedBy
        )
}
