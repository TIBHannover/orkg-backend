package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.verify
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPredicate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        PredicateController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        GraphJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [PredicateController::class])
@UsesMocking
internal class PredicateControllerUnitTest : RestDocsTest("predicates") {

    @MockkBean
    private lateinit var predicateService: PredicateUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    @TestWithMockUser
    @DisplayName("Given a predicate create request, when service succeeds, it creates and returns the predicate")
    fun create() {
        val request = PredicateController.CreatePredicateRequest(
            id = null,
            label = "predicate label"
        )
        val id = ThingId("R123")
        val predicate = createPredicate(id, label = request.label)

        every { predicateService.create(any()) } returns id
        every { predicateService.findById(id) } returns Optional.of(predicate)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        documentedPostRequestTo("/api/predicates")
            .content(request)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/predicates/$id")))
            .andExpectPredicate()
            .andDo(
                documentationHandler.document(
                    requestFields(
                        fieldWithPath("id").type("String").description("The id of the predicate. (optional)").optional(),
                        fieldWithPath("label").description("The label of the predicate.")
                    ),
                    responseFields(
                        fieldWithPath("id").description("The predicate id."),
                        fieldWithPath("label").description("The predicate label."),
                        timestampFieldWithPath("created_at", "the predicate  was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The ID of the user that created the predicate. All zeros if unknown."),
                        fieldWithPath("description").type("String").description("The description of the predicate, if exists.").optional(),
                        fieldWithPath("modifiable").description("Whether this predicate can be modified."),
                        fieldWithPath("_class").description("Class description, always `predicate`.").optional().ignored()
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
            predicateService.findById(id)
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a predicate create request, when id is specified and service succeeds, it creates and returns the predicate`() {
        val id = ThingId("R123")
        val request = PredicateController.CreatePredicateRequest(
            id = id,
            label = "predicate label"
        )
        val predicate = createPredicate(id, label = request.label)

        every { predicateService.create(any()) } returns id
        every { predicateService.findById(id) } returns Optional.of(predicate)
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        post("/api/predicates")
            .content(objectMapper.writeValueAsString(request))
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/predicates/$id")))
            .andExpectPredicate()

        verify(exactly = 1) {
            predicateService.create(
                withArg {
                    it.id shouldBe request.id
                    it.label shouldBe request.label
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                }
            )
            predicateService.findById(id)
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = id,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    @DisplayName("Given several predicates, when filtering by no parameters, then status is 200 OK and predicates are returned")
    fun getPaged() {
        every { predicateService.findAll(any()) } returns pageOf(createPredicate())
        every { statementService.findAllDescriptions(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/predicates")
            .accept(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPredicate("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { predicateService.findAll(any()) }
        verify(exactly = 1) { statementService.findAllDescriptions(any<Set<ThingId>>()) }
    }

    @Test
    @DisplayName("Given several predicates, when they are fetched with all possible filtering parameters, then status is 200 OK and predicates are returned")
    fun getPagedWithParameters() {
        every { predicateService.findAll(any(), any(), any(), any(), any()) } returns pageOf(createPredicate())
        every { statementService.findAllDescriptions(any<Set<ThingId>>()) } returns emptyMap()

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
        verify(exactly = 1) { statementService.findAllDescriptions(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given several predicates, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every { predicateService.findAll(any()) } throws exception

        mockMvc.perform(get("/api/predicates?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/predicates"))
    }
}
