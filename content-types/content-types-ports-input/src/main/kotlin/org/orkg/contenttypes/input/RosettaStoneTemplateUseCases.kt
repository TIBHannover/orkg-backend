package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface RosettaStoneTemplateUseCases :
    RetrieveRosettaStoneTemplateUseCase,
    CreateRosettaStoneTemplateUseCase,
    UpdateRosettaStoneTemplateUseCase,
    DeleteRosettaStoneTemplateUseCase

interface RetrieveRosettaStoneStatementUseCase {
    fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement>

    fun findAll(
        pageable: Pageable,
        context: ThingId? = null,
        templateId: ThingId? = null,
        templateTargetClassId: ThingId? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
    ): Page<RosettaStoneStatement>
}

interface CreateRosettaStoneTemplateUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        val description: String,
        val formattedLabel: FormattedLabel,
        val exampleUsage: String,
        val properties: List<TemplatePropertyCommand>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
    )
}

interface UpdateRosettaStoneTemplateUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val templateId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val description: String?,
        val formattedLabel: FormattedLabel?,
        val exampleUsage: String?,
        val properties: List<TemplatePropertyCommand>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
    )
}

interface DeleteRosettaStoneTemplateUseCase {
    fun delete(id: ThingId, contributorId: ContributorId)
}
