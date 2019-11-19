package eu.tib.orkg.prototype.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.rdf")
class RdfConfiguration {
    final var vocabPrefix: String? = null
    final var frontendUri: String? = null
}
