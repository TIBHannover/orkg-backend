package org.orkg.contenttypes.adapter.output.jpa.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.orm.jpa.JpaTransactionManager

@TestConfiguration
@ComponentScan("org.orkg.contenttypes.adapter.output.jpa")
class ContentTypesJpaTestConfiguration {
    @Bean
    fun jpaTransactionManager(): JpaTransactionManager = JpaTransactionManager()
}
