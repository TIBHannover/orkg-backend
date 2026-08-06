package org.orkg.api.common.configuration

import org.axonframework.common.jdbc.ConnectionProvider
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.extension.spring.messaging.unitofwork.SpringTransactionManager
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager

@EntityScan(
    basePackages = [
        "org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa",
        "org.axonframework.eventsourcing.eventstore.jpa",
    ],
)
@Configuration
class AxonConfiguration {
    @Bean
    fun axonTransactionManager(
        jpaTransactionManager: JpaTransactionManager,
        entityManagerProvider: EntityManagerProvider,
        connectionProvider: ConnectionProvider,
    ): SpringTransactionManager =
        SpringTransactionManager(jpaTransactionManager, entityManagerProvider, connectionProvider)
}
