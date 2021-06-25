package eu.tib.orkg.prototype.events.listeners

import java.util.UUID

data class NotificationData(
    val type: String,
    val data: String
)

data class NotificationUpdateData(
    var resourceId: String,
    var newResource: Boolean,
    var userId: UUID
)
