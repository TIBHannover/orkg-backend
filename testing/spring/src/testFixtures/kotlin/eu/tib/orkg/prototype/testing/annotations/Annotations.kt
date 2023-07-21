package eu.tib.orkg.prototype.testing.annotations

import io.mockk.junit5.MockKExtension
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.CLASS
import org.junit.jupiter.api.extension.ExtendWith

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
