package testhelper.spring.testcontainers

import java.time.Duration
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for running a test with Neo4j using TestContainers.
 *
 * This class **does not** add any Spring test annotations. It leaves setting up the context up to
 * the individual test classes. It will, however, dirty the context, so context caching is no longer
 * possible.
 *
 * Unfortunately, Liquigraph **will not work** with this solution due to issues in wiring components
 * and the general design decisions in Liquigraph (for the time being). It needs to be disabled for
 * all test.
 */
// FIXME: This should most likely also be a JUnit Extension to allow for greater flexibility and
//        less coupling. Unfortunately I did not get that to work yet.
@Testcontainers
@TestInstance(PER_METHOD)
@DirtiesContext
abstract class Neo4jContainerTest {
  companion object {
    @Container
    @JvmField // Important to make it accessible. @JvmStatic works, but will make it private.
    protected val neo4jContainer =
        Neo4jContainer<Nothing>("neo4j:3.5").apply {
          withAdminPassword(disabled)
          withStartupTimeout(Duration.ofMinutes(1))
        }

    @DynamicPropertySource
    @JvmStatic
    @Suppress("unused") // automatically called by Spring Boot, no direct invocation
    private fun configureProperties(registry: DynamicPropertyRegistry) {
      // The @DynamicPropertySource mechanism will be executed by Spring when setting up the
      // ApplicationContext.
      // The registry registers (no s**t!) callback handlers to obtain the values when needed.
      // Although the container is only started once per class (because of static initialization)
      // the test needs to be marked with a [PER_METHOD] lifecycle. This will create a new class for
      // each test and is the default in JUnit anyway. Those classes share the container.
      // Setting the class lifecycle to [PER_CLASS] will not work because the callbacks get called
      // before the container is started.
      // Modifying the context while starting up will break context caching (default in Spring Boot)
      // because following contexts will try to connect to a container port that is no longer
      // available.
      // We need to dirty the context to force Spring to re-build it. This has some performance
      // impact that we need to sacrifice for correctness.
      // -- 2021-03-15, MP
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
