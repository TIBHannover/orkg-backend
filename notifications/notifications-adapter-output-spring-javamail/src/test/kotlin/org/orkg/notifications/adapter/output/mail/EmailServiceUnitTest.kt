package org.orkg.notifications.adapter.output.mail

import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.notifications.adapter.output.mail.configuration.EmailServiceConfiguration
import org.orkg.notifications.output.EmailService
import org.orkg.notifications.testing.fixtures.createEmail
import org.orkg.notifications.testing.fixtures.createRecipient
import org.orkg.testing.MailpitContainerInitializer
import org.orkg.testing.MailpitContainerInitializer.Companion.httpPort
import org.orkg.testing.MailpitContainerInitializer.Companion.mailpitContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        MailSenderAutoConfiguration::class,
        EmailServiceAdapter::class,
        EmailServiceConfiguration::class
    ],
    initializers = [MailpitContainerInitializer::class]
)
@TestPropertySource(
    properties = [
        "orkg.mail.from.email=test@orkg.org",
        "orkg.mail.from.display-name=ORKG - Open Research Knowledge Graph",
        "orkg.mail.reply-to.email=reply@orkg.org",
        "orkg.mail.reply-to.display-name=ORKG Support Team",
    ]
)
internal class EmailServiceUnitTest {
    @Autowired
    private lateinit var emailService: EmailService

    @Test
    fun testSendEmail() {
        val email = createEmail()
        val recipient = createRecipient()
        val mailpitApiUrl = "http://${mailpitContainer.host}:${mailpitContainer.httpPort}/api"

        emailService.send(recipient, email)

        val messageId = given()
            .log().ifValidationFails()
            .`when`().get("$mailpitApiUrl/v1/messages")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .path<String>("messages[0].ID")

        given()
            .log().ifValidationFails()
            .`when`().get("$mailpitApiUrl/v1/message/$messageId")
            .then()
            .assertThat().statusCode(200)
            .and()
            .body("From.Name", equalTo("ORKG - Open Research Knowledge Graph"))
            .body("From.Address", equalTo("test@orkg.org"))
            .body("To[0].Name", equalTo("Example User"))
            .body("To[0].Address", equalTo("test@mail.com"))
            .body("ReplyTo[0].Name", equalTo("ORKG Support Team"))
            .body("ReplyTo[0].Address", equalTo("reply@orkg.org"))
            .body("Subject", equalTo("Test subject"))
            .body("Text", equalTo("Test message"))
            .body("HTML", equalTo("<p>Test message</p>"))
    }
}
