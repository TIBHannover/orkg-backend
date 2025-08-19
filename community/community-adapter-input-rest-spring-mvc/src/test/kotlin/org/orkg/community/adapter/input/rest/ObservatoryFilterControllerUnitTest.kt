package org.orkg.community.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.adapter.input.rest.json.CommunityJacksonModule
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.input.ObservatoryFilterUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createObservatoryFilter
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.Predicates
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectObservatoryFilter
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional
import java.util.UUID

@ContextConfiguration(
    classes = [
        ObservatoryFilterController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        CommunityJacksonModule::class,
        FixedClockConfig::class
    ]
)
@WebMvcTest(controllers = [ObservatoryFilterController::class])
internal class ObservatoryFilterControllerUnitTest : MockMvcBaseTest("observatory-filters") {
    @MockkBean
    private lateinit var observatoryUseCases: ObservatoryUseCases

    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @MockkBean
    private lateinit var observatoryFilterUseCases: ObservatoryFilterUseCases

    @Test
    @DisplayName("Given an observatory filter, when fetched by id, then status is 200 OK and filter is returned")
    fun getSingle() {
        val filter = createObservatoryFilter()
        val observatory = createObservatory()

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryFilterUseCases.findById(filter.id) } returns Optional.of(filter)

        documentedGetRequestTo("/api/observatories/{id}/filters/{filterId}", observatory.id, filter.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectObservatoryFilter()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory that the filter belongs to."),
                        parameterWithName("filterId").description("The identifier of the filter to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the filter."),
                        fieldWithPath("observatory_id").description("The id of the observatory that the filter belongs to."),
                        fieldWithPath("label").description("The label of the filter."),
                        fieldWithPath("path[]").description(PATH_DESCRIPTION),
                        fieldWithPath("range").description(RANGE_DESCRIPTION),
                        fieldWithPath("exact").description(EXACT_MATCH_DESCRIPTION),
                        timestampFieldWithPath("created_at", "the filter was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this filter."),
                        fieldWithPath("featured").description("Whether the filter is featured or not.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { observatoryFilterUseCases.findById(filter.id) }
    }

    @Test
    fun `Given an observatory filter, when fetched by id but observatory is missing, then status is 404 NOT FOUND`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()

        every { observatoryUseCases.findById(observatory.id) } returns Optional.empty()

        get("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
    }

    @Test
    fun `Given an observatory filter, when fetched by id but filter is missing, then status is 404 NOT FOUND`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryFilterUseCases.findById(id) } returns Optional.empty()

        get("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_filter_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { observatoryFilterUseCases.findById(id) }
    }

    @Test
    @DisplayName("Given several observatory filters, when fetched, then status is 200 OK and filters are returned")
    fun getPaged() {
        val filters = pageOf(createObservatoryFilter(), createObservatoryFilter())
        val observatory = createObservatory()

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryFilterUseCases.findAllByObservatoryId(observatory.id, any()) } returns filters

        documentedGetRequestTo("/api/observatories/{id}/filters", observatory.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectObservatoryFilter("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory that the filters belongs to.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { observatoryFilterUseCases.findAllByObservatoryId(observatory.id, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an observatory filter, when deleted by id, then status is 204 NO CONTENT")
    fun delete() {
        val id = createObservatoryFilter().id // keep the ID stable during different test runs
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )

        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryFilterUseCases.deleteById(id) } just runs

        documentedDeleteRequestTo("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .perform()
            .andExpect(status().isNoContent)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory that the filters belongs to."),
                        parameterWithName("filterId").description("The identifier of the filter to delete.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { observatoryFilterUseCases.deleteById(id) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter, when deleted by id but user does not belong to observatory, then status is 403 FORBIDDEN`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )

        every { contributorService.findById(any()) } returns Optional.of(user)

        delete("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .perform()
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:forbidden")

        verify(exactly = 1) { contributorService.findById(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter, when deleted by id but observatory is missing, then status is 204 NO CONTENT`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )

        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryUseCases.findById(observatory.id) } returns Optional.empty()

        delete("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an observatory filter create command, when service succeeds, then status is 201 CREATED and observatory filter is returned")
    fun create() {
        val filter = createObservatoryFilter()
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )

        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.create(any()) } returns filter.id

        documentedPostRequestTo("/api/observatories/{id}/filters", observatory.id)
            .content(command)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/observatories/${observatory.id}/filters/${filter.id}")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory that the filters belongs to.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created filter can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the filter."),
                        fieldWithPath("path[]").description(PATH_DESCRIPTION),
                        fieldWithPath("range").description(RANGE_DESCRIPTION),
                        fieldWithPath("exact").description(EXACT_MATCH_DESCRIPTION),
                        fieldWithPath("featured").description("Whether or not the filter is featured. (optional)")
                            .optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter create command, when service reports observatory does not exist, then status is 404 NOT FOUND`() {
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )
        val exception = ObservatoryNotFound(observatory.id)

        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.create(any()) } throws exception

        post("/api/observatories/${observatory.id}/filters")
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter create command, when service reports class range not found, then status is 404 NOT FOUND`() {
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to ThingId("Missing"),
            "exact" to false,
            "featured" to false
        )
        val exception = ClassNotFound.withThingId(ThingId("Missing"))

        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.create(any()) } throws exception

        post("/api/observatories/${observatory.id}/filters")
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter create command, when service reports predicate in path not found, then status is 404 NOT FOUND`() {
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(ThingId("Missing")),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )
        val exception = PredicateNotFound(ThingId("Missing"))

        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.create(any()) } throws exception

        post("/api/observatories/${observatory.id}/filters")
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:predicate_not_found")

        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter create command, when user does not belong to observatory, then status is 403 FORBIDDEN`() {
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )

        every { contributorService.findById(any()) } returns Optional.of(user)

        post("/api/observatories/${observatory.id}/filters")
            .content(command)
            .perform()
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:forbidden")

        verify(exactly = 1) { contributorService.findById(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an observatory filter update command, when service succeeds, then status is 204 NO CONTENT")
    fun update() {
        val id = createObservatoryFilter().id // keep the ID stable during different test runs
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.update(any()) } just runs

        documentedPatchRequestTo("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .content(command)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/observatories/${observatory.id}/filters/$id")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the observatory that the filters belongs to."),
                        parameterWithName("filterId").description("The identifier of the filter to update.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated filter can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The new label of the filter. (optional)").optional(),
                        fieldWithPath("path[]").description("$PATH_DESCRIPTION (optional)").optional(),
                        fieldWithPath("range").description("$RANGE_DESCRIPTION (optional)").optional(),
                        fieldWithPath("exact").description("$EXACT_MATCH_DESCRIPTION(optional)").optional(),
                        fieldWithPath("featured").description("Whether or not the filter is featured. (optional)")
                            .optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter update command, when service reports observatory does not exist, then status is 404 NOT FOUND`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )
        val exception = ObservatoryNotFound(observatory.id)

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.update(any()) } throws exception

        patch("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter update command, when service reports class range not found, then status is 404 NOT FOUND`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to ThingId("Missing"),
            "exact" to false,
            "featured" to false
        )
        val exception = ClassNotFound.withThingId(ThingId("Missing"))

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.update(any()) } throws exception

        patch("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter update command, when service reports predicate in path not found, then status is 404 NOT FOUND`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = observatory.id
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(ThingId("Missing")),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )
        val exception = PredicateNotFound(ThingId("Missing"))

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { contributorService.findById(any()) } returns Optional.of(user)
        every { observatoryFilterUseCases.update(any()) } throws exception

        patch("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:predicate_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { contributorService.findById(any()) }
        verify(exactly = 1) { observatoryFilterUseCases.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter update command, when user does not belong to observatory, then status is 403 FORBIDDEN`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatory = createObservatory()
        val user = createContributor(
            id = ContributorId(MockUserId.USER),
            observatoryId = ObservatoryId(UUID.randomUUID())
        )
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to Classes.resources,
            "exact" to false,
            "featured" to false
        )

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { contributorService.findById(any()) } returns Optional.of(user)

        patch("/api/observatories/{id}/filters/{filterId}", observatory.id, id)
            .content(command)
            .perform()
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:forbidden")

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { contributorService.findById(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given an observatory filter update command, when observatory does not exist, then status is 404 NOT FOUND`() {
        val id = ObservatoryFilterId(UUID.randomUUID())
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val command = mapOf(
            "label" to "filter",
            "path" to listOf(Predicates.hasResearchProblem),
            "range" to ThingId("Missing"),
            "exact" to false,
            "featured" to false
        )

        every { observatoryUseCases.findById(observatoryId) } returns Optional.empty()

        patch("/api/observatories/{id}/filters/{filterId}", observatoryId, id)
            .content(command)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:observatory_not_found")

        verify(exactly = 1) { observatoryUseCases.findById(observatoryId) }
    }
}

private const val PATH_DESCRIPTION =
    "Describes the path from the contribution node of a paper to the node that should be matched, where every entry stands for the predicate id of a statement."
private const val RANGE_DESCRIPTION =
    "The class id that represents the range of the value that should be matched. Subclasses will also be considered when matching."
private const val EXACT_MATCH_DESCRIPTION =
    "Whether to exactly match the given path. If `true`, the given path needs to exactly match, starting from the contribution resource. If `false`, the given path needs to exactly match, starting at any node in the subgraph of the contribution or the contribution node itself. The total path length limited to 10, including the length of the specified path, starting from the contribution node."
