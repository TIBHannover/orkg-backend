package org.orkg.notifications.input

import org.orkg.notifications.domain.Recipient

interface NotificationUseCases : SendEmailUseCase

interface SendEmailUseCase {
    fun sendTestEmail(recipient: Recipient, message: String)
}
