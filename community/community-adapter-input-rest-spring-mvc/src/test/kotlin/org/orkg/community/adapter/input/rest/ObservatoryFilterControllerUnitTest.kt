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
import org.orkg.common.exceptions.Forbidden
import org.orkg.community.adapter.input.rest.ObservatoryFilterController.CreateObservatoryFilterRequest
import org.orkg.community.adapter.input.rest.ObservatoryFilterController.UpdateObservatoryFilterRequest
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryFilterAlreadyExists
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.domain.ObservatoryFilterNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.input.ObservatoryFilterUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.OBSERVATORY_FILTER_EXACT_MATCH_DESCRIPTION
import org.orkg.community.testing.fixtures.OBSERVATORY_FILTER_PATH_DESCRIPTION
import org.orkg.community.testing.fixtures.OBSERVATORY_FILTER_RANGE_DESCRIPTION
import org.orkg.community.testing.fixtures.configuration.CommunityControllerUnitTestConfiguration
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.community.testing.fixtures.createObservatory
import org.orkg.community.testing.fixtures.createObservatoryFilter
import org.orkg.community.testing.fixtures.observatoryFilterResponseFields
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.Predicates
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectObservatoryFilter
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional
import java.util.UUID

@ContextConfiguration(classes = [ObservatoryFilterController::class, CommunityControllerUnitTestConfiguration::class])
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
    fun findById() {
        val filter = createObservatoryFilter()
        val observatory = createObservatory()

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryFilterUseCases.findById(filter.id) } returns Optional.of(filter)

        documentedGetRequestTo("/api/observatories/{id}/filters/{filterId}", observatory.id, filter.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectObservatoryFilter()
            .andDocument {
                summary("Fetching observatory filters")
                description(
                    """
                    A `GET` request provides information about an observatory filter, which always belongs to a specific <<observatories,observatory>>.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory that the filter belongs to."),
                    parameterWithName("filterId").description("The identifier of the filter to retrieve."),
                )
                responseFields<ObservatoryFilterRepresentation>(observatoryFilterResponseFields())
                throws(
                    ObservatoryNotFound::class,
                    ObservatoryFilterNotFound::class
                )
            }

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
    fun findAll() {
        val filters = pageOf(createObservatoryFilter(), createObservatoryFilter())
        val observatory = createObservatory()

        every { observatoryUseCases.findById(observatory.id) } returns Optional.of(observatory)
        every { observatoryFilterUseCases.findAllByObservatoryId(observatory.id, any()) } returns filters

        documentedGetRequestTo("/api/observatories/{id}/filters", observatory.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectObservatoryFilter("$.content[*]")
            .andDocument {
                summary("Listing observatory filters")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<observatory-filters-fetch,observatory filters>> that belong to the specified <<observatories,observatory>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory that the filters belongs to."),
                )
                pagedQueryParameters()
                pagedResponseFields<ObservatoryFilterRepresentation>(observatoryFilterResponseFields())
                throws(ObservatoryNotFound::class)
            }

        verify(exactly = 1) { observatoryUseCases.findById(observatory.id) }
        verify(exactly = 1) { observatoryFilterUseCases.findAllByObservatoryId(observatory.id, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given an observatory filter, when deleted by id, then status is 204 NO CONTENT")
    fun deleteById() {
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
            .andDocument {
                summary("Deleting observatory filters")
                description(
                    """
                    A `DELETE` request deletes an observatory filter.
                    The response will be `204 No Content` when successful.
                    
                    NOTE: The user performing the action needs to be a curator or a member of the observatory.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory that the filters belongs to."),
                    parameterWithName("filterId").description("The identifier of the filter to delete."),
                )
                throws(ObservatoryNotFound::class, ContributorNotFound::class, Forbidden::class)
            }

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
            .andDocument {
                summary("Creating observatory filters")
                description(
                    """
                    A `POST` request creates a new observatory filter with the given parameters.
                    The response will be `201 Created` when successful.
                    The observatory filter can be retrieved by following the URI in the `Location` header field.
                    
                    NOTE: The user performing the action needs to be a curator or a member of the observatory.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory that the filters belongs to."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created filter can be fetched from."),
                )
                requestFields<CreateObservatoryFilterRequest>(
                    fieldWithPath("label").description("The label of the filter."),
                    fieldWithPath("path[]").description(OBSERVATORY_FILTER_PATH_DESCRIPTION),
                    fieldWithPath("range").description(OBSERVATORY_FILTER_RANGE_DESCRIPTION),
                    fieldWithPath("exact").description(OBSERVATORY_FILTER_EXACT_MATCH_DESCRIPTION),
                    fieldWithPath("featured").description("Whether or not the filter is featured. (optional)").optional(),
                )
                throws(
                    ObservatoryNotFound::class,
                    ContributorNotFound::class,
                    Forbidden::class,
                    ClassNotFound::class,
                    PredicateNotFound::class,
                    ObservatoryFilterAlreadyExists::class,
                )
            }

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
            .andDocument {
                summary("Updating observatory filters")
                description(
                    """
                    A `PATCH` request updates an existing observatory filter with the given parameters.
                    The response will be `204 No Content` when successful.
                    
                    NOTE: The user performing the action needs to be a curator or a member of the observatory.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the observatory that the filters belongs to."),
                    parameterWithName("filterId").description("The identifier of the filter to update."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated filter can be fetched from."),
                )
                requestFields<UpdateObservatoryFilterRequest>(
                    fieldWithPath("label").description("The new label of the filter. (optional)").optional(),
                    fieldWithPath("path[]").description("$OBSERVATORY_FILTER_PATH_DESCRIPTION (optional)").optional(),
                    fieldWithPath("range").description("$OBSERVATORY_FILTER_RANGE_DESCRIPTION (optional)").optional(),
                    fieldWithPath("exact").description("$OBSERVATORY_FILTER_EXACT_MATCH_DESCRIPTION (optional)").optional(),
                    fieldWithPath("featured").description("Whether or not the filter is featured. (optional)").optional(),
                )
                throws(
                    ObservatoryNotFound::class,
                    ContributorNotFound::class,
                    Forbidden::class,
                    ObservatoryFilterNotFound::class,
                    ClassNotFound::class,
                    PredicateNotFound::class,
                )
            }

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
