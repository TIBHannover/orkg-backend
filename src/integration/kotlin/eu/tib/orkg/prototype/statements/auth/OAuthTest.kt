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
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(SpringExtension::class)
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
        userService.registerUser("user@example.org", "user", "User")
        val token = OrkgApiClient(port).getAccessToken("user@example.org", "user")
        assertThat(token).isNotNull()
    }
}
