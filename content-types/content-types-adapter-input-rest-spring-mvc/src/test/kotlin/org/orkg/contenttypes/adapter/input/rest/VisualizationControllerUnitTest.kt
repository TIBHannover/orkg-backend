package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.TooManyParameters
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.VisualizationNotFound
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.contenttypes.testing.fixtures.createDummyVisualization
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectVisualization
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [VisualizationController::class, ExceptionHandler::class, CommonJacksonModule::class])
@WebMvcTest(controllers = [VisualizationController::class])
@DisplayName("Given a Visualization controller")
internal class VisualizationControllerUnitTest : RestDocsTest("visualizations") {

    @MockkBean
    private lateinit var visualizationService: VisualizationUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @Test
    @DisplayName("Given a visualization, when it is fetched by id and service succeeds, then status is 200 OK and visualization is returned")
    fun getSingle() {
        val visualization = createDummyVisualization()
        every { visualizationService.findById(visualization.id) } returns Optional.of(visualization)

        documentedGetRequestTo("/api/visualizations/{id}", visualization.id)
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectVisualization()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the visualization to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the visualization."),
                        fieldWithPath("title").description("The title of the visualization."),
                        fieldWithPath("description").description("The description of the visualization."),
                        fieldWithPath("authors").description("The list of authors that originally contributed to the visualization."),
                        fieldWithPath("authors[].id").description("The ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].name").description("The name of the author."),
                        fieldWithPath("authors[].identifiers").description("The unique identifiers of the author."),
                        fieldWithPath("authors[].identifiers.orcid").description("The ORCID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.google_scholar").type("String").description("The Google Scholar ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.research_gate").type("String").description("The ResearchGate ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.linked_in").type("String").description("The LinkedIn ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.wikidata").type("String").description("The Wikidata ID of the author. (optional)").optional(),
                        fieldWithPath("authors[].identifiers.web_of_science").type("String").description("The Web of Science id of the author. (optional)").optional(),
                        fieldWithPath("authors[].homepage").description("The homepage of the author. (optional)").optional(),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the visualization belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the visualization belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the visualization resource. Can be one of "unknown", "manual" or "automatic"."""),
                        timestampFieldWithPath("created_at", "the visualization resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this visualization."),
                        fieldWithPath("visibility").description("""Visibility of the visualization. Can be one of "default", "featured", "unlisted" or "deleted".""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { visualizationService.findById(visualization.id) }
    }

    @Test
    fun `Given a visualization, when it is fetched by id and service reports missing visualization, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = VisualizationNotFound(id)
        every { visualizationService.findById(id) } returns Optional.empty()

        get("/api/visualizations/$id")
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/visualizations/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { visualizationService.findById(id) }
    }

    @Test
    @DisplayName("Given several visualizations, when they are fetched, then status is 200 OK and visualizations are returned")
    fun getPaged() {
        val visualizations = listOf(createDummyVisualization())
        every { visualizationService.findAll(any()) } returns PageImpl(visualizations, PageRequest.of(0, 5), 1)

        documentedGetRequestTo("/api/visualizations")
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectVisualization("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("title").description("Optional filter for the title of the visualization. Uses exact matching.").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "listed", "featured", "unlisted" or "deleted".""").optional(),
                        parameterWithName("created_by").description("Optional filter for the UUID of the user or service who created the visualization.").optional(),
                        parameterWithName("research_field").description("Optional filter for research field id.").optional(),
                        parameterWithName("include_subfields").description("Optional flag for whether subfields are included in the search or not.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { visualizationService.findAll(any()) }
    }

    @Test
    fun `Given several visualizations, when they are fetched by title, then status is 200 OK and visualizations are returned`() {
        val visualizations = listOf(createDummyVisualization())
        val title = visualizations.first().title
        every { visualizationService.findAllByTitle(title, any()) } returns PageImpl(visualizations, PageRequest.of(0, 5), 1)

        get("/api/visualizations?title=$title")
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectVisualization("$.content[*]")

        verify(exactly = 1) { visualizationService.findAllByTitle(title, any()) }
    }

    @Test
    fun `Given several visualizations, when they are fetched by visibility, then status is 200 OK and visualizations are returned`() {
        val visualizations = listOf(createDummyVisualization())
        val visibility = VisibilityFilter.ALL_LISTED
        every { visualizationService.findAllByVisibility(visibility, any()) } returns PageImpl(visualizations, PageRequest.of(0, 5), 1)

        get("/api/visualizations?visibility=$visibility")
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectVisualization("$.content[*]")

        verify(exactly = 1) { visualizationService.findAllByVisibility(visibility, any()) }
    }

    @Test
    fun `Given several visualizations, when they are fetched by contributor id, then status is 200 OK and visualizations are returned`() {
        val visualizations = listOf(createDummyVisualization())
        val contributorId = visualizations.first().createdBy
        every { visualizationService.findAllByContributor(contributorId, any()) } returns PageImpl(visualizations, PageRequest.of(0, 5), 1)

        get("/api/visualizations?created_by=$contributorId")
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { visualizationService.findAllByContributor(contributorId, any()) }
    }

    @Test
    fun `Given several visualizations, when they are fetched but multiple query parameters are given, then status is 400 BAD REQUEST`() {
        val visualizations = listOf(createDummyVisualization())
        val title = visualizations.first().title
        val contributorId = visualizations.first().createdBy
        val exception = TooManyParameters.atMostOneOf("title", "visibility", "created_by")

        get("/api/visualizations?title=$title&created_by=$contributorId")
            .accept(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/visualizations"))
            .andExpect(jsonPath("$.message").value(exception.message))
    }

    @Test
    fun `Given several visualizations, when they are fetched by visibility and research field id, then status is 200 OK and visualizations are returned`() {
        val visualizations = listOf(createDummyVisualization())
        val researchFieldId = ThingId("Science")
        every {
            visualizationService.findAllByResearchFieldAndVisibility(
                researchFieldId = researchFieldId,
                visibility = VisibilityFilter.ALL_LISTED,
                includeSubfields = true,
                pageable = any()
            )
        } returns PageImpl(visualizations, PageRequest.of(0, 5), 1)

        get("/api/visualizations?research_field=$researchFieldId&visibility=ALL_LISTED&include_subfields=true")
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) {
            visualizationService.findAllByResearchFieldAndVisibility(
                researchFieldId = researchFieldId,
                visibility = VisibilityFilter.ALL_LISTED,
                includeSubfields = true,
                pageable = any()
            )
        }
    }
}
