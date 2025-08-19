package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.adapter.input.rest.testing.fixtures.predicateResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPredicate
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(
    classes = [
        PredicateController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        GraphJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [PredicateController::class])
internal class PredicateControllerUnitTest : MockMvcBaseTest("predicates") {
    @MockkBean
    private lateinit var predicateService: PredicateUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun getSingle() {
        val predicate = createPredicate()

        every { predicateService.findById(any()) } returns Optional.of(predicate)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = predicate.id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        documentedGetRequestTo("/api/predicates/{id}", predicate.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPredicate()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the predicate to retrieve."),
                    ),
                    responseFields(predicateResponseFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { predicateService.findById(any()) }
        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = predicate.id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate create request, when service succeeds, then status is 201 CREATED")
    fun create() {
        val request = PredicateController.CreatePredicateRequest(
            id = null,
            label = "predicate label"
        )
        val id = ThingId("R123")

        every { predicateService.create(any()) } returns id

        documentedPostRequestTo("/api/predicates")
            .content(request)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/predicates/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created predicate can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("id").type("String").description("The id of the predicate. (optional)").optional(),
                        fieldWithPath("label").description("The label of the predicate.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            predicateService.create(
                withArg {
                    it.id shouldBe request.id
                    it.label shouldBe request.label
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a predicate create request, when id is specified and service succeeds, then status is 201 CREATED`() {
        val id = ThingId("R123")
        val request = PredicateController.CreatePredicateRequest(
            id = id,
            label = "predicate label"
        )

        every { predicateService.create(any()) } returns id

        post("/api/predicates")
            .content(request)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/predicates/$id")))

        verify(exactly = 1) {
            predicateService.create(
                withArg {
                    it.id shouldBe request.id
                    it.label shouldBe request.label
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                }
            )
        }
    }

    @Test
    @DisplayName("Given several predicates, when filtering by no parameters, then status is 200 OK and predicates are returned")
    fun getPaged() {
        every { predicateService.findAll(any()) } returns pageOf(createPredicate())
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/predicates")
            .accept(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPredicate("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { predicateService.findAll(any()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several predicates, when they are fetched with all possible filtering parameters, then status is 200 OK and predicates are returned")
    fun getPagedWithParameters() {
        every { predicateService.findAll(any(), any(), any(), any(), any()) } returns pageOf(createPredicate())
        every { statementService.findAllDescriptionsById(any<Set<ThingId>>()) } returns emptyMap()

        val label = "label"
        val exact = true
        val createdBy = ContributorId(MockUserId.USER)
        val createdAtStart = OffsetDateTime.now(clock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(clock).plusHours(1)

        documentedGetRequestTo("/api/predicates")
            .param("q", label)
            .param("exact", exact.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(ISO_OFFSET_DATE_TIME))
            .accept(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPredicate("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("A search term that must be contained in the label. (optional)"),
                        parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created the predicate. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned predicate can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned predicate can have. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            predicateService.findAll(
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
    fun `Given several predicates, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { predicateService.findAll(any()) } throws exception

        get("/api/predicates")
            .param("sort", "unknown")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) { predicateService.findAll(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate update command, when service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val predicate = createPredicate(label = "foo")
        val command = UpdatePredicateUseCase.UpdateCommand(
            id = predicate.id,
            contributorId = ContributorId(MockUserId.USER),
            label = predicate.label,
        )
        val request = mapOf(
            "label" to predicate.label,
        )

        every { predicateService.update(command) } just runs

        documentedPutRequestTo("/api/predicates/{id}", predicate.id)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", CoreMatchers.endsWith("/api/predicates/${predicate.id}")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the predicate.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated predicate can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The updated predicate label. (optional)").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { predicateService.update(command) }
    }

    @Test
    @TestWithMockCurator
    fun delete() {
        val predicate = createPredicate()
        val contributorId = ContributorId(MockUserId.CURATOR)

        every { predicateService.delete(predicate.id, contributorId) } just runs

        documentedDeleteRequestTo("/api/predicates/{id}", predicate.id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the predicate.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { predicateService.delete(predicate.id, contributorId) }
    }
}
