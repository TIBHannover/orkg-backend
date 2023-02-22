package eu.tib.orkg.prototype.testing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

const val NEO4J_VERSION = "4.4-community"

@Testcontainers
@SpringBootTest // TODO: This should be "downgraded" to @DataNeo4jTest after decoupling services and output adapters
@TestConstructor(autowireMode = ALL)
abstract class Neo4jTestContainersBaseTest {
    companion object {
        // It is important to not use @Containers here, so we can manage the life-cycle.
        // We instantiate only one container per test class.
        @JvmStatic
        protected val container: Neo4jContainer<*> =
            Neo4jContainer(DockerImageName.parse("neo4j:$NEO4J_VERSION"))
                .withEnv("NEO4JLABS_PLUGINS", """["apoc"]""")
                .withoutAuthentication()

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
                add("spring.data.neo4j.uri", container::getBoltUrl)
                add("spring.data.neo4j.username") { "neo4j" }
                add("spring.data.neo4j.password", container::getAdminPassword)
                add("spring.data.neo4j.use-native-types") { "true" } // TODO: Remove after upgrade, not supported anymore
                /* TODO: Use those values after upgrade
                add("spring.neo4j.uri", container::getBoltUrl)
                add("spring.neo4j.authentication.username") { "neo4j" }
                add("spring.neo4j.authentication.password", container::getAdminPassword)
                */
            }
        }
    }

    /*
    // TODO: Reactivate method once we upgrade to the new SDN version
    /** Verifies that the database is empty before each test, and fails if it is not. */
    @BeforeEach
    protected open fun verifyDatabaseIsEmpty(@Autowired client: Neo4jClient) {
        val count = client.query("match (n) return count(n) as count").fetchAs<Long>().one()
        assertThat(count)
            .overridingErrorMessage("Database not empty! Found %d nodes, expected 0.", count)
            .isEqualTo(0L)
    }
    */

    /*
    // TODO: Reactivate method once we upgrade to the new SDN version
    @AfterEach
    fun wipeDatabase() {
        client.query("MATCH (n) DETACH DELETE n").run()
    }
    */

    @Test
    @DisplayName("container is set up and running")
    fun containerStarted() {
        assertThat(container.isRunning).isTrue
    }
}
