package org.orkg.configuration

import org.neo4j.driver.Driver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.data.neo4j.autoconfigure.DataNeo4jAutoConfiguration
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jOperations
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager

// Configure custom transaction manager, because Spring Data Neo4j does not do that anymore if JPA is autoconfigured.
// See https://github.com/spring-projects/spring-data-neo4j/issues/2931 for more information.
// It will, however, happyly reuse the transaction manager provided by JPA's autoconfiguration, and will not be able
// to reuse a running transaction initiated by JPA, leading to hundreds of transactions to Neo4j for longer requests.
// The proper solution is to split the transaction management, and explicitly define the manager to be used.
@Configuration
class Neo4jConfiguration {
    // Inject a Neo4j template, because this is not done automatically when JPA is configured as well.
    @Bean
    @ConditionalOnMissingBean(Neo4jOperations::class)
    fun neo4jTemplate(
        neo4jClient: Neo4jClient,
        neo4jMappingContext: Neo4jMappingContext,
        neo4jTransactionManager: Neo4jTransactionManager,
    ): Neo4jTemplate = Neo4jTemplate(neo4jClient, neo4jMappingContext, neo4jTransactionManager)

    /**
     * A new transaction manager for Neo4j.
     *
     * The source was copied from [DataNeo4jAutoConfiguration.transactionManager] and converted to Kotlin.
     * The customizers are required, so custom converters, e.g., for IDs, can be hooked into it.
     */
    @Bean
    fun neo4jTransactionManager(
        driver: Driver,
        databaseSelectionProvider: DatabaseSelectionProvider,
        optionalCustomizers: ObjectProvider<TransactionManagerCustomizers>,
    ): Neo4jTransactionManager =
        Neo4jTransactionManager(driver, databaseSelectionProvider).also { transactionManager ->
            optionalCustomizers.ifAvailable { customizer -> customizer.customize(transactionManager) }
        }
}
