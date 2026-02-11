package org.orkg.contenttypes.adapter.output.jpa.configuration

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories("org.orkg.contenttypes.adapter.output.jpa.internal", transactionManagerRef = "jpaTransactionManager")
@EntityScan("org.orkg.contenttypes.adapter.output.jpa.internal")
class ContentTypesJpaConfiguration
