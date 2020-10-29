package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresObservatoryRepository
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresOrganizationRepository
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresObservatoryService(
    private val postgresObservatoryRepository: PostgresObservatoryRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val neo4jStatsService: Neo4jStatsService,
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

        var response = postgresObservatoryRepository.save(newObservatory).toObservatory()
        return if (response.researchField !== null)
            response.withResearchField(getResource(response.researchField.toString()))
        else response
    }

    override fun listObservatories(): List<Observatory> {

        var response = postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)

        response.forEach {
            if (it.researchField?.id !== null)
                it.withResearchField(getResource(it.researchField?.id!!))
        }
        return response
    }

    override fun findObservatoriesByOrganizationId(id: UUID): List<Observatory> {
        var response = postgresObservatoryRepository.findByorganizationsId(id)
            .map(ObservatoryEntity::toObservatory)

        response.forEach {
            if (it.researchField?.id !== null)
                it.withResearchField(getResource(it.researchField.toString()))
        }
        return response
    }

    override fun findByName(name: String): Optional<Observatory> {
        var response = postgresObservatoryRepository
            .findByName(name)
            .map(ObservatoryEntity::toObservatory).get()
        return if (response.researchField?.id !== null)
            Optional.of(response.withResearchField(getResource(response.researchField.toString())))
        else Optional.of(response)
    }

    override fun findById(id: UUID): Optional<Observatory> {
        var response = postgresObservatoryRepository.findById(id).map(ObservatoryEntity::toObservatory).get()
        return if (response.researchField?.id !== null)
            Optional.of(response.withResearchField(getResource(response.researchField.toString())))
        else Optional.of(response)
    }

    override fun changeName(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            name = to
        }
        var response = postgresObservatoryRepository.save(entity).toObservatory()
        return if (response.researchField?.id !== null)
            response.withResearchField(getResource(response.researchField.toString()))
        else response
    }

    override fun changeDescription(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            description = to
        }
        var response = postgresObservatoryRepository.save(entity).toObservatory()
        return if (response.researchField?.id !== null)
            response.withResearchField(getResource(response.researchField.toString()))
        else response
    }

    override fun changeResearchField(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            researchField = to
        }
        var response = postgresObservatoryRepository.save(entity).toObservatory()
        return if (response.researchField?.id !== null)
            response.withResearchField(getResource(response.researchField.toString()))
        else response
    }

    fun getResource(resource: String): Optional<Resource> {
            return resourceService.findById(ResourceId(resource))
    }
    fun Observatory.withResearchField(rf: Optional<Resource>) = this.apply {
        researchField?.id = rf.get().id.toString()
        researchField?.label = rf.get().label
    }
}
