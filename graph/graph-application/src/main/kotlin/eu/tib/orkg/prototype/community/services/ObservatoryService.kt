package eu.tib.orkg.prototype.community.services

import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.api.CreateObservatoryUseCase
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.application.ObservatoryNotFound
import eu.tib.orkg.prototype.community.application.OrganizationNotFound
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val researchFieldClassId = ThingId("ResearchField")

@Service
@Transactional
class ObservatoryService(
    private val postgresObservatoryRepository: ObservatoryRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val resourceRepository: ResourceRepository
) : ObservatoryUseCases {
    override fun create(command: CreateObservatoryUseCase.CreateCommand): ObservatoryId {
        val id = command.id ?: ObservatoryId(UUID.randomUUID())
        val organization = postgresOrganizationRepository
            .findById(command.organizationId.value)
            .orElseThrow { OrganizationNotFound(command.organizationId) }
        val researchField = resourceRepository.findById(command.researchField)
            .filter { resource -> researchFieldClassId in resource.classes }
            .orElseThrow { ResearchFieldNotFound(command.researchField) }
        val observatory = Observatory(
            id = id,
            name = command.name,
            description = command.description,
            researchField = researchField.id,
            organizationIds = mutableSetOf(OrganizationId(organization.id!!)),
            displayId = command.displayId
        )
        postgresObservatoryRepository.save(observatory)
        return id
    }

    override fun findAll(pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAll(pageable)

    override fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAllByOrganizationId(id, pageable)

    override fun findAllResearchFields(pageable: Pageable): Page<ThingId> =
        postgresObservatoryRepository.findAllResearchFields(pageable)

    override fun findByName(name: String): Optional<Observatory> =
        postgresObservatoryRepository.findByName(name)

    override fun findAllByNameContains(name: String, pageable: Pageable): Page<Observatory> =
        postgresObservatoryRepository.findAllByNameContains(name, pageable)

    override fun findById(id: ObservatoryId): Optional<Observatory> =
        postgresObservatoryRepository.findById(id)

    override fun findByDisplayId(id: String): Optional<Observatory> =
        postgresObservatoryRepository.findByDisplayId(id)

    override fun findAllByResearchField(researchFieldId: ThingId, pageable: Pageable): Page<Observatory> {
        resourceRepository.findById(researchFieldId)
            .filter { resource -> researchFieldClassId in resource.classes }
            .orElseThrow { ResearchFieldNotFound(researchFieldId) }
        return postgresObservatoryRepository.findAllByResearchField(researchFieldId, pageable)
    }

    override fun removeAll() = postgresObservatoryRepository.deleteAll()

    override fun changeName(id: ObservatoryId, name: String) {
        val observatory = postgresObservatoryRepository.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
            .copy(name = name)
        postgresObservatoryRepository.save(observatory)
    }

    override fun changeDescription(id: ObservatoryId, description: String) {
        val observatory = postgresObservatoryRepository.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
            .copy(description = description)
        postgresObservatoryRepository.save(observatory)
    }

    override fun changeResearchField(id: ObservatoryId, researchFieldId: ThingId) {
        val researchField = resourceRepository.findByIdAndClasses(researchFieldId, setOf(ThingId("ResearchField")))
            ?: throw ResearchFieldNotFound(researchFieldId)
        val observatory = postgresObservatoryRepository.findById(id)
            .orElseThrow { ObservatoryNotFound(id) }
            .copy(researchField = researchField.id)
        postgresObservatoryRepository.save(observatory)
    }

    override fun addOrganization(id: ObservatoryId, organizationId: OrganizationId) {
        postgresOrganizationRepository.findById(organizationId.value)
            .orElseThrow { OrganizationNotFound(organizationId) }
        val observatory = postgresObservatoryRepository.findById(id)
            .map { it.copy(organizationIds = it.organizationIds + organizationId) }
            .orElseThrow { ObservatoryNotFound(id) }
        postgresObservatoryRepository.save(observatory)
    }

    override fun deleteOrganization(id: ObservatoryId, organizationId: OrganizationId) {
        val observatory = postgresObservatoryRepository.findById(id)
            .map { it.copy(organizationIds = it.organizationIds - organizationId) }
            .orElseThrow { ObservatoryNotFound(id) }
        postgresObservatoryRepository.save(observatory)
    }
}
