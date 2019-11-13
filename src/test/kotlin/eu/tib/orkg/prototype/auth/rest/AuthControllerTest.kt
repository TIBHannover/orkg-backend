package eu.tib.orkg.prototype.auth.rest

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.auth.rest.AuthController.RegisterUserRequest
import eu.tib.orkg.prototype.auth.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [AuthController::class])
class AuthControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
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
            .andExpect(jsonPath("\$.errors[0].field").value("password"))
            .andExpect(jsonPath("\$.errors[0].message").value("must not be blank"))
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

    private fun registrationOf(user: RegisterUserRequest) =
        post("/auth/register")
            .contentType(APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(user))
}
