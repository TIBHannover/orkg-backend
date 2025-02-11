package org.orkg.configuration

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager

@Configuration
class JPAConfiguration {
    @Bean
    fun jpaTransactionManager(emf: EntityManagerFactory): JpaTransactionManager = JpaTransactionManager(emf)
}
