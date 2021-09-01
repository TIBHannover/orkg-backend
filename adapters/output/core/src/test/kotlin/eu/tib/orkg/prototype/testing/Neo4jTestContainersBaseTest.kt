package eu.tib.orkg.prototype.testing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@TestConstructor(autowireMode = ALL)
internal abstract class Neo4jTestContainersBaseTest {
    companion object {
        @JvmStatic
        @Container
        protected val container = Neo4jContainer<Nothing>(
            DockerImageName.parse("neo4j:4.1-community")
        ).apply {
            withoutAuthentication()
        }

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

    /** Custom code for cleaning up the database after each test. */
    protected abstract fun cleanup()

    /** Template method for calling [cleanup] after each test. */
    @AfterEach
    fun callCleanup() = cleanup()

    @Test
    fun `container is set up and running`() {
        assertThat(container.isRunning).isTrue
    }
}
