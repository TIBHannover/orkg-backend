package eu.tib.orkg.prototype.statements.infrastructure.jpa
import eu.tib.orkg.prototype.statements.domain.model.Observatory
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryService
import eu.tib.orkg.prototype.statements.domain.model.jpa.ObservatoryEntity
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
    override fun create(observatoryName: String, organizationID: UUID): ObservatoryEntity {
        val oId = UUID.randomUUID()
        val newObservatory = ObservatoryEntity().apply {
            id = oId
            name = observatoryName
            organizationId = organizationID
        }
        return postgresObservatoryRepository.save(newObservatory)
    }

    override fun listObservatories(): List<ObservatoryEntity> {
        return postgresObservatoryRepository.findAll()
    }

    override fun findObservatoriesByOrganizationId(id: UUID): List<ObservatoryEntity> {
        return postgresObservatoryRepository.findByorganizationId(id)
    }

    override fun findByName(name: String): Optional<ObservatoryEntity> {
        return postgresObservatoryRepository.findByName(name)
    }

    override fun findById(id: UUID): Optional<Observatory> {
        return postgresObservatoryRepository.findById(id)
            .map(ObservatoryEntity::toObservatory)
    }

    override fun findByUserId(id: UUID): Optional<ObservatoryEntity> {
        return postgresObservatoryRepository.findByUsersId(id)
    }
}
