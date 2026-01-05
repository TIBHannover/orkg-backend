package org.orkg.contenttypes.adapter.input.rest

import com.epages.restdocs.apispec.ParameterType
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.VisualizationController.CreateVisualizationRequest
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.domain.testing.fixtures.createVisualization
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.contenttypes.input.testing.fixtures.authorListFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.visualizationResponseFields
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectVisualization
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

@ContextConfiguration(classes = [VisualizationController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [VisualizationController::class])
internal class VisualizationControllerUnitTest : MockMvcBaseTest("visualizations") {
    @MockkBean
    private lateinit var visualizationService: VisualizationUseCases

    @Test
    @DisplayName("Given a visualization, when it is fetched by id and service succeeds, then status is 200 OK and visualization is returned")
    fun findById() {
        val visualization = createVisualization()
        every { visualizationService.findById(visualization.id) } returns Optional.of(visualization)

        documentedGetRequestTo("/api/visualizations/{id}", visualization.id)
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectVisualization()
            .andDocument {
                summary("Fetching visualizations")
                description(
                    """
                    A `GET` request provides information about a visualization.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the visualization to retrieve."),
                )
                responseFields<VisualizationRepresentation>(visualizationResponseFields())
                throws(VisualizationNotFound::class)
            }

        verify(exactly = 1) { visualizationService.findById(visualization.id) }
    }

    @Test
    fun `Given a visualization, when it is fetched by id and service reports missing visualization, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { visualizationService.findById(id) } returns Optional.empty()

        get("/api/visualizations/{id}", id)
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:visualization_not_found")

        verify(exactly = 1) { visualizationService.findById(id) }
    }

    @Test
    @DisplayName("Given several visualizations, when they are fetched, then status is 200 OK and visualizations are returned")
    fun getPaged() {
        every {
            visualizationService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createVisualization())

        documentedGetRequestTo("/api/visualizations")
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectVisualization("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            visualizationService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several visualizations, when filtering by several parameters, then status is 200 OK and visualizations are returned")
    fun findAll() {
        val visualization = createVisualization()
        every { visualizationService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns pageOf(visualization)

        val label = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val researchProblemId = ThingId("R357")

        documentedGetRequestTo("/api/visualizations")
            .param("title", label)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("research_problem", researchProblemId.value)
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectVisualization("$.content[*]")
            .andDocument {
                summary("Listing visualizations")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<visualizations-fetch,visualizations>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("title").description("A search term that must be contained in the title of the visualization. (optional).").optional(),
                    parameterWithName("exact").description("Whether label matching is exact or fuzzy (optional, default: false)").type(ParameterType.BOOLEAN).optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created the visualization. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned visualization can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned visualization can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the visualization belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the visualization belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("research_field").description("Filter for research field id. The research field of a visualization is determined by the research field of a linking comparison. (optional)").optional(),
                    parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)").optional(),
                    parameterWithName("research_problem").description("Filter for research problem id. (optional)").optional(),
                )
                pagedResponseFields<VisualizationRepresentation>(visualizationResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            visualizationService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe label
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                researchProblem = researchProblemId,
            )
        }
    }

    @Test
    fun `Given several visualizations, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            visualizationService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        get("/api/visualizations")
            .param("sort", "unknown")
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) {
            visualizationService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a create visualization request, when service succeeds, it creates and returns the visualization")
    fun create() {
        val id = ThingId("R123")
        every { visualizationService.create(any()) } returns id

        documentedPostRequestTo("/api/visualizations")
            .content(createVisualizationRequest())
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/visualizations/$id")))
            .andDocument {
                summary("Creating visualizations")
                description(
                    """
                    A `POST` request creates a new visualization with all the given parameters.
                    The response will be `201 Created` when successful.
                    The visualization (object) can be retrieved by following the URI in the `Location` header field.
                    """
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created visualization can be fetched from."),
                )
                requestFields<CreateVisualizationRequest>(
                    fieldWithPath("title").description("The title of the visualization."),
                    fieldWithPath("description").description("The description of the visualization."),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the visualization belongs to."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the visualization belongs to."),
                    fieldWithPath("extraction_method").description("""The method used to extract the visualization resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional(),
                    *authorListFields("visualization").toTypedArray(),
                )
                throws(
                    InvalidLabel::class,
                    InvalidDescription::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                )
            }

        verify(exactly = 1) { visualizationService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create visualization request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneOrganizationAllowed()
        every { visualizationService.create(any()) } throws exception

        post("/api/visualizations")
            .content(createVisualizationRequest())
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_organization_allowed")

        verify(exactly = 1) { visualizationService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create visualization request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneObservatoryAllowed()
        every { visualizationService.create(any()) } throws exception

        post("/api/visualizations")
            .content(createVisualizationRequest())
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_observatory_allowed")

        verify(exactly = 1) { visualizationService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create visualization request, when service reports author not found, then status is 404 NOT FOUND`() {
        val exception = AuthorNotFound(ThingId("R123"))
        every { visualizationService.create(any()) } throws exception

        post("/api/visualizations")
            .content(createVisualizationRequest())
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:author_not_found")

        verify(exactly = 1) { visualizationService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create visualization request, when service reports ambiguous author, then status is 400 BAD REQUEST`() {
        val exception = AmbiguousAuthor(
            Author(
                id = ThingId("R123"),
                name = "author",
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
            )
        )
        every { visualizationService.create(any()) } throws exception

        post("/api/visualizations")
            .content(createVisualizationRequest())
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:ambiguous_author")

        verify(exactly = 1) { visualizationService.create(any()) }
    }

    private fun createVisualizationRequest() =
        CreateVisualizationRequest(
            title = "test",
            description = "visualization description",
            authors = listOf(
                AuthorRequest(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI.create("https://example.org/author")
                ),
                AuthorRequest(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
            organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
            extractionMethod = ExtractionMethod.UNKNOWN
        )
}
