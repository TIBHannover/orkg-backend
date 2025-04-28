package org.orkg.notifications.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.notifications.adapter.input.rest.EmailController.SendTestEmailRequest
import org.orkg.notifications.domain.Recipient
import org.orkg.notifications.input.NotificationUseCases
import org.orkg.testing.annotations.TestWithMockAdmin
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        EmailController::class,
        ExceptionHandler::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [EmailController::class])
internal class EmailControllerUnitTest : MockMvcBaseTest("emails") {
    @MockkBean
    private lateinit var notificationUseCases: NotificationUseCases

    @Test
    @TestWithMockAdmin
    @DisplayName("Given a test email request, when service succeeds, then status is 204 NO CONTENT")
    fun sendTestEmail() {
        val recipient = Recipient("admin@example.org", "Test Admin")
        val request = SendTestEmailRequest(message = "Hello there!")

        every { notificationUseCases.sendTestEmail(any(), any()) } just runs

        documentedPostRequestTo("/api/emails/test")
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { notificationUseCases.sendTestEmail(recipient, request.message) }
    }
}
