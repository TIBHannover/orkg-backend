package org.orkg.testing.annotations

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import io.mockk.junit5.MockKExtension
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.testing.MockUserId
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
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
// @MockKExtension.ConfirmVerification // TODO: uncomment after upgrade
// @MockKExtension.CheckUnnecessaryStub // TODO: uncomment after upgrade
annotation class UsesMocking

@SpringBootTest
@ContextConfiguration(initializers = [Neo4jContainerInitializer::class])
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
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
@WithMockAuthentication(authorities = [], name = MockUserId.USER)
annotation class TestWithMockUser

@Retention(RUNTIME)
@WithMockAuthentication(authorities = ["ROLE_CURATOR"], name = MockUserId.CURATOR)
annotation class TestWithMockCurator

@Retention(RUNTIME)
@WithMockAuthentication(authorities = ["ROLE_ADMIN"], name = MockUserId.ADMIN)
annotation class TestWithMockAdmin
