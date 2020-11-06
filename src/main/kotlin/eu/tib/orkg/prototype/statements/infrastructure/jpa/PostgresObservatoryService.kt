package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
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
    override fun create(name: String, description: String, organization: Organization, researchField: String): Observatory {
        val oId = UUID.randomUUID()
        val org = postgresOrganizationRepository
            .findById(organization.id!!)
            .orElseThrow { OrganizationNotFound(organization.id) } // FIXME: should always have an ID
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            this.name = name
            this.description = description
            this.researchField = researchField
            organizations = mutableSetOf(org)
        }

        val response = postgresObservatoryRepository.save(newObservatory).toObservatory()
        return if (isIdPresent(response))
            response.withResearchField(response.researchField?.id!!)
        else response
    }

    override fun listObservatories(): List<Observatory> =
        postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)
            .onEach {
            if (isIdPresent(it))
                it.withResearchField(it.researchField?.id!!)
            }

    override fun findObservatoriesByOrganizationId(id: UUID): List<Observatory> =
        postgresObservatoryRepository.findByorganizationsId(id)
            .map(ObservatoryEntity::toObservatory)
            .onEach {
            if (isIdPresent(it))
                it.withResearchField(it.researchField?.id!!)
        }

    override fun findByName(name: String): Optional<Observatory> {
        val response = postgresObservatoryRepository
            .findByName(name)
            .map(ObservatoryEntity::toObservatory).get()
        return if (isIdPresent(response))
            Optional.of(response.withResearchField(response.researchField?.id!!))
        else Optional.of(response)
    }

    override fun findById(id: UUID): Optional<Observatory> {
        val response = postgresObservatoryRepository.findById(id).map(ObservatoryEntity::toObservatory).get()
        return if (isIdPresent(response))
            Optional.of(response.withResearchField(response.researchField?.id!!))
        else Optional.of(response)
    }

    override fun findObservatoriesByResearchField(researchField: String): List<Observatory> {
        val response = postgresObservatoryRepository.findByResearchField(researchField).map(ObservatoryEntity::toObservatory)

        response.forEach {
            if (isIdPresent(it))
                it.withResearchField(it.researchField?.id!!)
        }
        return response
    }

    override fun changeName(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            name = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return if (isIdPresent(response))
            response.withResearchField(response.researchField?.id!!)
        else response
    }

    override fun changeDescription(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            description = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return if (isIdPresent(response))
            response.withResearchField(response.researchField?.id!!)
        else response
    }

    override fun changeResearchField(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            researchField = to
        }
        val response = postgresObservatoryRepository.save(entity).toObservatory()
        return if (isIdPresent(response))
            response.withResearchField(response.researchField?.id!!)
        else response
    }

    fun isIdPresent(response: Observatory): Boolean {
        return response.researchField?.id !== null
    }

    fun Observatory.withResearchField(resourceId: String) = this.apply {
        val resource = resourceService.findById(ResourceId(resourceId))
        researchField?.id = resource.get().id.toString()
        researchField?.label = resource.get().label
    }
}
