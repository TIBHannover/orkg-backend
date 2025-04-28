package org.orkg.notifications.domain.internal

import io.kotest.matchers.maps.shouldContainAll
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest

internal class EmailTemplateLoaderUnitTest : MockkBaseTest {
    private val emailTemplateLoader = EmailTemplateLoader()

    @Test
    fun `Given an email template loader, it loads messages correctly`() {
        emailTemplateLoader.messages shouldContainAll mapOf(
            "email-test-subject" to "Test email",
            "email-test-body" to "This is a test email.\n{0}",
            "email-test-body-html" to "<p>This is a test email.<br/>{0}</p>",
        )
    }
}
