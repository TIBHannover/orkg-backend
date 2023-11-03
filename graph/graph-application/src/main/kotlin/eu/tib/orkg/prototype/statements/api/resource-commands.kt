package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateResourceUseCase {
    fun create(command: CreateCommand): ThingId

    // legacy methods:
    fun create(label: String): Resource
    fun create(
        userId: ContributorId,
        label: String,
        observatoryId: ObservatoryId,
        extractionMethod: ExtractionMethod,
        organizationId: OrganizationId
    ): Resource

    data class CreateCommand(
        val id: ThingId? = null,
        val label: String,
        val classes: Set<ThingId> = emptySet(),
        val extractionMethod: ExtractionMethod = ExtractionMethod.UNKNOWN,
        val contributorId: ContributorId? = null,
        val observatoryId: ObservatoryId? = null,
        val organizationId: OrganizationId? = null,
    )
}

interface UpdateResourceUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ThingId,
        val label: String? = null,
        val classes: Set<ThingId>? = null,
        val observatoryId: ObservatoryId? = null,
        val organizationId: OrganizationId? = null,
        val extractionMethod: ExtractionMethod? = null
    )
}

interface DeleteResourceUseCase {
    // legacy methods:
    fun delete(id: ThingId)
    fun removeAll()
}
