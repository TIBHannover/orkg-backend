package eu.tib.orkg.prototype.testing

import java.math.BigInteger
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

const val POSTGRES_VERSION = "11"

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(
    basePackages = [
        "eu.tib.orkg.prototype.files.adapter.output.jpa",
        "eu.tib.orkg.prototype.discussions.adapter.output.jpa"
    ]
)
@TestConstructor(autowireMode = ALL)
abstract class PostgresTestContainersBaseTest {
    companion object {
        // It is important to not use @Containers here, so we can manage the life-cycle.
        // We instantiate only one container per test class.
        @JvmStatic
        protected val container: PostgreSQLContainer<*> =
            PostgreSQLContainer(DockerImageName.parse("postgres:$POSTGRES_VERSION"))

        // Start the container once per class. This needs to be done via a static method.
        // If @TestInstance(PER_CLASS) is used, Spring fails to set up the application context.
        @JvmStatic
        @BeforeAll
        fun startContainer() = container.start()

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            with(registry) {
                add("spring.datasource.url", container::getJdbcUrl)
                add("spring.datasource.username", container::getUsername)
                add("spring.datasource.password", container::getPassword)
                add("spring.datasource.driver-class-name", container::getDriverClassName)
                add("spring.jpa.database-platform") { "org.hibernate.dialect.PostgreSQLDialect" }
                add("spring.jpa.hibernate.ddl-auto") { "validate" }
                add("spring.jpa.show-sql") { true }
            }
        }
    }

    @Autowired
    protected lateinit var entityManager: EntityManager

    @BeforeEach
    protected fun verifyDatabaseIsEmpty() {
        val tables = entityManager
            .createNativeQuery("SELECT tablename FROM pg_catalog.pg_tables " +
                "WHERE schemaname != 'pg_catalog' " +
                "AND schemaname != 'information_schema';")
            .resultList
            .joinToString(", ")

        val result = entityManager.createNativeQuery("SELECT COUNT(*) FROM $tables;")
            .resultList
            .first()

        assertThat(result)
            .overridingErrorMessage("Database not empty! Found %d rows, expected 0.", result)
            .isEqualTo(BigInteger.ZERO)
    }

    @Test
    @DisplayName("container is set up and running")
    fun containerStarted() {
        assertThat(container.isRunning).isTrue
    }
}
