package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ObservatoryService(
    private val postgresObservatoryRepository: ObservatoryRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val resourceService: ResourceUseCases
) : ObservatoryUseCases {
    override fun create(
        id: ObservatoryId?,
        name: String,
        description: String,
        organizationId: OrganizationId,
        researchField: ThingId?,
        displayId: String
    ): ObservatoryId {
        val oId = id ?: ObservatoryId(UUID.randomUUID())
        val org = postgresOrganizationRepository
            .findById(organizationId.value)
            .orElseThrow { OrganizationNotFound(organizationId) }
        val observatory = Observatory(
            id = oId,
            name = name,
            description = description,
            // We can pass `null` for the label here, as only the id gets stored
            researchField = if (researchField?.value != null) ResearchField(researchField.value, null) else null,
            organizationIds = mutableSetOf(OrganizationId(org.id!!)),
            displayId = displayId
        )
        postgresObservatoryRepository.save(observatory)
        return oId
    }

    override fun listObservatories(pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAll(pageable)
            .map(::expand)

    override fun findObservatoriesByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findByOrganizationId(id, pageable)
            .map(::expand)

    override fun findByName(name: String): Optional<Observatory> =
        postgresObservatoryRepository.findByName(name)
            .map(::expand)

    override fun findById(id: ObservatoryId): Optional<Observatory> =
        postgresObservatoryRepository.findById(id)
            .map(::expand)

    override fun findByDisplayId(id: String): Optional<Observatory> =
        postgresObservatoryRepository.findByDisplayId(id)
            .map(::expand)

    override fun findObservatoriesByResearchField(researchField: ThingId, pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findByResearchField(researchField, pageable)
            .map(::expand)

    override fun removeAll() = postgresObservatoryRepository.deleteAll()

    override fun changeName(id: ObservatoryId, name: String): Observatory {
        val observatory = postgresObservatoryRepository.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
            .copy(name = name)
        postgresObservatoryRepository.save(observatory)
        return expand(observatory)
    }

    override fun changeDescription(id: ObservatoryId, description: String): Observatory {
        val observatory = postgresObservatoryRepository.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
            .copy(description = description)
        postgresObservatoryRepository.save(observatory)
        return expand(observatory)
    }

    override fun changeResearchField(id: ObservatoryId, researchField: ResearchField): Observatory {
        val observatory = postgresObservatoryRepository.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
            .copy(researchField = researchField)
        postgresObservatoryRepository.save(observatory)
        return expand(observatory)
    }

    override fun addOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory {
        postgresOrganizationRepository.findById(organizationId.value)
            .orElseThrow { OrganizationNotFound(organizationId) }
        val observatory = postgresObservatoryRepository.findById(id)
            .map { it.copy(organizationIds = it.organizationIds + organizationId) }
            .orElseThrow { ObservatoryNotFound(id) }
        postgresObservatoryRepository.save(observatory)
        return expand(observatory)
    }

    override fun deleteOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory {
        val observatory = postgresObservatoryRepository.findById(id)
            .map { it.copy(organizationIds = it.organizationIds - organizationId) }
            .orElseThrow { ObservatoryNotFound(id) }
        postgresObservatoryRepository.save(observatory)
        return expand(observatory)
    }

    fun hasResearchField(response: Observatory): Boolean {
        return response.researchField?.id !== null
    }

    fun Observatory.withResearchField(resourceId: String) = this.apply {
        val resource = resourceService.findById(ThingId(resourceId))
        researchField?.id = resource.get().id.toString()
        researchField?.label = resource.get().label
    }

    private fun expand(response: Observatory): Observatory =
        if (hasResearchField(response))
            response.withResearchField(response.researchField?.id!!)
        else response
}
