package org.orkg.community.adapter.output.jpa

import org.orkg.community.adapter.output.jpa.internal.KeycloakEventStateEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresKeycloakEventStateRepository
import org.orkg.community.domain.EventType
import org.orkg.community.output.KeycloakEventStateRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class SpringJpaPostgresKeycloakEventStateAdapter(
    private val postgresRepository: PostgresKeycloakEventStateRepository,
) : KeycloakEventStateRepository {
    override fun findById(id: EventType): Int =
        postgresRepository.findById(id).get().counter

    override fun save(id: EventType, counter: Int) {
        postgresRepository.save(KeycloakEventStateEntity(id, counter))
    }
}
