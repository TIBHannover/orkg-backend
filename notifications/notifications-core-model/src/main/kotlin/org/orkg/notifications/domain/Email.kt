package org.orkg.notifications.domain

data class Email(
    val subject: String,
    val htmlBody: String,
    val textBody: String,
)
