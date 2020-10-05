package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
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
    private val neo4jStatsService: Neo4jStatsService
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

        println(newObservatory.toObservatory())
        return postgresObservatoryRepository.save(newObservatory).toObservatory()
    }

    override fun listObservatories(): List<Observatory> {

        return postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)
    }

    override fun findObservatoriesByOrganizationId(id: UUID): List<Observatory> {
        return postgresObservatoryRepository.findByorganizationsId(id)
            .map(ObservatoryEntity::toObservatory)
    }

    override fun findByName(name: String): Optional<Observatory> =
        postgresObservatoryRepository
            .findByName(name)
            .map(ObservatoryEntity::toObservatory)

    override fun findById(id: UUID): Optional<Observatory> {
        return postgresObservatoryRepository.findById(id)
            .map(ObservatoryEntity::toObservatory)
    }

    override fun changeName(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            name = to
        }
        return postgresObservatoryRepository.save(entity).toObservatory()
    }

    override fun changeDescription(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            description = to
        }
        return postgresObservatoryRepository.save(entity).toObservatory()
    }

    override fun changeResearchField(id: UUID, to: String): Observatory {
        val entity = postgresObservatoryRepository.findById(id).get().apply {
            researchField = to
        }
        return postgresObservatoryRepository.save(entity).toObservatory()
    }
}
