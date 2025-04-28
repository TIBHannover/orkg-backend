package org.orkg.notifications.testing.fixtures

import org.orkg.notifications.domain.Email
import org.orkg.notifications.domain.Recipient

fun createEmail(): Email = Email(
    subject = "Test subject",
    textBody = "Test message",
    htmlBody = "<p>Test message</p>"
)

fun createRecipient(): Recipient = Recipient(
    email = "test@mail.com",
    name = "Example User"
)
