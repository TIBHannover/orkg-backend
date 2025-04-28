package org.orkg.notifications.adapter.output.mail.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "orkg.mail")
class EmailServiceConfiguration {
    lateinit var from: EmailConfig
    lateinit var replyTo: EmailConfig

    class EmailConfig {
        lateinit var email: String
        lateinit var displayName: String
    }
}
