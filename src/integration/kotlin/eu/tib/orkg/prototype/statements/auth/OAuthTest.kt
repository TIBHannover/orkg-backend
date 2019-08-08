package eu.tib.orkg.prototype.statements.auth

import eu.tib.orkg.prototype.auth.service.UserService
import eu.tib.orkg.prototype.statements.client.OrkgApiClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [OAuthTest.Initializer::class])
@AutoConfigureTestDatabase(replace = NONE)
class OAuthTest {
    @Autowired
    private lateinit var userService: UserService

    @LocalServerPort
    private var port: Int = 8000

    @Test
    @Suppress("UsePropertyAccessSyntax")
    fun test() {
        // TODO: replace with API call and enable transaction management
        userService.registerUser("user@example.org", "user")
        val token = OrkgApiClient(port).getAccessToken("user@example.org", "user")
        assertThat(token).isNotNull()
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            postgres.start()
            TestPropertyValues.of(
                "spring.datasource.url=${postgres.jdbcUrl}",
                "spring.datasource.username=${postgres.username}",
                "spring.datasource.password=${postgres.password}",
                "spring.datasource.driver-class-name=org.postgresql.Driver"
            ).applyTo(applicationContext.environment)
        }

        companion object {
            private val postgres: KPostgreSQLContainer by lazy {
                KPostgreSQLContainer("postgres:11").apply {
                    withDatabaseName("postgres")
                    withUsername("postgres")
                    withPassword("postgres")
                }
            }
        }
    }
}

/**
 * This is a class used as a work-around. Kotlin is "confused" by the use of Generics in
 * the TestContainers library. Generating a sub-class avoids this problem.
 */
class KPostgreSQLContainer(imageName: String) : PostgreSQLContainer<KPostgreSQLContainer>(imageName)
