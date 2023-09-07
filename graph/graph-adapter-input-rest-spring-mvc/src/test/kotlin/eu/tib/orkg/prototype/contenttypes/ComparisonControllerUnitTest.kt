package eu.tib.orkg.prototype.contenttypes

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.contenttypes.api.ComparisonUseCases
import eu.tib.orkg.prototype.contenttypes.application.COMPARISON_JSON_V2
import eu.tib.orkg.prototype.contenttypes.application.ComparisonController
import eu.tib.orkg.prototype.contenttypes.application.ComparisonNotFound
import eu.tib.orkg.prototype.contenttypes.application.ComparisonRelatedFigureNotFound
import eu.tib.orkg.prototype.contenttypes.application.ComparisonRelatedResourceNotFound
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.createDummyComparison
import eu.tib.orkg.prototype.createDummyComparisonRelatedFigure
import eu.tib.orkg.prototype.createDummyComparisonRelatedResource
import eu.tib.orkg.prototype.pageOf
import eu.tib.orkg.prototype.shared.TooManyParameters
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.testing.andExpectComparison
import eu.tib.orkg.prototype.testing.andExpectComparisonRelatedFigure
import eu.tib.orkg.prototype.testing.andExpectComparisonRelatedResource
import eu.tib.orkg.prototype.testing.andExpectPage
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import eu.tib.orkg.prototype.testing.spring.restdocs.timestampFieldWithPath
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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

@ContextConfiguration(classes = [ComparisonController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [ComparisonController::class])
@DisplayName("Given a Comparison controller")
internal class ComparisonControllerUnitTest : RestDocsTest("comparisons") {

    @MockkBean
    private lateinit var comparisonService: ComparisonUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: AuthUseCase

    @Test
    @DisplayName("Given a comparison, when it is fetched by id and service succeeds, then status is 200 OK and comparison is returned")
    fun getSingle() {
        val comparison = createDummyComparison()
        every { comparisonService.findById(comparison.id) } returns Optional.of(comparison)

        documentedGetRequestTo("/api/comparisons/{id}", comparison.id)
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparison()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison."),
                        fieldWithPath("title").description("The title of the comparison."),
                        fieldWithPath("description").description("The description of the comparison."),
                        fieldWithPath("research_fields").description("The list of research fields the comparison is assigned to."),
                        fieldWithPath("research_fields[].id").description("The id of the research field."),
                        fieldWithPath("research_fields[].label").description("The label of the research field."),
                        fieldWithPath("identifiers").description("The unique identifiers of the comparison."),
                        fieldWithPath("identifiers.doi").description("The DOI of the comparison. (optional)").optional(),
                        fieldWithPath("publication_info").description("The publication info of the comparison.").optional(),
                        fieldWithPath("publication_info.published_month").description("The month in which the comparison was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_year").description("The year in which the comparison was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_in").description("The venue where the comparison was published. (optional)").optional(),
                        fieldWithPath("publication_info.url").description("The URL to the original comparison. (optional)").optional(),
                        fieldWithPath("authors").description("The list of authors that originally contributed to the comparison."),
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
                        fieldWithPath("contributions").description("The list of contributions of the comparison."),
                        fieldWithPath("contributions[].id").description("The ID of the contribution."),
                        fieldWithPath("contributions[].label").description("The label of the contribution."),
                        fieldWithPath("visualizations").description("The list of visualizations of the comparison."),
                        fieldWithPath("visualizations[].id").description("The ID of the visualization."),
                        fieldWithPath("visualizations[].label").description("The label of the visualization."),
                        fieldWithPath("related_figures").description("The list of related figures of the comparison."),
                        fieldWithPath("related_figures[].id").description("The ID of the related figure."),
                        fieldWithPath("related_figures[].label").description("The label of the related figure."),
                        fieldWithPath("related_resources").description("The list of related resources of the comparison."),
                        fieldWithPath("related_resources[].id").description("The ID of the related resource."),
                        fieldWithPath("related_resources[].label").description("The label of the related resource."),
                        fieldWithPath("references[]").description("The list of references of the comparison."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the comparison belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the comparison belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the comparison resource. Can be one of "unknown", "manual" or "automatic"."""),
                        timestampFieldWithPath("created_at", "the comparison resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this comparison."),
                        fieldWithPath("previous_version").description("The ID of the resource of a previous version of the comparison."),
                        fieldWithPath("is_anonymized").description("Whether or not the comparison is anonymized."),
                        fieldWithPath("visibility").description("""Visibility of the comparison. Can be one of "default", "featured", "unlisted" or "deleted".""")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.findById(comparison.id) }
    }

    @Test
    fun `Given a comparison, when it is fetched by id and service reports missing comparison, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = ComparisonNotFound(id)
        every { comparisonService.findById(id) } returns Optional.empty()

        get("/api/comparisons/$id")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.findById(id) }
    }

    @Test
    @DisplayName("Given several comparisons, when they are fetched, then status is 200 OK and comparisons are returned")
    fun getPaged() {
        val comparisons = listOf(createDummyComparison())
        every { comparisonService.findAll(any()) } returns PageImpl(comparisons, PageRequest.of(0, 5), 1)

        documentedGetRequestTo("/api/comparisons")
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparison("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("research_field").description("Optional filter for research field id.").optional(),
                        parameterWithName("title").description("Optional filter for the title of the comparison. Uses exact matching.").optional(),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "listed", "featured", "unlisted" or "deleted".""").optional(),
                        parameterWithName("created_by").description("Optional filter for research field id.").optional(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.findAll(any()) }
    }

    @Test
    fun `Given several comparisons, when they are fetched by doi, then status is 200 OK and comparisons are returned`() {
        val comparisons = listOf(createDummyComparison())
        val doi = comparisons.first().identifiers["doi"]!!
        every { comparisonService.findAllByDOI(doi, any()) } returns PageImpl(comparisons, PageRequest.of(0, 5), 1)

        get("/api/comparisons?doi=$doi")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparison("$.content[*]")

        verify(exactly = 1) { comparisonService.findAllByDOI(doi, any()) }
    }

    @Test
    fun `Given several comparisons, when they are fetched by title, then status is 200 OK and comparisons are returned`() {
        val comparisons = listOf(createDummyComparison())
        val title = comparisons.first().title
        every { comparisonService.findAllByTitle(title, any()) } returns PageImpl(comparisons, PageRequest.of(0, 5), 1)

        get("/api/comparisons?title=$title")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparison("$.content[*]")

        verify(exactly = 1) { comparisonService.findAllByTitle(title, any()) }
    }

    @Test
    fun `Given several comparisons, when they are fetched by visibility, then status is 200 OK and comparisons are returned`() {
        val comparisons = listOf(createDummyComparison())
        val visibility = VisibilityFilter.ALL_LISTED
        every { comparisonService.findAllByVisibility(visibility, any()) } returns PageImpl(comparisons, PageRequest.of(0, 5), 1)

        get("/api/comparisons?visibility=$visibility")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparison("$.content[*]")

        verify(exactly = 1) { comparisonService.findAllByVisibility(visibility, any()) }
    }

    @Test
    fun `Given several comparisons, when they are fetched by contributor id, then status is 200 OK and comparisons are returned`() {
        val comparisons = listOf(createDummyComparison())
        val contributorId = comparisons.first().createdBy
        every { comparisonService.findAllByContributor(contributorId, any()) } returns PageImpl(comparisons, PageRequest.of(0, 5), 1)

        get("/api/comparisons?created_by=$contributorId")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.number").value(0)) // page number
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { comparisonService.findAllByContributor(contributorId, any()) }
    }

    @Test
    fun `Given several comparisons, when they are fetched but multiple query parameters are given, then status is 400 BAD REQUEST`() {
        val comparisons = listOf(createDummyComparison())
        val title = comparisons.first().title
        val contributorId = comparisons.first().createdBy
        val exception = TooManyParameters.atMostOneOf("doi", "title", "visibility", "created_by")

        get("/api/comparisons?title=$title&created_by=$contributorId")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))
    }

    @Test
    @DisplayName("Given a comparison related resource, when fetched by id, then status is 200 OK and comparison related resource is returned")
    fun relatedResourceGetSingle() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = createDummyComparisonRelatedResource()

        every {
            comparisonService.findRelatedResourceById(comparisonId, comparisonRelatedResource.id)
        } returns Optional.of(comparisonRelatedResource)

        documentedGetRequestTo("/api/comparisons/{comparisonId}/related-resources/{id}", comparisonId, comparisonRelatedResource.id)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonRelatedResource()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("comparisonId").description("The identifier of the comparison."),
                        parameterWithName("id").description("The identifier of the comparison related resource to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison related resource."),
                        fieldWithPath("label").description("The title of label comparison related resource."),
                        fieldWithPath("image").description("The url for the image of the comparison related resource."),
                        fieldWithPath("url").description("The url of the comparison related resource."),
                        fieldWithPath("description").description("The description of the comparison related resource."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.findRelatedResourceById(comparisonId, comparisonRelatedResource.id) }
    }

    @Test
    fun `Given a comparison related resource, when fetched by id but service reports missing comparison related resource, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResourceId = ThingId("R1435")
        val exception = ComparisonRelatedResourceNotFound(comparisonRelatedResourceId)

        every {
            comparisonService.findRelatedResourceById(comparisonId, comparisonRelatedResourceId)
        } returns Optional.empty()

        get("/api/comparisons/$comparisonId/related-resources/$comparisonRelatedResourceId")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-resources/$comparisonRelatedResourceId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.findRelatedResourceById(comparisonId, comparisonRelatedResourceId) }
    }

    @Test
    fun `Given several comparison related resources, when fetched, then status is 200 OK and comparison related resources are returned`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = listOf(createDummyComparisonRelatedResource())

        every { comparisonService.findAllRelatedResources(comparisonId, any()) } returns pageOf(comparisonRelatedResource)

        get("/api/comparisons/$comparisonId/related-resources")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedResource("$.content[*]")

        verify(exactly = 1) { comparisonService.findAllRelatedResources(comparisonId, any()) }
    }

    @Test
    @DisplayName("Given a comparison related figure, when fetched by id, then status is 200 OK and comparison related figure is returned")
    fun relatedFigureGetSingle() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigure = createDummyComparisonRelatedFigure()

        every {
            comparisonService.findRelatedFigureById(comparisonId, comparisonRelatedFigure.id)
        } returns Optional.of(comparisonRelatedFigure)

        documentedGetRequestTo("/api/comparisons/{comparisonId}/related-figures/{id}", comparisonId, comparisonRelatedFigure.id)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonRelatedFigure()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("comparisonId").description("The identifier of the comparison."),
                        parameterWithName("id").description("The identifier of the comparison related figure to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison related figure."),
                        fieldWithPath("label").description("The title of label comparison related figure."),
                        fieldWithPath("image").description("The url for the image of the comparison related figure."),
                        fieldWithPath("description").description("The description of the comparison related figure."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.findRelatedFigureById(comparisonId, comparisonRelatedFigure.id) }
    }

    @Test
    fun `Given a comparison related figure, when fetched by id but service reports missing comparison related figure, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigureId = ThingId("R1435")
        val exception = ComparisonRelatedFigureNotFound(comparisonRelatedFigureId)

        every {
            comparisonService.findRelatedFigureById(comparisonId, comparisonRelatedFigureId)
        } returns Optional.empty()

        get("/api/comparisons/$comparisonId/related-figures/$comparisonRelatedFigureId")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-figures/$comparisonRelatedFigureId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.findRelatedFigureById(comparisonId, comparisonRelatedFigureId) }
    }

    @Test
    fun `Given several comparison related figures, when fetched, then status is 200 OK and comparison related figures are returned`() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigure = listOf(createDummyComparisonRelatedFigure())

        every { comparisonService.findAllRelatedFigures(comparisonId, any()) } returns pageOf(comparisonRelatedFigure)

        get("/api/comparisons/$comparisonId/related-figures")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedFigure("$.content[*]")

        verify(exactly = 1) { comparisonService.findAllRelatedFigures(comparisonId, any()) }
    }
}
