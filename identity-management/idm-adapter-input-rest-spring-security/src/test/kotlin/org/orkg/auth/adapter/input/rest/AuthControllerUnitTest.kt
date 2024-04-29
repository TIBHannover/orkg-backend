package org.orkg.auth.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.LocalDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.internal.verification.VerificationModeFactory.times
import org.orkg.auth.adapter.input.rest.AuthController.RegisterUserRequest
import org.orkg.auth.domain.Role
import org.orkg.auth.domain.User
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.testing.FixedClockConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup
import org.springframework.web.context.WebApplicationContext

@ContextConfiguration(classes = [AuthController::class, ExceptionHandler::class, FixedClockConfig::class])
@WebMvcTest(controllers = [AuthController::class])
class AuthControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: AuthUseCase

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockBean
    private lateinit var userDetailsService: UserDetailsService

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun setup() {
        mockMvc = webAppContextSetup(context)
            .build()
    }

    @Test
    fun whenEmailIsInvalid_ThenFailValidation() {
        val user = RegisterUserRequest(
            email = "invalid",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors[0].field").value("email"))
            .andExpect(jsonPath("\$.errors[0].message").value("must be a well-formed email address"))
    }

    @Test
    fun whenEmailIsEmpty_ThenFailValidation() {
        val user = RegisterUserRequest(
            email = "",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors[0].field").value("email"))
            .andExpect(jsonPath("\$.errors[0].message").value("must not be blank"))
    }

    @Test
    fun whenPasswordIsBlank_ThenFailValidation() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = " ".repeat(5),
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("$.errors[?(@.field == \"password\" && @.message == \"must not be blank\")]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == \"password\" && @.message == \"Please choose a more secure password. It should be longer than 6 characters.\")]").exists())
    }

    @Test
    fun whenPasswordIsTooShort_ThenFailValidation() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = "abc",
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors[0].field").value("password"))
            .andExpect(jsonPath("\$.errors[0].message").value("Please choose a more secure password. It should be longer than 6 characters."))
    }

    @Test
    fun whenMatchingPasswordIsBlank_ThenFailValidation() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = "irrelevant",
            matchingPassword = " ".repeat(5),
            displayName = "irrelevant"
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors[0].field").value("matching_password"))
            .andExpect(jsonPath("\$.errors[0].message").value("must not be blank"))
    }

    @Test
    fun whenDisplayNameIsTooLong_ThenFailValidation() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = "x".repeat(101)
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.errors[0].field").value("display_name"))
            .andExpect(jsonPath("\$.errors[0].message").value("size must be between 1 and 100"))
    }

    @Test
    fun whenEmailIsValid_ThenSucceed() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = null
        )

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isOk)
    }

    @Test
    fun whenRequestIsValidAndEmailDoesExist_ThenFail() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )
        given(userService.findByEmail(anyString())).willReturn(Optional.of(defaultUser()))

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun whenRequestIsValidAndEmailDoesNotExist_ThenCallRegistration() {
        val user = RegisterUserRequest(
            email = "user@example.org",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )
        given(userService.findByEmail(anyString())).willReturn(Optional.empty())

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isOk)
            .andReturn()

        then(userService).should(times(1)).registerUser("user@example.org", "irrelevant", "irrelevant")
    }

    @Test
    fun whenEmailHasCapitalLettersToLowerCase_ThenCallRegistration() {
        val user = RegisterUserRequest(
            email = "USER@EXAMPLE.ORG",
            password = "irrelevant",
            matchingPassword = "irrelevant",
            displayName = "irrelevant"
        )
        given(userService.findByEmail(anyString())).willReturn(Optional.empty())

        mockMvc
            .perform(registrationOf(user))
            .andExpect(status().isOk)

        then(userService).should(times(1)).registerUser("user@example.org", "irrelevant", "irrelevant")
    }

    private fun registrationOf(user: RegisterUserRequest) =
        post("/api/auth/register")
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(user))

    private fun defaultUser() = User(
        id = UUID.randomUUID(),
        email = "user@example.org",
        displayName = "J. Doe",
        password = "!invalid, not a hash",
        enabled = true,
        createdAt = LocalDateTime.now(clock),
        roles = setOf(Role.USER),
        organizationId = null,
        observatoryId = null,
    )
}
