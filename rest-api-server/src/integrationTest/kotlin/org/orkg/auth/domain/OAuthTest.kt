package org.orkg.auth.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orkg.auth.client.OrkgApiClient
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.junit.jupiter.SpringExtension

private const val path = "/oauth/token"

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureTestDatabase(replace = NONE)
class OAuthTest {
    @Autowired
    private lateinit var userService: AuthUseCase

    @Autowired
    private lateinit var userRepository: UserRepository

    @Value("\${local.server.port}")
    private var port: Int = 8000

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Test
    fun test() {
        // TODO: replace with API call and enable transaction management
        userService.registerUser("user@example.org", "user", "User")
        val token = OrkgApiClient(port).getAccessToken("user@example.org", "user", path)
        assertThat(token).isNotNull()
    }

    @Test
    fun `when email address is different between registration and login, it should work nevertheless`() {
        userService.registerUser("This.User@example.org", "user", "User")
        val token = OrkgApiClient(port).getAccessToken("this.user@example.org", "user", path)
        assertThat(token).isNotNull()
    }
}
