package eu.tib.orkg.prototype.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories("eu.tib.orkg.prototype.statements.domain.model.jpa")
@EntityScan("eu.tib.orkg.prototype.statements.domain.model.jpa")
class JpaConfiguration
