package eu.tib.orkg.prototype.statements.infrastructure.neo4j
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryServiceNeo
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jObservatory
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jObservatoryRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jOrganizationRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jObservatoryService(
    private val postgresObservatoryRepository: Neo4jObservatoryRepository,
    private val postgresOrganizationRepository: Neo4jOrganizationRepository,
    private val resourceService: ResourceService
) : ObservatoryServiceNeo {
    override fun create(name: String, description: String, organization: OrganizationId, researchField: String): Observatory {
        val oId = UUID.randomUUID()
        val org = postgresOrganizationRepository
            .findById(organization.value)
            .orElseThrow { OrganizationNotFound(organization) } // FIXME: should always have an ID

            val newObservatory = Neo4jObservatory().apply {
                this.observatoryId = ObservatoryId(oId)
                this.name = name
                this.description = description
                this.researchField = researchField
                organizations = mutableSetOf(org)
            }
        val response = postgresObservatoryRepository.save(newObservatory).toObservatory()
        return expand(response)
    }

    override fun create(id: UUID, name: String, description: String, researchField: String): Observatory {
        val response = postgresObservatoryRepository.save(Neo4jObservatory(observatoryId = ObservatoryId(id), name = name, description = description, researchField = researchField)).toObservatory()
        return expand(response)
    }

    override fun createRelationInObservatoryOrganization(
        organization: UUID,
        observatory: UUID
    ) {
        postgresObservatoryRepository.createRelationInObservatoryOrganization(organization, observatory)
    }

    override fun listObservatories(): List<Observatory> =
        postgresObservatoryRepository.findAll()
            .map(Neo4jObservatory::toObservatory)
            .onEach {
            if (hasResearchField(it))
                it.withResearchField(it.researchField?.id!!)
            }

    override fun findByName(name: String): Optional<Observatory> {
        val response = postgresObservatoryRepository
            .findByName(name)
            .map(Neo4jObservatory::toObservatory)!!
        return if (response.isPresent && hasResearchField(response.get()))
            Optional.of(response.get().withResearchField(response.get().researchField?.id!!))
        else response
    }

    override fun findById(id: ObservatoryId): Optional<Observatory> {
        val response = postgresObservatoryRepository.findById(id.value).map(Neo4jObservatory::toObservatory).get()
        return if (hasResearchField(response))
            Optional.of(response.withResearchField(response.researchField?.id!!))
        else Optional.of(response)
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
