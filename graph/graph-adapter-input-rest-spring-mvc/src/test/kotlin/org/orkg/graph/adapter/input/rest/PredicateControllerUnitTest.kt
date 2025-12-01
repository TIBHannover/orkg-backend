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
import org.orkg.community.domain.ContributorNotFound
import org.orkg.graph.adapter.input.rest.PredicateController.CreatePredicateRequest
import org.orkg.graph.adapter.input.rest.PredicateController.UpdatePredicateRequest
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.testing.fixtures.predicateResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.PredicateAlreadyExists
import org.orkg.graph.domain.PredicateInUse
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.PredicateNotModifiable
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
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.Optional

@ContextConfiguration(classes = [PredicateController::class, GraphControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [PredicateController::class])
internal class PredicateControllerUnitTest : MockMvcBaseTest("predicates") {
    @MockkBean
    private lateinit var predicateService: PredicateUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun findById() {
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
            .andDocument {
                summary("Fetching predicates")
                description(
                    """
                    A `GET` request provides information about a predicate.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the predicate to retrieve."),
                )
                responseFields<PredicateRepresentation>(predicateResponseFields())
                throws(PredicateNotFound::class)
            }

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
        val request = CreatePredicateRequest(
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
            .andDocument {
                summary("Creating predicates")
                description(
                    """ 
                    A `POST` request creates a new predicate with a given label.
                    The response will be `201 Created` when successful.
                    The predicate can be retrieved by following the URI in the `Location` header field.
                    """
                )
                requestFields<CreatePredicateRequest>(
                    fieldWithPath("label").description("The label of the predicate."),
                    fieldWithPath("id").type("String").description("The id of the predicate. (optional)").optional()
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created predicate can be fetched from.")
                )
                throws(InvalidLabel::class, PredicateAlreadyExists::class)
            }

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
        val request = CreatePredicateRequest(
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
    fun findAll() {
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
            .andDocument {
                summary("Listing predicates")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<predicates-fetch,predicates>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("q").description("A search term that must be contained in the label. (optional)").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created the predicate. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned predicate can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned predicate can have. (optional)").optional(),
                )
                pagedResponseFields<PredicateRepresentation>(predicateResponseFields())
                throws(UnknownSortingProperty::class)
            }

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
            .andDocument {
                summary("Updating predicates")
                description(
                    """
                    A `PUT` request updates a predicate with the given parameters.
                    The response will be `204 NO CONTENT` when successful.
                    The updated predicate can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the predicate.")
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated predicate can be fetched from.")
                )
                requestFields<UpdatePredicateRequest>(
                    fieldWithPath("label").description("The updated predicate label. (optional)").optional(),
                )
                throws(PredicateNotFound::class, PredicateNotModifiable::class, InvalidLabel::class)
            }

        verify(exactly = 1) { predicateService.update(command) }
    }

    @Test
    @TestWithMockCurator
    fun deleteById() {
        val predicate = createPredicate()
        val contributorId = ContributorId(MockUserId.CURATOR)

        every { predicateService.delete(predicate.id, contributorId) } just runs

        documentedDeleteRequestTo("/api/predicates/{id}", predicate.id)
            .perform()
            .andExpect(status().isNoContent)
            .andDocument {
                summary("Deleting predicates")
                description(
                    """
                    A `DELETE` request with the id of the predicate to delete.
                    The response will be `204 NO CONTENT` when successful.

                    [NOTE]
                    ====
                    1. If the predicate doesn't exist, the return status will be `404 NOT FOUND`.
                    2. If the predicate is not modifiable, the return status will be `403 FORBIDDEN`.
                    3. If the predicate is used in a statement (excluding subject position), the return status will be `403 FORBIDDEN`.
                    4. If the performing user is not the creator of the predicate and does not have the curator role, the return status will be `403 FORBIDDEN`.
                    ====
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the predicate.")
                )
                throws(PredicateNotFound::class, PredicateNotModifiable::class, PredicateInUse::class, ContributorNotFound::class, NeitherOwnerNorCurator::class)
            }

        verify(exactly = 1) { predicateService.delete(predicate.id, contributorId) }
    }
}
