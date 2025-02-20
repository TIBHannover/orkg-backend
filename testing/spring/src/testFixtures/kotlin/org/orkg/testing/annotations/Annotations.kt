package org.orkg.testing.annotations

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import org.neo4j.driver.Driver
import org.orkg.testing.MockUserId
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@SpringBootTest
@ContextConfiguration(initializers = [Neo4jContainerInitializer::class])
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
@TestPropertySource(properties = ["org.neo4j.migrations.packages-to-scan=org.orkg.migrations.neo4j"])
annotation class Neo4jContainerIntegrationTest

@DataNeo4jTest
@EnableAutoConfiguration
@ContextConfiguration(initializers = [Neo4jContainerInitializer::class])
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
@Import(Neo4jTestConfiguration::class)
@TestPropertySource(properties = ["org.neo4j.migrations.packages-to-scan=org.orkg.migrations.neo4j"])
annotation class Neo4jContainerUnitTest(
    @get:AliasFor(annotation = ContextConfiguration::class, attribute = "classes")
    val classes: Array<KClass<*>> = [],
)

@TestConfiguration
class Neo4jTestConfiguration {
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

/**
 * Annotation that helps to set up tests with PostgreSQL in TestContainers.
 *
 * Tests are transactional by default. The test database is replaced with a TestContainers Docker container that is
 * configured via `application.yaml`.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
annotation class TestContainersJpaTest

@Retention(RUNTIME)
@WithMockAuthentication(authorities = [], name = MockUserId.USER)
annotation class TestWithMockUser

@Retention(RUNTIME)
@WithMockAuthentication(authorities = ["ROLE_CURATOR"], name = MockUserId.CURATOR)
annotation class TestWithMockCurator

@Retention(RUNTIME)
@WithMockAuthentication(authorities = ["ROLE_ADMIN"], name = MockUserId.ADMIN)
annotation class TestWithMockAdmin
