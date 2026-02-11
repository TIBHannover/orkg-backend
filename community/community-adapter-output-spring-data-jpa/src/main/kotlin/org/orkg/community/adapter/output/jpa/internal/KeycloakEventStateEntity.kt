package org.orkg.community.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.community.domain.EventType

@Entity
@Table(name = "keycloak_event_states")
class KeycloakEventStateEntity {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: EventType? = null

    @Column(name = "counter", nullable = false)
    var counter: Int = 0
}
