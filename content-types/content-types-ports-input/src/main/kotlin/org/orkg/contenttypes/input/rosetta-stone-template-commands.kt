package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.FormattedLabel

interface CreateRosettaStoneTemplateUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val label: String,
        val description: String,
        val formattedLabel: FormattedLabel,
        val exampleUsage: String,
        val properties: List<TemplatePropertyDefinition>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>
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
        val properties: List<TemplatePropertyDefinition>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?
    )
}

interface DeleteRosettaStoneTemplateUseCase {
    fun delete(id: ThingId, contributorId: ContributorId)
}
