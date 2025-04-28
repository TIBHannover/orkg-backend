package org.orkg.notifications.domain

data class Recipient(
    val email: String,
    val name: String? = null,
)
