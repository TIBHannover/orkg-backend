package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresObservatoryRepository
import eu.tib.orkg.prototype.statements.infrastructure.neo4j.Neo4jStatsService
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresObservatoryService(
    private val postgresObservatoryRepository: PostgresObservatoryRepository,
    private val neo4jStatsService: Neo4jStatsService
) : ObservatoryService {
    override fun create(name: String, description: String, organization: OrganizationEntity, researchField: String): ObservatoryEntity {
        val oId = UUID.randomUUID()
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            this.name = name
            this.description = description
            this.researchField = researchField
            organizations = mutableSetOf(
                organization
            )
        }

        println(newObservatory.toObservatory())
        return postgresObservatoryRepository.save(newObservatory)
    }

    override fun listObservatories(): List<Observatory> {
        var observatoriesList = postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)

        observatoriesList.forEach {
            it.numPapers = neo4jStatsService.getObservatoryPapersCount(it.id!!)
            it.numComparisons = neo4jStatsService.getObservatoryComparisonsCount(it.id!!)
        }

            return observatoriesList
    }

    override fun findObservatoriesByOrganizationId(id: UUID): List<Observatory> {
        return postgresObservatoryRepository.findByorganizationsId(id)
            .map(ObservatoryEntity::toObservatory)
    }

    override fun findByName(name: String): Optional<ObservatoryEntity> {
        return postgresObservatoryRepository.findByName(name)
    }

    override fun findById(id: UUID): Optional<Observatory> {
        var observatory = postgresObservatoryRepository.findById(id)
            .map(ObservatoryEntity::toObservatory)
        observatory.get().numPapers = neo4jStatsService.getObservatoryPapersCount(observatory.get().id!!)
        observatory.get().numComparisons = neo4jStatsService.getObservatoryComparisonsCount(observatory.get().id!!)
        return observatory
    }

    override fun updateObservatory(observatory: Observatory): Observatory {
        val observatoryEntity = ObservatoryEntity().apply {
            id = observatory.id
            name = observatory.name
            description = observatory.description
            researchField = observatory.researchField
            organizations = observatory.organizations
        }
        return postgresObservatoryRepository.save(observatoryEntity).toObservatory()
    }
}
