package org.orkg.testing

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Initializer to start a Postgres instance using TestContainers.
 */
class PostgresContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    // TODO: might be nice to aggregate values for debugging, if possible

    companion object {
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:$POSTGRES_VERSION"))
            .withReuse(true)
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        postgresContainer.start()
        TestPropertyValues.of(postgresContainer.settings()).applyTo(applicationContext)
    }

    private fun PostgreSQLContainer<*>.settings() = listOf(
        "spring.datasource.url=${getJdbcUrl()}",
        "spring.datasource.username=${username}",
        "spring.datasource.password=${password}",
        "spring.datasource.driver-class-name=${driverClassName}",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.show-sql=true",
    )
}
