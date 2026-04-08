package org.orkg.testing.annotations

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import org.orkg.constants.BuildConfig
import org.orkg.testing.Neo4jContainerInitializer
import org.orkg.testing.PostgresContainerInitializer
import org.orkg.testing.configuration.Neo4jTransactionManagerConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import kotlin.reflect.KClass

@Neo4jContainerIntegrationTest
@PostgresContainerIntegrationTest
annotation class IntegrationTest

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
@Import(Neo4jTransactionManagerConfiguration::class)
@TestPropertySource(properties = ["org.neo4j.migrations.packages-to-scan=org.orkg.migrations.neo4j"])
annotation class Neo4jContainerUnitTest(
    @get:AliasFor(annotation = ContextConfiguration::class, attribute = "classes")
    val classes: Array<KClass<*>> = [],
)
