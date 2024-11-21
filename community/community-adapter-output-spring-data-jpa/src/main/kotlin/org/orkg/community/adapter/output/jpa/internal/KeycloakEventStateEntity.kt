package org.orkg.community.adapter.output.jpa.internal

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table
import org.orkg.community.domain.EventType

@Entity
@Table(name = "keycloak_event_states")
class KeycloakEventStateEntity(
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    var eventType: EventType? = null,

    @Column(name = "counter")
    var counter: Int = 0
)
