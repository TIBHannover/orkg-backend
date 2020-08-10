package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.application.OrganizationNotFound
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.Organization
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
    private val postgresOrganizationRepository: PostgresOrganizationRepository
) : ObservatoryService {
    override fun create(name: String, description: String, organization: Organization): Observatory {
        val oId = UUID.randomUUID()
        val org = postgresOrganizationRepository.findById(organization.id!!).orElseThrow { OrganizationNotFound() } // FIXME: should always have an ID
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            this.name = name
            this.description = description
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
}
