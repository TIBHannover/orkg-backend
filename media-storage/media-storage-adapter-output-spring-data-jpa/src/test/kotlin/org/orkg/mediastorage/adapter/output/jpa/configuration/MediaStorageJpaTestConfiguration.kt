package org.orkg.mediastorage.adapter.output.jpa.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.orm.jpa.JpaTransactionManager

@TestConfiguration
@ComponentScan("org.orkg.mediastorage.adapter.output.jpa")
class MediaStorageJpaTestConfiguration {
    @Bean
    fun jpaTransactionManager(): JpaTransactionManager = JpaTransactionManager()
}
