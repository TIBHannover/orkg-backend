package org.orkg.community.input

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId

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
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ObservatoryId,
        val name: String? = null,
        val organizations: Set<OrganizationId>? = null,
        val description: String? = null,
        val researchField: ThingId? = null
    ) {
        fun hasNoContents(): Boolean =
            name == null && organizations == null && description == null && researchField == null
    }

    // legacy methods:
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
