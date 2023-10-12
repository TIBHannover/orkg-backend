package eu.tib.orkg.prototype.testing.annotations

import eu.tib.orkg.prototype.testing.Neo4jContainerInitializer
import io.mockk.junit5.MockKExtension
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

/**
 * Marks a test as using mocking (via MockK).
 *
 * The annotation ensures that [MockKExtension] is applied.
 */
@Retention(RUNTIME)
@Target(CLASS)
@ExtendWith(MockKExtension::class)
//@MockKExtension.ConfirmVerification // TODO: uncomment after upgrade
//@MockKExtension.CheckUnnecessaryStub // TODO: uncomment after upgrade
annotation class UsesMocking

@SpringBootTest
@ContextConfiguration(initializers = [Neo4jContainerInitializer::class])
annotation class Neo4jContainerIntegrationTest
