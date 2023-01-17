package eu.tib.orkg.prototype.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(value = [
    "eu.tib.orkg.prototype.statements.domain.model.jpa",
    "eu.tib.orkg.prototype.community.adapter.output.jpa.internal"
])
@EntityScan(value = [
    "eu.tib.orkg.prototype.statements.domain.model.jpa",
    "eu.tib.orkg.prototype.community.adapter.output.jpa.internal"
])
class JpaConfiguration
