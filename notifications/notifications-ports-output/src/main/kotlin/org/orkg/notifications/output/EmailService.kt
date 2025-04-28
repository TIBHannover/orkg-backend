package org.orkg.notifications.output

import org.orkg.notifications.domain.Email
import org.orkg.notifications.domain.Recipient

interface EmailService {
    fun send(recipient: Recipient, email: Email)
}
