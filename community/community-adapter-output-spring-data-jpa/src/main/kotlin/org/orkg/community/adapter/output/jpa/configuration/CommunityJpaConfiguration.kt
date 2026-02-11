package org.orkg.community.adapter.output.jpa.configuration

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories("org.orkg.community.adapter.output.jpa.internal", transactionManagerRef = "jpaTransactionManager")
@EntityScan("org.orkg.community.adapter.output.jpa.internal")
class CommunityJpaConfiguration
