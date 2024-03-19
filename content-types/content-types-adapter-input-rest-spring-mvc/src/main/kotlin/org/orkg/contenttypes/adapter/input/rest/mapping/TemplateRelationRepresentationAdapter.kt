package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.domain.TemplateRelations
import java.util.*
import org.orkg.contenttypes.adapter.input.rest.TemplateRelationRepresentation
import org.springframework.data.domain.Page

interface TemplateRelationRepresentationAdapter {

    fun Optional<TemplateRelations>.mapToTemplateRelationRepresentation(): Optional<TemplateRelationRepresentation> =
        map { it.toTemplateRelationRepresentation() }

    fun Page<TemplateRelations>.mapToTemplateRelationRepresentation(): Page<TemplateRelationRepresentation> =
        map { it.toTemplateRelationRepresentation() }

    fun TemplateRelations.toTemplateRelationRepresentation(): TemplateRelationRepresentation =
        TemplateRelationRepresentation(researchFields, researchProblems, predicate)
}
