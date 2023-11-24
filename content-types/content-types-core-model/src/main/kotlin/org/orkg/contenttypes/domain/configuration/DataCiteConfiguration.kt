package org.orkg.contenttypes.domain.configuration

import java.util.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "orkg.datacite")
class DataCiteConfiguration {
    final var username: String? = null
    final var password: String? = null
    final var doiPrefix: String? = null
    final var url: String? = null
    final var publish: String? = null

    val encodedCredentials: String get() =
        Base64.getEncoder().encodeToString("$username:$password".toByteArray())
}
