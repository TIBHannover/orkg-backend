package eu.tib.orkg.prototype.community.adapter.output.jpa

import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.ObservatoryEntity
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.toObservatory
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringJpaPostgresObservatoryAdapter(
    private val postgresRepository: PostgresObservatoryRepository,
    private val postgresOrganizationRepository: PostgresOrganizationRepository,
    private val userRepository: JpaUserRepository
) : ObservatoryRepository {
    override fun save(observatory: Observatory) {
        postgresRepository.save(
            postgresRepository.toObservatoryEntity(observatory, postgresOrganizationRepository, userRepository)
        )
    }

    override fun findById(id: ObservatoryId): Optional<Observatory> =
        postgresRepository.findById(id.value).map(ObservatoryEntity::toObservatory)

    override fun findByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory> =
        postgresRepository.findByOrganizationsId(id.value, pageable).map(ObservatoryEntity::toObservatory)

    override fun findByName(name: String): Optional<Observatory> =
        postgresRepository.findByName(name).map(ObservatoryEntity::toObservatory)

    override fun findByDisplayId(displayId: String): Optional<Observatory> =
        postgresRepository.findByDisplayId(displayId).map(ObservatoryEntity::toObservatory)

    override fun findByResearchField(researchField: ThingId, pageable: Pageable): Page<Observatory> =
        postgresRepository.findByResearchField(researchField.value, pageable).map(ObservatoryEntity::toObservatory)

    override fun findAll(pageable: Pageable): Page<Observatory> =
        postgresRepository.findAll(pageable).map(ObservatoryEntity::toObservatory)

    override fun deleteAll() = postgresRepository.deleteAll()
}
