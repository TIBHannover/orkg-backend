package eu.tib.orkg.prototype.community.api

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateObservatoryUseCase {
    fun create(command: CreateCommand): ObservatoryId

    data class CreateCommand(
        val id: ObservatoryId? = null,
        val name: String,
        val description: String,
        val organizationId: OrganizationId,
        val researchField: ThingId,
        val displayId: String
    )
}

interface UpdateObservatoryUseCase {
    fun changeName(id: ObservatoryId, name: String)

    fun changeDescription(id: ObservatoryId, description: String)

    fun changeResearchField(id: ObservatoryId, researchFieldId: ThingId)

    fun addOrganization(id: ObservatoryId, organizationId: OrganizationId)

    fun deleteOrganization(id: ObservatoryId, organizationId: OrganizationId)
}

interface DeleteObservatoryUseCase {
    /**
     * Remove all observatories
     */
    fun removeAll()
}
