package org.orkg.testing

import java.math.BigInteger
import javax.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.testcontainers.junit.jupiter.Testcontainers

const val POSTGRES_VERSION = "11"

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(
    basePackages = [
        "org.orkg.mediastorage.adapter.output.jpa",
        "org.orkg.community.adapter.output.jpa",
        "org.orkg.discussions.adapter.output.jpa",
        "org.orkg.auth.adapter.output.jpa.spring",
    ]
)
@TestConstructor(autowireMode = ALL)
abstract class PostgresTestContainersBaseTest {

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
        assertThat(PostgresContainerInitializer.postgresContainer.isRunning).isTrue
    }
}
