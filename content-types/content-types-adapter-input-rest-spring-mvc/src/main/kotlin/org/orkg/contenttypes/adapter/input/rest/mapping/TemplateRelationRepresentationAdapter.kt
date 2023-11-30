package org.orkg.contenttypes.adapter.input.rest.mapping

import org.orkg.contenttypes.domain.TemplateRelation
import java.util.*
import org.orkg.contenttypes.adapter.input.rest.TemplateRelationRepresentation
import org.springframework.data.domain.Page

interface TemplateRelationRepresentationAdapter {

    fun Optional<TemplateRelation>.mapToTemplateRelationRepresentation() : Optional<TemplateRelationRepresentation> =
        map { it.toTemplateRelationRepresentation() }

    fun Page<TemplateRelation>.mapToTemplateRelationRepresentation() : Page<TemplateRelationRepresentation> =
        map { it.toTemplateRelationRepresentation() }

    fun TemplateRelation.toTemplateRelationRepresentation() : TemplateRelationRepresentation =
        TemplateRelationRepresentation(researchFields, researchProblems, predicate)
}
