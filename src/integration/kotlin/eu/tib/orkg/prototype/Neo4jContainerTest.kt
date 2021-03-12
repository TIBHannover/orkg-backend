package eu.tib.orkg.prototype

import java.time.Duration
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for running a test with Neo4j using TestContainers.
 *
 * This class does not add any Spring test annotations. It leaves setting up the
 * context up to the individual test classes.
 */
@Testcontainers
@TestInstance(PER_METHOD)
abstract class Neo4jContainerTest {
    companion object {
        @Container
        @JvmStatic
        private val neo4jContainer = Neo4jContainer<Nothing>("neo4j:3.5").apply {
            withAdminPassword(disabled)
            withStartupTimeout(Duration.ofMinutes(1))
        }

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        private fun configureProperties(registry: DynamicPropertyRegistry) {
            with(registry) {
                add("spring.data.neo4j.uri", neo4jContainer::getBoltUrl)
                add("spring.data.neo4j.username") { "neo4j" }
                add("spring.data.neo4j.password", neo4jContainer::getAdminPassword)
                add("spring.data.neo4j.use-native-types") { true }
            }
        }

        // Solely for improving readability…
        private const val disabled: String = ""
    }
}
