package eu.tib.orkg.prototype.events.listeners

data class NotificationData(
    val type: String,
    val data: String
)

data class NotificationUpdateData(
    var resourceId: String,
    var newResource: Boolean
)
