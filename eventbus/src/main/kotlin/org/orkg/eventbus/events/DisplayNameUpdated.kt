package org.orkg.eventbus.events

import org.orkg.eventbus.Event
import java.util.UUID

data class DisplayNameUpdated(
    val id: UUID,
    val displayName: String,
) : Event
