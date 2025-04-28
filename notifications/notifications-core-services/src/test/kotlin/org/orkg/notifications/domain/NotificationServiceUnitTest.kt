package org.orkg.notifications.domain

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.Assets.emailHtml
import org.orkg.common.testing.fixtures.Assets.emailText
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.notifications.domain.internal.EmailTemplateLoader
import org.orkg.notifications.output.EmailService
import org.orkg.notifications.testing.fixtures.createRecipient

internal class NotificationServiceUnitTest : MockkBaseTest {
    private val emailService: EmailService = mockk()
    private val emailTemplateLoader: EmailTemplateLoader = EmailTemplateLoader()

    private val service = NotificationService(emailService, emailTemplateLoader)

    @Test
    fun `Given a recipient and a custom message, when sending a test email, it returns success`() {
        val recipient = createRecipient()
        val customMessage = "Hello from notification service unit test!"
        val expected = Email(
            subject = "Test email",
            htmlBody = emailHtml("testEmail"),
            textBody = emailText("testEmail")
        )

        every { emailService.send(recipient, any()) } just runs

        service.sendTestEmail(recipient, customMessage)

        verify(exactly = 1) { emailService.send(recipient, expected) }
    }
}
