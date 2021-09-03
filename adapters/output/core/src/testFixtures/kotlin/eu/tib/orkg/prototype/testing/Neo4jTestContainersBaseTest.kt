package eu.tib.orkg.prototype.testing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@TestConstructor(autowireMode = ALL)
abstract class Neo4jTestContainersBaseTest(
    private val client: Neo4jClient
) {
    companion object {
        // It is important to not use @Containers here, so we can manage the life-cycle.
        // We instantiate only one container per test class.
        @JvmStatic
        protected val container = Neo4jContainer<Nothing>(
            DockerImageName.parse("neo4j:4.1-community")
        ).apply {
            withoutAuthentication()
        }

        // Start the container once per class. This needs to be done via a static method.
        // If @TestInstance(PER_CLASS) is used, Spring fails to set up the application context.
        // Ryuk will manage the shut-down, so shutdown method is required.
        @JvmStatic
        @BeforeAll
        fun startContainer() = container.start()

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            with(registry) {
                add("spring.neo4j.uri", container::getBoltUrl)
                add("spring.neo4j.authentication.username") { "neo4j" }
                add("spring.neo4j.authentication.password", container::getAdminPassword)
            }
        }
    }

    /** Verifies that the database is empty before each test, and fails if it is not. */
    @BeforeEach
    protected open fun verifyDatabaseIsEmpty(@Autowired client: Neo4jClient) {
        val count = client.query("match (n) return count(n) as count").fetchAs<Long>().one()
        assertThat(count)
            .overridingErrorMessage("Database not empty! Found %d nodes, expected 0.", count)
            .isEqualTo(0L)
    }

    @AfterEach
    fun wipeDatabase() {
        client.query("MATCH (n) DETACH DELETE n").run()
    }

    @Test
    @DisplayName("container is set up and running")
    fun containerStarted() {
        assertThat(container.isRunning).isTrue
    }
}
