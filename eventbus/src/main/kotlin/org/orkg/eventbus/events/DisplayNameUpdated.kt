package org.orkg.eventbus.events

import java.util.UUID
import org.orkg.eventbus.Event

data class DisplayNameUpdated(
    val id: UUID,
    val displayName: String,
) : Event
