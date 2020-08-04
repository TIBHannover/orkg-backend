package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.OrganizationEntity
import eu.tib.orkg.prototype.statements.domain.model.jpa.PostgresObservatoryRepository
import java.util.Optional
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PostgresObservatoryService(
    private val postgresObservatoryRepository: PostgresObservatoryRepository
) : ObservatoryService {
    override fun create(name: String, description: String, organization: OrganizationEntity): ObservatoryEntity {
        val oId = UUID.randomUUID()
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            this.name = name
            this.description = description
            organizations = mutableSetOf(
                organization
            )
        }

        println(newObservatory.toObservatory())
        return postgresObservatoryRepository.save(newObservatory)
    }

    override fun listObservatories(): List<Observatory> {
        return postgresObservatoryRepository.findAll()
            .map(ObservatoryEntity::toObservatory)
    }

    override fun findObservatoriesByOrganizationId(id: UUID): List<Observatory> {
        return postgresObservatoryRepository.findByorganizationsId(id)
            .map(ObservatoryEntity::toObservatory)
    }

    override fun findByName(name: String): Optional<ObservatoryEntity> {
        return postgresObservatoryRepository.findByName(name)
    }

    override fun findById(id: UUID): Optional<Observatory> {
        return postgresObservatoryRepository.findById(id)
            .map(ObservatoryEntity::toObservatory)
    }

    override fun updateObservatory(observatory: Observatory): Observatory {
        val observatoryEntity = ObservatoryEntity().apply {
            id = observatory.id
            name = observatory.name
            description = observatory.description
        }
        return postgresObservatoryRepository.save(observatoryEntity).toObservatory()
    }
}
