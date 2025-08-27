package org.orkg.dataimport.adapter.output.jpa.configuration

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories("org.orkg.dataimport.adapter.output.jpa.internal", transactionManagerRef = "jpaTransactionManager")
@EntityScan("org.orkg.dataimport.adapter.output.jpa.internal")
class DataImportJpaConfiguration
