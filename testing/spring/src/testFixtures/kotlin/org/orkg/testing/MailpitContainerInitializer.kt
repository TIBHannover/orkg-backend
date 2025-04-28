package org.orkg.testing

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

private const val SMTP_PORT = 1025
private const val HTTP_PORT = 8025

/**
 * Initializer to start a Mailpit instance using TestContainers.
 */
class MailpitContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    // TODO: might be nice to aggregate values for debugging, if possible

    companion object {
        val mailpitContainer = GenericContainer("axllent/mailpit:v1.21")
            .withExposedPorts(SMTP_PORT, HTTP_PORT)
            .waitingFor(Wait.forHttp("/").forPort(HTTP_PORT))

        val GenericContainer<*>.httpPort: Int
            get() = mailpitContainer.getMappedPort(HTTP_PORT)

        val GenericContainer<*>.smtpPort: Int
            get() = mailpitContainer.getMappedPort(SMTP_PORT)
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        mailpitContainer.start()
        TestPropertyValues.of(mailpitContainer.settings()).applyTo(applicationContext)
    }

    private fun GenericContainer<*>.settings() = listOf(
        "spring.mail.host=${mailpitContainer.host}",
        "spring.mail.port=${mailpitContainer.smtpPort}",
    )
}
