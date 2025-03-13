package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface TemplateInstanceUseCases :
    RetrieveTemplateInstanceUseCase,
    UpdateTemplateInstanceUseCase

interface RetrieveTemplateInstanceUseCase {
    fun findById(
        templateId: ThingId,
        id: ThingId,
        nested: Boolean = false,
    ): Optional<TemplateInstance>

    fun findAll(
        templateId: ThingId,
        pageable: Pageable,
        nested: Boolean = false,
        label: SearchString? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<TemplateInstance>
}

interface UpdateTemplateInstanceUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val subject: ThingId,
        val templateId: ThingId,
        val contributorId: ContributorId,
        val statements: Map<ThingId, List<String>>,
        override val resources: Map<String, CreateResourceCommandPart> = emptyMap(),
        override val literals: Map<String, CreateLiteralCommandPart> = emptyMap(),
        override val predicates: Map<String, CreatePredicateCommandPart> = emptyMap(),
        override val classes: Map<String, CreateClassCommandPart> = emptyMap(),
        override val lists: Map<String, CreateListCommandPart> = emptyMap(),
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
    ) : CreateThingsCommand
}
