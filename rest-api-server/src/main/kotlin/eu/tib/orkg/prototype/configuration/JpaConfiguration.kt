package eu.tib.orkg.prototype.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    "eu.tib.orkg.prototype.files.adapter.output.jpa.internal",
    "eu.tib.orkg.prototype.community.adapter.output.jpa.internal",
    "eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal",
    "eu.tib.orkg.prototype.discussions.adapter.output.jpa.internal",
)
@EntityScan(
    "eu.tib.orkg.prototype.files.adapter.output.jpa.internal",
    "eu.tib.orkg.prototype.community.adapter.output.jpa.internal",
    "eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal",
    "eu.tib.orkg.prototype.discussions.adapter.output.jpa.internal",
)
class JpaConfiguration
