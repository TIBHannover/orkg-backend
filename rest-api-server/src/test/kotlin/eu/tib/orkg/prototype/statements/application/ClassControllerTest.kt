package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.AuthorizationServerUnitTestWorkaround
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.statements.api.ClassUseCases
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import io.mockk.every
import java.net.URI
import java.time.OffsetDateTime
import java.util.Optional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [ClassController::class])
@AuthorizationServerUnitTestWorkaround
@DisplayName("Given a Class controller")
class ClassControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var classService: ClassUseCases

    @Suppress("unused") // required by ClassController but not used in the test (yet)
    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Nested
    @DisplayName("When a class with a URI exists")
    inner class URIsExist {
        @Test
        @DisplayName("Then querying for that URI should return `200 OK`")
        fun shouldReturn200() {
            every { classService.findByURI(any()) } returns Optional.of(mockReply())

            mockMvc
                .perform(performGetByURI("http://example.org/exists"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("\$.uri").value("http://example.org/exists"))
        }

        @Test
        @DisplayName("Then creating a new class with the same URI should return `400 Bad Request`")
        fun postShouldReturnError() {
            every { classService.findByURI(any()) } returns Optional.of(mockReply())

            val body = mapOf(
                "label" to "irrelevant",
                "uri" to "http://example.org/exists"
            )

            mockMvc
                .perform(performPost(body))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("\$.status").value(400))
                .andExpect(jsonPath("\$.errors.length()").value(1))
                .andExpect(jsonPath("\$.errors[0].field").value("uri"))
                .andExpect(jsonPath("\$.errors[0].message").value("The URI <http://example.org/exists> is already assigned to class with ID C1."))
        }
    }

    @Nested
    @DisplayName("When no class with a given URI exists")
    inner class URIDoesNotExist {
        @Test
        @DisplayName("Then querying for that URI should return `404 Not Found`")
        fun shouldReturn404() {
            every { classService.findByURI(any()) } returns Optional.empty()

            mockMvc
                .perform(performGetByURI("http://example.org/non-existent"))
                .andExpect(status().isNotFound)
                .andExpect(requestContentIsEmpty())
        }
    }

    private fun requestContentIsEmpty() = content().string("")

    private fun performGetByURI(uri: String) =
        get("/api/classes/?uri=$uri")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")

    private fun performPost(body: Map<String, String>) =
        post("/api/classes/")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))

    private fun mockReply() = Class(
        id = ClassId("C1"),
        label = "test class",
        createdAt = OffsetDateTime.now(),
        uri = URI.create("http://example.org/exists")
    )
}
