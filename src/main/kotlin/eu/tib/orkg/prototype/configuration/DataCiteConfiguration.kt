package eu.tib.orkg.prototype.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "orkg.datacite")
class DataCiteConfiguration {
    final var username: String? = null
    final var password: String? = null
    final var DOIPrefix: String? = null
}
