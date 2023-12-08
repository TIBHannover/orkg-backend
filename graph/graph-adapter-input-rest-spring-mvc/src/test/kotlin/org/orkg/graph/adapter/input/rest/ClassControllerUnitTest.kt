package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.mockk.every
import io.mockk.verify
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.toOptional
import org.orkg.graph.input.AlreadyInUse
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.InvalidURI
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdateClassUseCase.ReplaceCommand
import org.orkg.graph.input.UpdateNotAllowed
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.fixtures.createClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.orkg.graph.input.ClassNotFound as ClassNotFoundProblem
import org.orkg.graph.input.InvalidLabel as InvalidLabelProblem

internal const val INVALID_LABEL = "invalid\nlabel"
internal const val INVALID_URI = "invalid\nuri"

@ContextConfiguration(classes = [ClassController::class, ExceptionHandler::class, CommonJacksonModule::class])
@WebMvcTest(controllers = [ClassController::class])
@DisplayName("Given a Class controller")
internal class ClassControllerUnitTest {

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
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

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
                .andExpect(jsonPath("$.uri").value("http://example.org/exists"))
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
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("uri"))
                .andExpect(jsonPath("$.errors[0].message").value("""The URI <http://example.org/exists> is already assigned to class with ID "C1"."""))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/classes/"))
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("""Class with URI "http://example.org/non-existent" not found."""))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/classes/"))
        }
    }

    @Test
    fun `Given the class is replaced, when service succeeds, then status is 200 OK and class is returned`() {
        val id = ThingId("EXISTS")
        val replacingClass = createClass(id = id, label = "new label")
        val body = objectMapper.writeValueAsString(replacingClass)
        every { classService.replace(id, command = any()) } returns Success(Unit)
        every { classService.findById(id) } returns replacingClass.toOptional()

        mockMvc.performPut("/api/classes/$id", body)
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.id").value(id.toString()))
        verify(exactly = 1) {
            classService.replace(
                id,
                command = ReplaceCommand(label = replacingClass.label, uri = replacingClass.uri)
            )
        }
    }

    @Test
    fun `Given the class is replaced, when service reports class cannot be found, then status is 404 NOT FOUND`() {
        val id = ThingId("NON-EXISTENT")
        val replacingClass = createClass(label = "new label")
        val body = objectMapper.writeValueAsString(replacingClass)
        every { classService.replace(id, command = any()) } returns Failure(ClassNotFoundProblem)

        mockMvc.performPut("/api/classes/$id", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Class "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class is replaced, when service reports label is invalid, then status is 400 BAD REQUEST and returns error information`() {
        val id = ThingId("EXISTS")
        val replacingClass = createClass(label = INVALID_LABEL)
        val body = objectMapper.writeValueAsString(replacingClass)
        every { classService.replace(id, command = any()) } returns Failure(InvalidLabelProblem)

        mockMvc.performPut("/api/classes/$id", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class is replaced, when service reports changing URI is not allowed, then status is 403 FORBIDDEN and returns error information`() {
        val id = ThingId("EXISTS")
        val replacingClass = createClass(label = "new label", uri = URI.create("https://example.com/NEW#uri"))
        val body = objectMapper.writeValueAsString(replacingClass)
        every { classService.replace(id, command = any()) } returns Failure(UpdateNotAllowed)

        mockMvc.performPut("/api/classes/$id", body)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("""The class "EXISTS" already has a URI. It is not allowed to change URIs."""))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class is replaced, when service reports URI is in use, then status is 403 FORBIDDEN and returns error information`() {
        val id = ThingId("EXISTS")
        val replacingClass = createClass(label = "new label", uri = URI.create("https://example.com/NEW#uri"))
        val body = objectMapper.writeValueAsString(replacingClass)
        every { classService.replace(id, command = any()) } returns Failure(AlreadyInUse)

        mockMvc.performPut("/api/classes/$id", body)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("The URI <https://example.com/NEW#uri> is already in use by another class."))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class label is patched, when service reports class cannot be found, then status is 404 NOT FOUND`() {
        val id = ThingId("NON-EXISTENT")
        val body = mapOf("label" to "some label")
        every {
            classService.updateLabel(id, "some label")
        } returns Failure(ClassNotFoundProblem)

        mockMvc.performPatch("/api/classes/$id", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Class "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class label is patched, when service succeeds, then status is 200 OK`() {
        val id = ThingId("EXISTS")
        val body = mapOf("label" to "some label")
        every { classService.updateLabel(id, "some label") } returns Success(Unit)

        mockMvc.performPatch("/api/classes/$id", body).andExpect(status().isOk)
    }

    @Test
    fun `Given the class label is patched and no label is provided, then status is 200 OK and no action was taken`() {
        val dummyId = "EXISTS"
        val body = mapOf("label" to null)

        mockMvc.performPatch("/api/classes/$dummyId", body).andExpect(status().isOk)
        verify(exactly = 0) { classService.updateLabel(any(), any()) }
    }

    @Test
    fun `Given the class label is patched, when service reports the label is invalid, then status is 400 BAD REQUEST and returns error information`() {
        val id = ThingId("EXISTS")
        val body = mapOf("label" to INVALID_LABEL)
        every { classService.updateLabel(id, INVALID_LABEL) } returns Failure(InvalidLabelProblem)

        mockMvc.performPatch("/api/classes/$id", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class URI is patched, when service reports class cannot be found, then status is 404 NOT FOUND`() {
        val id = ThingId("NON-EXISTENT")
        val body = mapOf("uri" to "https://example.org")
        every {
            classService.updateURI(
                id, "https://example.org"
            )
        } returns Failure(ClassNotFoundProblem)

        mockMvc.performPatch("/api/classes/$id", body)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("""Class "$id" not found."""))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class URI is patched and a valid URI is provided, when service succeeds, then status is 200 OK`() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to "https://example.org/some/new#URI")
        every { classService.updateURI(id, "https://example.org/some/new#URI") } returns Success(Unit)

        mockMvc.performPatch("/api/classes/$id", body).andExpect(status().isOk)
        verify(exactly = 1) { classService.updateURI(id, any()) }
    }

    @Test
    fun `Given the class URI is patched, when service reports updating is not allowed, then status is 403 FORBIDDEN and returns error information`() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to INVALID_URI)
        every { classService.updateURI(id, INVALID_URI) } returns Failure(UpdateNotAllowed)

        mockMvc.performPatch("/api/classes/$id", body)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("""The class "EXISTS" already has a URI. It is not allowed to change URIs."""))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class URI is patched, when service reports URI is in use, then status is 403 FORBIDDEN and returns error information`() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to "https://example.org/some/new#URI")
        every { classService.updateURI(id, "https://example.org/some/new#URI") } returns Failure(AlreadyInUse)

        mockMvc.performPatch("/api/classes/$id", body)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("The URI <https://example.org/some/new#URI> is already in use by another class."))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class URI is patched, when service reports URI is invalid, then status is 400 BAD REQUEST and returns error information`() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to INVALID_URI)
        every { classService.updateURI(id, INVALID_URI) } returns Failure(InvalidURI)

        mockMvc.performPatch("/api/classes/$id", body)
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("uri"))
            .andExpect(jsonPath("$.errors[0].message").value("The provided URI is not a valid URI."))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes/$id"))
    }

    @Test
    fun `Given the class URI is patched and no URI is provided, then status is 200 OK and no action was taken`() {
        val dummyId = "EXISTS"
        val body = mapOf("uri" to null)

        mockMvc.performPatch("/api/classes/$dummyId", body).andExpect(status().isOk)
        verify(exactly = 0) { classService.updateURI(any(), any()) }
    }

    private fun MockMvc.performPatch(urlTemplate: String, body: Map<String, Any?>): ResultActions = perform(
        patch(urlTemplate).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))
    )

    private fun MockMvc.performPut(urlTemplate: String, body: String): ResultActions = perform(
        put(urlTemplate).contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8").content(body)
    )

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
        id = ThingId("C1"),
        label = "test class",
        createdAt = OffsetDateTime.now(),
        uri = URI.create("http://example.org/exists")
    )
}
