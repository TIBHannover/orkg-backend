package org.orkg.testing.configuration

import org.neo4j.driver.Driver
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager

@TestConfiguration
class Neo4jTransactionManagerConfiguration {
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
