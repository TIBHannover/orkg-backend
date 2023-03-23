package eu.tib.orkg.prototype.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ConfigurationProperties(prefix = "spring.rdf")
@EnableScheduling
class RdfConfiguration {
    final var vocabPrefix: String? = null
    final var frontendUri: String? = null
}
