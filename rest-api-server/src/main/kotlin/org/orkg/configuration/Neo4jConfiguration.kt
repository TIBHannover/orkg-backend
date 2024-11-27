package org.orkg.configuration

import org.neo4j.driver.Driver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jOperations
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager

// Configure custom transaction manager, because Spring Data Neo4j does not do that anymore if JPA is auto-configured.
// See https://github.com/spring-projects/spring-data-neo4j/issues/2931.
@Configuration
class Neo4jConfiguration {
    @Bean("neo4jTemplate")
    @ConditionalOnMissingBean(Neo4jOperations::class)
    fun neo4jTemplate(
        neo4jClient: Neo4jClient,
        neo4jMappingContext: Neo4jMappingContext,
        driver: Driver,
        databaseNameProvider: DatabaseSelectionProvider,
        optionalCustomizers: ObjectProvider<TransactionManagerCustomizers>
    ): Neo4jTemplate {
        val transactionManager = Neo4jTransactionManager(driver, databaseNameProvider)
        optionalCustomizers.ifAvailable { customizer: TransactionManagerCustomizers ->
            customizer.customize(transactionManager)
        }
        return Neo4jTemplate(neo4jClient, neo4jMappingContext, transactionManager)
    }
}
