package org.orkg.community.output

import org.orkg.community.domain.EventType

interface KeycloakEventStateRepository {
    fun findById(id: EventType): Int

    fun save(id: EventType, counter: Int)
}
