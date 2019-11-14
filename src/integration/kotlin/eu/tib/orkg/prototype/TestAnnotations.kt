package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Annotation that helps to setup tests of Neo4j repositories.
 */
@Target(CLASS)
@DataNeo4jTest
@Import(Neo4jConfiguration::class)
@Transactional
annotation class Neo4jRepositoryTest

/**
 * Annotation that helps to setup tests with Neo4j service implementations.
 *
 * The @[DataNeo4jTest] annotation excludes all [org.springframework.stereotype.Component]s by default.
 * Filters are disabled, so that they are not excluded from the component search.
 * Since services reside in a package that is not leaded by default due to the excluded `ApplicationContext`,
 * component scanning is extended to include the base packages containing the Neo4j service implementations.
 */
@Target(CLASS)
@DataNeo4jTest(useDefaultFilters = false)
@Import(Neo4jConfiguration::class)
@ComponentScan(
    basePackages = [
        "eu.tib.orkg.prototype.statements.domain.model.neo4j",
        "eu.tib.orkg.prototype.statements.infrastructure.neo4j"
    ]
)
@Transactional
annotation class Neo4jServiceTest

/**
 * Annotation that helps to setup tests with PostgreSQL in TestContainers.
 *
 * Tests are transactional by default. The test database is replaced with a TestContainers Docker container that is
 * configured via `application.yaml`.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
annotation class TestContainersJpaTest
