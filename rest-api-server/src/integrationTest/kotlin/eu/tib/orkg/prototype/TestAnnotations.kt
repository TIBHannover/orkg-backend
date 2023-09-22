package eu.tib.orkg.prototype

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

/**
 * Annotation that helps to setup tests with PostgreSQL in TestContainers.
 *
 * Tests are transactional by default. The test database is replaced with a TestContainers Docker container that is
 * configured via `application.yaml`.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
annotation class TestContainersJpaTest
