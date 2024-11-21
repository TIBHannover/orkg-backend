package org.orkg.community.adapter.output.jpa.internal
import org.orkg.community.domain.EventType
import org.springframework.data.jpa.repository.JpaRepository

interface PostgresKeycloakEventStateRepository : JpaRepository<KeycloakEventStateEntity, EventType>
