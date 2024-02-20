package org.orkg.community.adapter.output.jpa

import java.util.*
import org.orkg.auth.adapter.output.jpa.internal.JpaUserRepository
import org.orkg.auth.adapter.output.jpa.internal.UserEntity
import org.orkg.auth.domain.User
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.adapter.output.jpa.internal.ObservatoryEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresObservatoryRepository
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.adapter.output.jpa.internal.toContributor
import org.orkg.community.adapter.output.jpa.internal.toObservatory
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Observatory
import org.orkg.community.output.ObservatoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
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

    override fun findAllByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Observatory> =
        postgresRepository.findAllByOrganizationsId(id.value, pageable).map(ObservatoryEntity::toObservatory)

    override fun findByName(name: String): Optional<Observatory> =
        postgresRepository.findByName(name).map(ObservatoryEntity::toObservatory)

    override fun findAllByNameContains(name: String, pageable: Pageable): Page<Observatory> =
        postgresRepository.findAllByNameContainsIgnoreCase(name, pageable).map(ObservatoryEntity::toObservatory)

    override fun findByDisplayId(displayId: String): Optional<Observatory> =
        postgresRepository.findByDisplayId(displayId).map(ObservatoryEntity::toObservatory)

    override fun findAllByResearchField(researchField: ThingId, pageable: Pageable): Page<Observatory> =
        postgresRepository.findAllByResearchField(researchField.value, pageable).map(ObservatoryEntity::toObservatory)

    override fun findAll(pageable: Pageable): Page<Observatory> =
        postgresRepository.findAll(pageable).map(ObservatoryEntity::toObservatory)

    override fun findAllResearchFields(pageable: Pageable): Page<ThingId> =
        postgresRepository.findAllResearchFields(pageable).map { ThingId(it) }

    override fun deleteAll() = postgresRepository.deleteAll()

    // TODO: refactor
    override fun allMembers(id: ObservatoryId, pageable: Pageable): Page<Contributor> =
        userRepository.findAllByObservatoryId(id.value, pageable).map(UserEntity::toUser).map(User::toContributor)

    override fun count(): Long = postgresRepository.count()
}
