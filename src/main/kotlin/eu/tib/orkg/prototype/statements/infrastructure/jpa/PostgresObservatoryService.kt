package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresObservatoryRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresObservatoryService(
    private val postgresObservatoryRepository: PostgresObservatoryRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val resourceService: ResourceService
) : ObservatoryService {
    override fun create(name: String, description: String, organization: Organization, researchField: String, uriName: String): Observatory {
        val oId = UUID.randomUUID()
        val org = postgresOrganizationRepository
            .findById(organization.id!!.value)
            .orElseThrow { OrganizationNotFound(organization.id) } // FIXME: should always have an ID
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            this.name = name
            this.description = description
            this.researchField = researchField
            organizations = mutableSetOf(org)
            this.uriName = uriName
        }

        val response = postgresObservatoryRepository.save(newObservatory).toObservatory()
        return expand(response)
    }

    override fun listObservatories(): List<Observatory> =
        postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)
            .onEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
            }

    override fun findObservatoriesByOrganizationId(id: OrganizationId): List<Observatory> =
        postgresObservatoryRepository.findByorganizationsId(id.value)
            .map(ObservatoryEntity::toObservatory)
            .onEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
        }

    override fun findByName(name: String): Optional<Observatory> {
        val response = postgresObservatoryRepository
            .findByName(name)
            .map(ObservatoryEntity::toObservatory)!!
        return if (response.isPresent && hasResearchField(response.get()))
            Optional.of(response.get().withResearchField(response.get().researchField?.id!!))
        else response
    }

    override fun findById(id: ObservatoryId): Optional<Observatory> {
        val response = postgresObservatoryRepository.findById(id.value).map(ObservatoryEntity::toObservatory).get()
        return if (hasResearchField(response))
            Optional.of(response.withResearchField(response.researchField?.id!!))
        else Optional.of(response)
    }

    override fun findByUriName(id: String): Optional<Observatory> {
        val response = postgresObservatoryRepository.findByUriName(id).map(ObservatoryEntity::toObservatory).get()
        return if (hasResearchField(response))
            Optional.of(response.withResearchField(response.researchField?.id!!))
        else Optional.of(response)
    }

    override fun findObservatoriesByResearchField(researchField: String): List<Observatory> {
        val response = postgresObservatoryRepository.findByResearchField(researchField).map(ObservatoryEntity::toObservatory)

        response.forEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
        }
        return response
    }

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

    fun hasResearchField(response: Observatory): Boolean {
        return response.researchField?.id !== null
    }

    fun Observatory.withResearchField(resourceId: String) = this.apply {
        val resource = resourceService.findById(ResourceId(resourceId))
        researchField?.id = resource.get().id.toString()
        researchField?.label = resource.get().label
    }

    private fun expand(response: Observatory): Observatory =
        if (hasResearchField(response))
            response.withResearchField(response.researchField?.id!!)
        else response
}
