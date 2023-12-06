package org.orkg.testing.annotations

import io.mockk.junit5.MockKExtension
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.testing.MockUserId
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
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

/**
 * Annotation that helps to setup tests with PostgreSQL in TestContainers.
 *
 * Tests are transactional by default. The test database is replaced with a TestContainers Docker container that is
 * configured via `application.yaml`.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
annotation class TestContainersJpaTest

@Retention(RUNTIME)
@WithMockUser(
    username = MockUserId.ANONYMOUS,
    authorities = ["ROLE_ANONYMOUS"]
)
annotation class TestWithMockAnonymousUser

@Retention(RUNTIME)
@WithMockUser(
    username = MockUserId.USER,
    authorities = ["ROLE_USER"]
)
annotation class TestWithMockUser

@Retention(RUNTIME)
@WithMockUser(
    username = MockUserId.CURATOR,
    authorities = ["ROLE_ADMIN"]
)
annotation class TestWithMockCurator

@Retention(RUNTIME)
@WithMockUser(
    username = MockUserId.ADMIN,
    authorities = ["ROLE_ADMIN"]
)
annotation class TestWithMockAdmin
