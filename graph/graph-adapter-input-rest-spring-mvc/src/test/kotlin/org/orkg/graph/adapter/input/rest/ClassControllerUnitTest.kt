package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.adapter.input.rest.testing.fixtures.classResponseFields
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.toOptional
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectClass
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedPatchRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ClassController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ClassController::class])
@DisplayName("Given a Class controller")
internal class ClassControllerUnitTest : RestDocsTest("classes") {

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

    @Autowired
    private lateinit var clock: Clock

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
    @TestWithMockUser
    @DisplayName("Given a class is created, when service succeeds, then status is 200 OK and class is returned")
    fun create() {
        val id = ThingId("C123")
        val uri = URI.create("http://example.org/bar")
        val label = "foo"
        val request = mapOf("id" to id, "label" to label, "uri" to uri)

        every { classService.create(any()) } returns id
        every { classService.findById(id) } returns createClass(id = id, label = label, uri = uri).toOptional()

        post("/api/classes/")
            .content(request)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andExpectClass()
            .andExpect(jsonPath("$.id").value(id.value))
            .andExpect(jsonPath("$.label").value(label))
            .andExpect(jsonPath("$.uri").value(uri.toString()))
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("id").description("The class id (optional)"),
                        fieldWithPath("label").description("The class label"),
                        fieldWithPath("uri").description("The class URI")
                    ),
                    responseFields(classResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.create(withArg {
                it.id shouldBe id
                it.label shouldBe label
                it.uri shouldBe uri
                it.contributorId.toString() shouldBe MockUserId.USER
            })
        }
        verify(exactly = 1) { classService.findById(id) }
    }

    @Test
    @DisplayName("Given the class is replaced, when service succeeds, then status is 200 OK and class is returned")
    fun replace() {
        val id = ThingId("EXISTS")
        val body = mapOf(
            "label" to "new label",
            "uri" to "https://example.org/some/new#URI"
        )
        every { classService.replace(any()) } just runs
        every { classService.findById(id) } returns Optional.of(
            createClass(
                id = id,
                label = "new label",
                uri = URI.create("https://example.org/some/new#URI")
            )
        )

        documentedPutRequestTo("/api/classes/{id}", id)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.label").value("new label"))
            .andExpect(jsonPath("$.uri").value("https://example.org/some/new#URI"))
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("label").description("The updated class label"),
                        fieldWithPath("uri").description("The updated class label")
                    ),
                    responseFields(classResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.replace(withArg {
                it.id shouldBe id
                it.label shouldBe "new label"
                it.uri shouldBe URI.create("https://example.org/some/new#URI")
            })
        }
        verify(exactly = 1) { classService.findById(id) }
    }

    @Test
    @DisplayName("Given the a class label and URI is patched, when service succeeds, then status is 200 OK")
    fun update() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to "https://example.org/some/new#URI", "label" to "some label")
        every { classService.update(any()) } just runs

        documentedPatchRequestTo("/api/classes/{id}", id)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("label").description("The updated class label (optional)"),
                        fieldWithPath("uri").description("The updated class label (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.update(withArg {
                it.id shouldBe id
                it.label shouldBe "some label"
                it.uri shouldBe URI.create("https://example.org/some/new#URI")
            })
        }
    }

    @Test
    fun `Given the class label is patched, when service succeeds, then status is 200 OK`() {
        val id = ThingId("EXISTS")
        val body = mapOf("label" to "some label")
        every { classService.update(any()) } just runs

        patch("/api/classes/{id}", id)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)

        verify(exactly = 1) {
            classService.update(withArg {
                it.id shouldBe id
                it.label shouldBe "some label"
                it.uri shouldBe null
            })
        }
    }

    @Test
    fun `Given the class URI is patched, when service succeeds, then status is 200 OK`() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to "https://example.org/some/new#URI")
        every { classService.update(any()) } just runs

        patch("/api/classes/{id}", id)
            .content(body)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
        verify(exactly = 1) {
            classService.update(withArg {
                it.id shouldBe id
                it.label shouldBe null
                it.uri shouldBe URI.create("https://example.org/some/new#URI")
            })
        }
    }

    private fun performGetByURI(uri: String) =
        get("/api/classes/?uri=$uri")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")

    private fun mockReply() = Class(
        id = ThingId("C1"),
        label = "test class",
        createdAt = OffsetDateTime.now(clock),
        uri = URI.create("http://example.org/exists")
    )
}
