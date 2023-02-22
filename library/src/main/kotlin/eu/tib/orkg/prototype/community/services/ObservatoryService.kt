package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.ObservatoryEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresObservatoryService(
    private val postgresObservatoryRepository: PostgresObservatoryRepository,
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
    ): Observatory {
        val observatoryId = id ?: ObservatoryId(UUID.randomUUID())
        val org = postgresOrganizationRepository
            .findById(organizationId.value)
            .orElseThrow { OrganizationNotFound(organizationId) } // FIXME: should always have an ID
        val newObservatory = ObservatoryEntity().apply {
            this.id = observatoryId.value
            this.name = name
            this.description = description
            this.researchField = researchField?.value
            organizations = mutableSetOf(org)
            this.displayId = displayId
        }

        val response = postgresObservatoryRepository.save(newObservatory).toObservatory()
        return expand(response)
    }

    override fun listObservatories(): List<Observatory> =
        postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)
            .map(::expand)

    override fun findObservatoriesByOrganizationId(id: OrganizationId): List<Observatory> =
        postgresObservatoryRepository.findByOrganizationsId(id.value)
            .map(ObservatoryEntity::toObservatory)
            .map(::expand)

    override fun findByName(name: String): Optional<Observatory> =
        postgresObservatoryRepository.findByName(name)
            .map(ObservatoryEntity::toObservatory)
            .map(::expand)

    override fun findById(id: ObservatoryId): Optional<Observatory> =
        postgresObservatoryRepository.findById(id.value)
            .map(ObservatoryEntity::toObservatory)
            .map(::expand)

    override fun findByDisplayId(id: String): Optional<Observatory> =
        postgresObservatoryRepository.findByDisplayId(id)
            .map(ObservatoryEntity::toObservatory)
            .map(::expand)

    override fun findObservatoriesByResearchField(researchField: String): List<Observatory> =
        postgresObservatoryRepository.findByResearchField(researchField)
            .map(ObservatoryEntity::toObservatory)
            .map(::expand)

    override fun removeAll() = postgresObservatoryRepository.deleteAll()

    override fun changeName(id: ObservatoryId, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id.value).get().apply {
            name = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    override fun changeDescription(id: ObservatoryId, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id.value).get().apply {
            description = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    override fun changeResearchField(id: ObservatoryId, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id.value).get().apply {
            researchField = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    override fun updateOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory {
        val org = postgresOrganizationRepository
            .findById(organizationId.value)
            .orElseThrow { OrganizationNotFound(organizationId) }
        val entity = postgresObservatoryRepository.findById(id.value).get()
        entity.organizations?.add(org)

        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
    }

    override fun deleteOrganization(id: ObservatoryId, organizationId: OrganizationId): Observatory {
        val org = postgresOrganizationRepository
            .findById(organizationId.value)
            .orElseThrow { OrganizationNotFound(organizationId) }
        val entity = postgresObservatoryRepository.findById(id.value).get()
        entity.organizations?.remove(org)

        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return expand(response)
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
