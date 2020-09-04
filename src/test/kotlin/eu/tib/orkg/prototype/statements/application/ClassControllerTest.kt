package eu.tib.orkg.prototype.statements.application

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ClassService
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(controllers = [ClassController::class])
@DisplayName("Given a Class controller")
class ClassControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var classService: ClassService

    @Suppress("unused") // required by ClassController but not used in the test (yet)
    @MockkBean
    private lateinit var resourceService: ResourceService

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

    private fun mockReply() = Class(
        id = ClassId("C1"),
        label = "test class",
        createdAt = OffsetDateTime.now(),
        uri = URI.create("http://example.org/exists")
    )
}
