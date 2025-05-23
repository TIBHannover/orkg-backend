package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.testing.fixtures.classResponseFields
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectClass
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [ClassController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ClassController::class])
internal class ClassControllerUnitTest : MockMvcBaseTest("classes") {
    @MockkBean
    private lateinit var classService: ClassUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun getSingle() {
        val `class` = createClass()
        every { classService.findById(any()) } returns Optional.of(`class`)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = `class`.id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        documentedGetRequestTo("/api/classes/{id}", `class`.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectClass()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class to retrieve."),
                    ),
                    responseFields(classResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classService.findById(any()) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = `class`.id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun getSingleByUri() {
        val `class` = createClass()
        every { classService.findByURI(any()) } returns Optional.of(`class`)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = `class`.id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        documentedGetRequestTo("/api/classes")
            .param("uri", `class`.uri.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectClass()
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("uri").description("The URI of the class to retrieve")
                    ),
                    responseFields(classResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classService.findByURI(any()) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = `class`.id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    @DisplayName("Given several classes, when filtering by no parameters, then status is 200 OK and classes are returned")
    fun getPaged() {
        every { classService.findAll(any()) } returns pageOf(createClass())
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/classes")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectClass("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classService.findAll(any()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several classes, when they are fetched with all possible filtering parameters, then status is 200 OK and classes are returned")
    fun getPagedWithParameters() {
        every { classService.findAll(any(), any(), any(), any(), any()) } returns pageOf(createClass())
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        val label = "label"
        val exact = true
        val createdBy = ContributorId(MockUserId.USER)
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)

        documentedGetRequestTo("/api/classes")
            .param("q", label)
            .param("exact", exact.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectClass("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)"),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created the class. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned class can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned class can have. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd
            )
        }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given several classes, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { classService.findAll(any()) } throws exception

        get("/api/classes")
            .param("sort", "unknown")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/classes"))

        verify(exactly = 1) { classService.findAll(any()) }
    }

    @Nested
    @DisplayName("When a class with a URI exists")
    inner class URIsExist {
        @Test
        @DisplayName("Then querying for that URI should return `200 OK`")
        fun shouldReturn200() {
            every { classService.findByURI(any()) } returns Optional.of(mockReply())
            every {
                statementService.findAll(
                    pageable = PageRequests.SINGLE,
                    subjectId = any(),
                    predicateId = Predicates.description,
                    objectClasses = setOf(Classes.literal)
                )
            } returns pageOf()

            get("/api/classes")
                .param("uri", "https://example.org/exists")
                .perform()
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.uri").value("https://example.org/exists"))

            verify(exactly = 1) { classService.findByURI(any()) }
            verify(exactly = 1) {
                statementService.findAll(
                    pageable = PageRequests.SINGLE,
                    subjectId = mockReply().id,
                    predicateId = Predicates.description,
                    objectClasses = setOf(Classes.literal)
                )
            }
        }
    }

    @Nested
    @DisplayName("When no class with a given URI exists")
    inner class URIDoesNotExist {
        @Test
        @DisplayName("Then querying for that URI should return `404 Not Found`")
        fun shouldReturn404() {
            every { classService.findByURI(any()) } returns Optional.empty()

            get("/api/classes")
                .param("uri", "http://example.org/non-existent")
                .perform()
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("""Class with URI "http://example.org/non-existent" not found."""))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/classes"))

            verify(exactly = 1) { classService.findByURI(any()) }
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a class is created, when service succeeds, then status is 201 CREATED")
    fun create() {
        val id = ThingId("C123")
        val uri = ParsedIRI("https://example.org/bar")
        val label = "foo"
        val request = mapOf("id" to id, "label" to label, "uri" to uri)

        every { classService.create(any()) } returns id

        documentedPostRequestTo("/api/classes")
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created class can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("id").description("The class id (optional)"),
                        fieldWithPath("label").description("The class label"),
                        fieldWithPath("uri").description("The class URI")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.create(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe label
                    it.uri shouldBe uri
                    it.contributorId.toString() shouldBe MockUserId.USER
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given the class is replaced, when service succeeds, then status is 204 NO CONTENT")
    fun replace() {
        val id = ThingId("EXISTS")
        val body = mapOf(
            "label" to "new label",
            "uri" to "https://example.org/some/new#URI"
        )
        every { classService.replace(any()) } just runs

        documentedPutRequestTo("/api/classes/{id}", id)
            .content(body)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated class can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The updated class label"),
                        fieldWithPath("uri").description("The updated class label")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.replace(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe "new label"
                    it.uri shouldBe ParsedIRI("https://example.org/some/new#URI")
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given the a class label and URI is patched, when service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to "https://example.org/some/new#URI", "label" to "some label")
        every { classService.update(any()) } just runs

        documentedPatchRequestTo("/api/classes/{id}", id)
            .content(body)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/classes/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated class can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The updated class label (optional)"),
                        fieldWithPath("uri").description("The updated class label (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            classService.update(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe "some label"
                    it.uri shouldBe ParsedIRI("https://example.org/some/new#URI")
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given the class label is patched, when service succeeds, then status is 204 NO CONTENT`() {
        val id = ThingId("EXISTS")
        val body = mapOf("label" to "some label")
        every { classService.update(any()) } just runs

        patch("/api/classes/{id}", id)
            .content(body)
            .perform()
            .andExpect(status().isNoContent)

        verify(exactly = 1) {
            classService.update(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe "some label"
                    it.uri shouldBe null
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given the class URI is patched, when service succeeds, then status is 204 NO CONTENT`() {
        val id = ThingId("EXISTS")
        val body = mapOf("uri" to "https://example.org/some/new#URI")
        every { classService.update(any()) } just runs

        patch("/api/classes/{id}", id)
            .content(body)
            .perform()
            .andExpect(status().isNoContent)
        verify(exactly = 1) {
            classService.update(
                withArg {
                    it.id shouldBe id
                    it.label shouldBe null
                    it.uri shouldBe ParsedIRI("https://example.org/some/new#URI")
                }
            )
        }
    }

    private fun mockReply() = Class(
        id = ThingId("C1"),
        label = "test class",
        createdAt = OffsetDateTime.now(clock),
        uri = ParsedIRI("https://example.org/exists")
    )
}
