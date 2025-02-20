package org.orkg.community.input

import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Observatory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ObservatoryUseCases :
    RetrieveObservatoryUseCase,
    CreateObservatoryUseCase,
    UpdateObservatoryUseCase,
    DeleteObservatoryUseCase

interface RetrieveObservatoryUseCase {
    fun findAll(pageable: Pageable): Page<Observatory>

    fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory>

    fun findAllResearchFields(pageable: Pageable): Page<ThingId>

    fun findByName(name: String): Optional<Observatory>

    fun findAllByNameContains(name: String, pageable: Pageable): Page<Observatory>

    fun findById(id: ObservatoryId): Optional<Observatory>

    fun findByDisplayId(id: String): Optional<Observatory>

    fun findAllByResearchFieldId(researchFieldId: ThingId, pageable: Pageable): Page<Observatory>
}

interface CreateObservatoryUseCase {
    fun create(command: CreateCommand): ObservatoryId

    data class CreateCommand(
        val id: ObservatoryId? = null,
        val name: String,
        val description: String,
        val organizations: Set<OrganizationId>,
        val researchField: ThingId,
        val displayId: String,
        val sustainableDevelopmentGoals: Set<ThingId>,
    )
}

interface UpdateObservatoryUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val id: ObservatoryId,
        val name: String? = null,
        val organizations: Set<OrganizationId>? = null,
        val description: String? = null,
        val researchField: ThingId? = null,
        val sustainableDevelopmentGoals: Set<ThingId>? = null,
    ) {
        fun hasNoContents(): Boolean =
            name == null &&
                organizations == null &&
                description == null &&
                researchField == null &&
                sustainableDevelopmentGoals == null
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
    fun deleteAll()
}
