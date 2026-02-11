package org.orkg.testing.annotations

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import org.neo4j.driver.Driver
import org.orkg.constants.BuildConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.Neo4jContainerInitializer
import org.orkg.testing.PostgresContainerInitializer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizers
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

@SpringBootTest
@ContextConfiguration(initializers = [PostgresContainerInitializer::class])
@TestPropertySource(properties = ["spring.datasource.url=jdbc:tc:postgresql:${BuildConfig.CONTAINER_IMAGE_POSTGRES_TAG}://localhost/db"])
annotation class PostgresContainerIntegrationTest

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

@Retention(RUNTIME)
@WithMockAuthentication(
    authorities = [],
    userId = MockUserId.USER,
    username = "Test User",
    email = "user@example.org"
)
annotation class TestWithMockUser

@Retention(RUNTIME)
@WithMockAuthentication(
    authorities = ["ROLE_CURATOR"],
    userId = MockUserId.CURATOR,
    username = "Test Curator",
    email = "curator@example.org"
)
annotation class TestWithMockCurator

@Retention(RUNTIME)
@WithMockAuthentication(
    authorities = ["ROLE_ADMIN"],
    userId = MockUserId.ADMIN,
    username = "Test Admin",
    email = "admin@example.org"
)
annotation class TestWithMockAdmin
