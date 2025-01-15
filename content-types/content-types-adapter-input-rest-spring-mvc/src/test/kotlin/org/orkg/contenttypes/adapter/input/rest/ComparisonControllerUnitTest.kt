package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotFound
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotFound
import org.orkg.contenttypes.domain.ComparisonType
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonConfig
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonData
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonRelatedFigure
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonRelatedResource
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectComparison
import org.orkg.testing.andExpectComparisonRelatedFigure
import org.orkg.testing.andExpectComparisonRelatedResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
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

@ContextConfiguration(classes = [ComparisonController::class, ExceptionHandler::class, CommonJacksonModule::class, ContentTypeJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ComparisonController::class])
internal class ComparisonControllerUnitTest : RestDocsTest("comparisons") {

    @MockkBean
    private lateinit var comparisonService: ComparisonUseCases

    @Test
    @DisplayName("Given a comparison, when it is fetched by id and service succeeds, then status is 200 OK and comparison is returned")
    fun getSingle() {
        val comparison = createComparison()
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
                        fieldWithPath("contributions").description("The list of contributions of the comparison."),
                        fieldWithPath("contributions[].id").description("The ID of the contribution."),
                        fieldWithPath("contributions[].label").description("The label of the contribution."),
                        fieldWithPath("config").description("The configuration of the comparison."),
                        fieldWithPath("config.predicates").description("The list of labels of the predicates used in the comparison."),
                        fieldWithPath("config.contributions").description("The list of ids of contributions that are being compared in the comparison."),
                        fieldWithPath("config.transpose").description("Whether the comparison table is transposed."),
                        fieldWithPath("config.type").description("The type of method used to create the comparison. Either of ${ComparisonType.entries.joinToString { "\"$it\"" }}."),
                        // The short_codes field exists for compatibility reasons, but should not end up in the documentation, as it allows for arbitrary inputs.
                        fieldWithPath("config.short_codes").description("The list of short form ids for the comparison.").optional().ignored(),
                        fieldWithPath("data").description("The data contained in the comparison."),
                        fieldWithPath("data.contributions").description("The list of contributions that are being compared in the comparison."),
                        fieldWithPath("data.contributions[].id").description("The id of the contribution."),
                        fieldWithPath("data.contributions[].label").description("The label of the contribution."),
                        fieldWithPath("data.contributions[].paper_id").description("The id of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].paper_label").description("The label of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].paper_year").description("The publication year of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].active").description("Whether the contribution (column or row if transposed) should be displayed."),
                        fieldWithPath("data.predicates").description("The list of predicates used in the comparison."),
                        fieldWithPath("data.predicates[].id").description("When the comparison type is \"MERGE\", this is the predicate id. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
                        fieldWithPath("data.predicates[].label").description("When the comparison type is \"MERGE\", this is the label of the predicate. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
                        fieldWithPath("data.predicates[].n_contributions").description("The count of contributions that contain a statements for the predicate."),
                        fieldWithPath("data.predicates[].active").description("Whether the predicate (row or column if transposed) should be displayed."),
                        fieldWithPath("data.predicates[].similar_predicates").description("The list of similar predicate labels."),
                        fieldWithPath("data.data").description("The values of the comparison."),
                        fieldWithPath("data.data.*").description("A map of predicate ids to the values for each contribution."),
                        fieldWithPath("data.data.*[]").description("All values for the predicate in the comparison. This corresponds to a row (or column if transposed) of the comparison."),
                        fieldWithPath("data.data.*[][]").description("All values for the predicate and a single contribution. Every value corresponds to a single cell of the comparison."),
                        fieldWithPath("data.data.*[][].id").description("The id of the orkg entity behind the value."),
                        fieldWithPath("data.data.*[][].label").description("The label of the orkg entity behind the value. This corresponds to the cell value."),
                        fieldWithPath("data.data.*[][].classes").description("The classes of the orkg entity, if it is a resource, empty otherwise."),
                        fieldWithPath("data.data.*[][].path").description("The predicate path (ids) of the value within the contribution."),
                        fieldWithPath("data.data.*[][].path_labels").description("The corresponding predicate labels of the predicate path."),
                        fieldWithPath("data.data.*[][]._class").description("The type of the orkg entity behind the value. Either of \"class\", \"resource\", \"predicate\" or \"literal\"."),
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
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the comparison belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the comparison belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the comparison resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC"."""),
                        timestampFieldWithPath("created_at", "the comparison resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this comparison."),
                        fieldWithPath("versions.head").description("The head version of the comparison."),
                        fieldWithPath("versions.head.id").description("The id of the head version."),
                        fieldWithPath("versions.head.label").description("The label of the head version."),
                        timestampFieldWithPath("versions.head.created_at", "the head version was created"),
                        fieldWithPath("versions.head.created_by").type("String").description("The UUID of the user or service who created the comparison version."),
                        fieldWithPath("versions.published").description("The list of published versions of the comparison."),
                        fieldWithPath("versions.published[].id").description("The id of the published version."),
                        fieldWithPath("versions.published[].label").description("The label of the published version."),
                        timestampFieldWithPath("versions.published[].created_at", "the published version was created"),
                        fieldWithPath("versions.published[].created_by").type("String").description("The UUID of the user or service who created the comparison version."),
                        fieldWithPath("versions.published[].changelog").description("The changelog of the comparison version. (currently unsupported)"),
                        fieldWithPath("is_anonymized").description("Whether or not the comparison is anonymized."),
                        fieldWithPath("visibility").description("""Visibility of the comparison. Can be one of "DEFAULT", "FEATURED", "UNLISTED" or "DELETED"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this comparison.").optional(),
                        fieldWithPath("published").description("Whether the comparison is published or not."),
                        fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `comparison`."),
                    ).and(authorListFields("comparison"))
                        .and(publicationInfoFields("comparison"))
                        .and(sustainableDevelopmentGoalsFields("comparison"))
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

        get("/api/comparisons/{id}", id)
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
        every {
            comparisonService.findAll(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        } returns pageOf(createComparison())

        documentedGetRequestTo("/api/comparisons")
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparison("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonService.findAll(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    @DisplayName("Given several comparisons, when filtering by several parameters, then status is 200 OK and comparisons are returned")
    fun getPagedWithParameters() {
        every {
            comparisonService.findAll(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        } returns pageOf(createComparison())

        val title = "label"
        val exact = true
        val doi = "10.123/8764"
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val published = true
        val sdg = ThingId("SDG_1")
        val researchProblemId = ThingId("R789")

        documentedGetRequestTo("/api/comparisons")
            .param("title", title)
            .param("exact", exact.toString())
            .param("doi", doi)
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("published", published.toString())
            .param("sdg", sdg.value)
            .param("research_problem", researchProblemId.value)
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparison("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("title").description("A search term that must be contained in the title of the comparison. (optional)"),
                        parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("doi").description("Filter for the DOI of the comparison. (optional)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED"."""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this comparison. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)"),
                        parameterWithName("research_field").description("Filter for research field id. (optional)"),
                        parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
                        parameterWithName("published").description("Filter for the publication status of the comparison. (optional)"),
                        parameterWithName("sdg").description("Filter for the sustainable development goal that the comparison belongs to. (optional)"),
                        parameterWithName("research_problem").description("Filter for the research problem id the comparison is related to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe title
                },
                doi = doi,
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                published = published,
                sustainableDevelopmentGoal = sdg,
                researchProblem = researchProblemId
            )
        }
    }

    @Test
    fun `Given several comparisons, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            comparisonService.findAll(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        } throws exception

        get("/api/comparisons")
            .param("sort", "unknown")
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/comparisons"))

        verify(exactly = 1) {
            comparisonService.findAll(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        }
    }

    @Test
    @DisplayName("Given a comparison related resource, when fetched by id, then status is 200 OK and comparison related resource is returned")
    fun relatedResourceGetSingle() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = createComparisonRelatedResource()

        every {
            comparisonService.findRelatedResourceById(comparisonId, comparisonRelatedResource.id)
        } returns Optional.of(comparisonRelatedResource)

        documentedGetRequestTo("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResource.id)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonRelatedResource()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedResourceId").description("The identifier of the comparison related resource to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison related resource."),
                        fieldWithPath("label").description("The title of label comparison related resource."),
                        fieldWithPath("image").description("The url for the image of the comparison related resource."),
                        fieldWithPath("url").description("The url of the comparison related resource."),
                        fieldWithPath("description").description("The description of the comparison related resource."),
                        timestampFieldWithPath("created_at", "the comparison related resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this comparison related resource.")
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

        get("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResourceId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-resources/$comparisonRelatedResourceId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.findRelatedResourceById(comparisonId, comparisonRelatedResourceId) }
    }

    @Test
    @DisplayName("Given several comparison related resources, when fetched, then status is 200 OK and comparison related resources are returned")
    fun relatedResourceGetPaged() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResource = listOf(createComparisonRelatedResource())

        every { comparisonService.findAllRelatedResources(comparisonId, any()) } returns pageOf(comparisonRelatedResource)

        documentedGetRequestTo("/api/comparisons/{id}/related-resources", comparisonId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedResource("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.findAllRelatedResources(comparisonId, any()) }
    }

    @Test
    @DisplayName("Given a comparison related figure, when fetched by id, then status is 200 OK and comparison related figure is returned")
    fun relatedFigureGetSingle() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigure = createComparisonRelatedFigure()

        every {
            comparisonService.findRelatedFigureById(comparisonId, comparisonRelatedFigure.id)
        } returns Optional.of(comparisonRelatedFigure)

        documentedGetRequestTo("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, comparisonRelatedFigure.id)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectComparisonRelatedFigure()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related figure to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the comparison related figure."),
                        fieldWithPath("label").description("The title of label comparison related figure."),
                        fieldWithPath("image").description("The url for the image of the comparison related figure."),
                        fieldWithPath("description").description("The description of the comparison related figure."),
                        timestampFieldWithPath("created_at", "the comparison related figure was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this comparison related figure.")
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

        get("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, comparisonRelatedFigureId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-figures/$comparisonRelatedFigureId"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.findRelatedFigureById(comparisonId, comparisonRelatedFigureId) }
    }

    @Test
    @DisplayName("Given several comparison related figures, when fetched, then status is 200 OK and comparison related figures are returned")
    fun relatedFigureGetPaged() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigure = listOf(createComparisonRelatedFigure())

        every { comparisonService.findAllRelatedFigures(comparisonId, any()) } returns pageOf(comparisonRelatedFigure)

        documentedGetRequestTo("/api/comparisons/{id}/related-figures", comparisonId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectComparisonRelatedFigure("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.findAllRelatedFigures(comparisonId, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison, when publishing, then status 204 NO CONTENT")
    fun publish() {
        val comparisonVersionId = ThingId("R1564")
        val subject = "comparison subject"
        val description = "comparison description"
        val authors = listOf(Author("Author 1"))
        val assignDOI = true
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors,
            "assign_doi" to assignDOI
        )

        every { comparisonService.publish(any()) } returns comparisonVersionId

        documentedPostRequestTo("/api/comparisons/{id}/publish", comparisonVersionId)
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/comparisons/$comparisonVersionId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison to publish.")
                    ),
                    requestFields(
                        fieldWithPath("subject").description("The subject of the comparison."),
                        fieldWithPath("description").description("The description of the comparison."),
                        fieldWithPath("assign_doi").description("Whether to assign a new DOI for the comparison when publishing."),
                    ).and(authorListFields("comparison"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonService.publish(
                withArg {
                    it.id shouldBe comparisonVersionId
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                    it.assignDOI shouldBe assignDOI
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a comparison, when publishing but service reports missing comparison, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val subject = "comparison subject"
        val description = "comparison description"
        val authors = listOf(Author("Author 1"))
        val assignDOI = true
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors,
            "assign_doi" to assignDOI
        )
        val exception = ComparisonNotFound(id)

        every { comparisonService.publish(any()) } throws exception

        post("/api/comparisons/$id/publish")
            .content(request)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$id/publish"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) {
            comparisonService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                    it.assignDOI shouldBe assignDOI
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a comparison, when publishing but service reports doi service unavailable, then status is 503 SERVICE UNAVAILABLE`() {
        val id = ThingId("R123")
        val subject = "comparison subject"
        val description = "comparison description"
        val authors = listOf(Author("Author 1"))
        val assignDOI = true
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors,
            "assign_doi" to assignDOI
        )
        val exception = ServiceUnavailable.create("DOI", 500, "Internal error")

        every { comparisonService.publish(any()) } throws exception

        post("/api/comparisons/$id/publish")
            .content(request)
            .perform()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$id/publish"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) {
            comparisonService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                    it.assignDOI shouldBe assignDOI
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a create comparison request, when service succeeds, it creates and returns the comparison")
    fun create() {
        val id = ThingId("R123")
        every { comparisonService.create(any()) } returns id

        documentedPostRequestTo("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created comparison can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the comparison."),
                        fieldWithPath("description").description("The description of the comparison."),
                        fieldWithPath("research_fields").description("The list of research fields the comparison will be assigned to."),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the comparison will be assigned to. (optional)").optional(),
                        fieldWithPath("contributions[]").description("The ids of the contributions the comparison compares."),
                        fieldWithPath("config").description("The configuration of the comparison."),
                        fieldWithPath("config.predicates").description("The list of labels of the predicates used in the comparison."),
                        fieldWithPath("config.contributions").description("The list of ids of contributions that are being compared in the comparison."),
                        fieldWithPath("config.transpose").description("Whether the comparison table is transposed."),
                        fieldWithPath("config.type").description("The type of method used to create the comparison. Either of ${ComparisonType.entries.joinToString { "\"$it\"" }}."),
                        // The short_codes field exists for compatibility reasons, but should not end up in the documentation, as it allows for arbitrary inputs.
                        fieldWithPath("config.short_codes").description("The list of short form ids for the comparison.").optional().ignored(),
                        fieldWithPath("data").description("The data contained in the comparison."),
                        fieldWithPath("data.contributions").description("The list of contributions that are being compared in the comparison."),
                        fieldWithPath("data.contributions[].id").description("The id of the contribution."),
                        fieldWithPath("data.contributions[].label").description("The label of the contribution."),
                        fieldWithPath("data.contributions[].paper_id").description("The id of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].paper_label").description("The label of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].paper_year").description("The publication year of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].active").description("Whether the contribution (column or row if transposed) should be displayed."),
                        fieldWithPath("data.predicates").description("The list of predicates used in the comparison."),
                        fieldWithPath("data.predicates[].id").description("When the comparison type is \"MERGE\", this is the predicate id. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
                        fieldWithPath("data.predicates[].label").description("When the comparison type is \"MERGE\", this is the label of the predicate. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
                        fieldWithPath("data.predicates[].n_contributions").description("The count of contributions that contain a statements for the predicate."),
                        fieldWithPath("data.predicates[].active").description("Whether the predicate (row or column if transposed) should be displayed."),
                        fieldWithPath("data.predicates[].similar_predicates").description("The list of similar predicate labels."),
                        fieldWithPath("data.data").description("The values of the comparison."),
                        fieldWithPath("data.data.*").description("A map of predicate ids to the values for each contribution."),
                        fieldWithPath("data.data.*[]").description("All values for the predicate in the comparison. This corresponds to a row (or column if transposed) of the comparison."),
                        fieldWithPath("data.data.*[][]").description("All values for the predicate and a single contribution. Every value corresponds to a single cell of the comparison."),
                        fieldWithPath("data.data.*[][].id").description("The id of the orkg entity behind the value."),
                        fieldWithPath("data.data.*[][].label").description("The label of the orkg entity behind the value. This corresponds to the cell value."),
                        fieldWithPath("data.data.*[][].classes").description("The classes of the orkg entity, if it is a resource, empty otherwise."),
                        fieldWithPath("data.data.*[][].path").description("The predicate path (ids) of the value within the contribution."),
                        fieldWithPath("data.data.*[][].path_labels").description("The corresponding predicate labels of the predicate path."),
                        fieldWithPath("data.data.*[][]._class").description("The type of the orkg entity behind the value. Either of \"class\", \"resource\", \"predicate\" or \"literal\"."),
                        fieldWithPath("references[]").description("The references to external sources that the comparison refers to."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the comparison belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the comparison belongs to."),
                        fieldWithPath("is_anonymized").description("Whether or not the comparison should be displayed as anonymous."),
                        fieldWithPath("extraction_method").description("""The method used to extract the comparison resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC".""")
                    ).and(authorListFields("comparison"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports too few contributions, then status is 400 BAD REQUEST`() {
        val exception = RequiresAtLeastTwoContributions()
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports contribution not found, then status is 404 NOT FOUND`() {
        val exception = ContributionNotFound(ThingId("R123"))
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports only one research field allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneResearchFieldAllowed()
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports research field is missing, then status is 404 NOT FOUND`() {
        val exception = ResearchFieldNotFound(ThingId("R123"))
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneOrganizationAllowed()
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneObservatoryAllowed()
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports author not found, then status is 404 NOT FOUND`() {
        val exception = AuthorNotFound(ThingId("R123"))
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a create comparison request, when service reports ambiguous author, then status is 400 BAD REQUEST`() {
        val exception = AmbiguousAuthor(
            Author(
                id = ThingId("R123"),
                name = "author",
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
            )
        )
        every { comparisonService.create(any()) } throws exception

        post("/api/comparisons")
            .content(createComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.create(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related resource request, when service succeeds, it creates and returns the comparison related resource")
    fun createComparisonRelatedResourceTest() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonService.createComparisonRelatedResource(any()) } returns id

        documentedPostRequestTo("/api/comparisons/{id}/related-resources", comparisonId)
            .content(createComparisonRelatedResourceRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created comparison related resource can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The comparison to attach the comparison related resource to.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related resource."),
                        fieldWithPath("image").description("The url to the image of the comparison related resource. (optional)"),
                        fieldWithPath("url").description("The url of the comparison related resource. (optional)"),
                        fieldWithPath("description").description("The description of the comparison related resource. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.createComparisonRelatedResource(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a comparison related resource request, when service reports missing comparison, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R100")
        val exception = ComparisonNotFound(comparisonId)
        every { comparisonService.createComparisonRelatedResource(any()) } throws exception

        post("/api/comparisons/$comparisonId/related-resources")
            .content(createComparisonRelatedResourceRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-resources"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.createComparisonRelatedResource(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related figure request, when service succeeds, it creates and returns the comparison related figure")
    fun createComparisonRelatedFigureTest() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonService.createComparisonRelatedFigure(any()) } returns id

        documentedPostRequestTo("/api/comparisons/{id}/related-figures", comparisonId)
            .content(createComparisonRelatedFigureRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-figures/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created comparison related figure can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The comparison to attach the comparison related figure to.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related figure."),
                        fieldWithPath("image").description("The url to the image of the comparison related figure. (optional)"),
                        fieldWithPath("description").description("The description of the comparison related figure. (optional)")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.createComparisonRelatedFigure(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a comparison related figure request, when service reports missing comparison, then status is 404 NOT FOUND`() {
        val comparisonId = ThingId("R100")
        val exception = ComparisonNotFound(comparisonId)
        every { comparisonService.createComparisonRelatedFigure(any()) } throws exception

        post("/api/comparisons/$comparisonId/related-figures")
            .content(createComparisonRelatedFigureRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/comparisons/$comparisonId/related-figures"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { comparisonService.createComparisonRelatedFigure(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a update comparison request, when service succeeds, it updates the comparison")
    fun update() {
        val id = ThingId("R123")
        every { comparisonService.update(any()) } just runs

        documentedPutRequestTo("/api/comparisons/{id}", id)
            .content(updateComparisonRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the comparison. (optional)"),
                        fieldWithPath("description").description("The description of the comparison. (optional)"),
                        fieldWithPath("research_fields").description("The list of research fields the comparison will be assigned to. (optional)"),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the comparison will be assigned to. (optional)").optional(),
                        fieldWithPath("contributions[]").description("The ids of the contributions the comparison compares. (optional)"),
                        fieldWithPath("config").description("The configuration of the comparison. (optional)"),
                        fieldWithPath("config.predicates").description("The list of labels of the predicates used in the comparison."),
                        fieldWithPath("config.contributions").description("The list of ids of contributions that are being compared in the comparison."),
                        fieldWithPath("config.transpose").description("Whether the comparison table is transposed."),
                        fieldWithPath("config.type").description("The type of method used to create the comparison. Either of ${ComparisonType.entries.joinToString { "\"$it\"" }}."),
                        // The short_codes field exists for compatibility reasons, but should not end up in the documentation, as it allows for arbitrary inputs.
                        fieldWithPath("config.short_codes").description("The list of short form ids for the comparison.").optional().ignored(),
                        fieldWithPath("data").description("The data contained in the comparison."),
                        fieldWithPath("data.contributions").description("The list of contributions that are being compared in the comparison."),
                        fieldWithPath("data.contributions[].id").description("The id of the contribution."),
                        fieldWithPath("data.contributions[].label").description("The label of the contribution."),
                        fieldWithPath("data.contributions[].paper_id").description("The id of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].paper_label").description("The label of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].paper_year").description("The publication year of the paper the contribution belongs to."),
                        fieldWithPath("data.contributions[].active").description("Whether the contribution (column or row if transposed) should be displayed."),
                        fieldWithPath("data.predicates").description("The list of predicates used in the comparison."),
                        fieldWithPath("data.predicates[].id").description("When the comparison type is \"MERGE\", this is the predicate id. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
                        fieldWithPath("data.predicates[].label").description("When the comparison type is \"MERGE\", this is the label of the predicate. When the comparison type is \"PATH\", this is a '/' delimited list of predicate labels, indicating the path from the contribution resource."),
                        fieldWithPath("data.predicates[].n_contributions").description("The count of contributions that contain a statements for the predicate."),
                        fieldWithPath("data.predicates[].active").description("Whether the predicate (row or column if transposed) should be displayed."),
                        fieldWithPath("data.predicates[].similar_predicates").description("The list of similar predicate labels."),
                        fieldWithPath("data.data").description("The values of the comparison. (optional)"),
                        fieldWithPath("data.data.*").description("A map of predicate ids to the values for each contribution."),
                        fieldWithPath("data.data.*[]").description("All values for the predicate in the comparison. This corresponds to a row (or column if transposed) of the comparison."),
                        fieldWithPath("data.data.*[][]").description("All values for the predicate and a single contribution. Every value corresponds to a single cell of the comparison."),
                        fieldWithPath("data.data.*[][].id").description("The id of the orkg entity behind the value."),
                        fieldWithPath("data.data.*[][].label").description("The label of the orkg entity behind the value. This corresponds to the cell value."),
                        fieldWithPath("data.data.*[][].classes").description("The classes of the orkg entity, if it is a resource, empty otherwise."),
                        fieldWithPath("data.data.*[][].path").description("The predicate path (ids) of the value within the contribution."),
                        fieldWithPath("data.data.*[][].path_labels").description("The corresponding predicate labels of the predicate path."),
                        fieldWithPath("data.data.*[][]._class").description("The type of the orkg entity behind the value. Either of \"class\", \"resource\", \"predicate\" or \"literal\"."),
                        fieldWithPath("references[]").description("The references to external sources that the comparison refers to. (optional)"),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations or conference series the comparison belongs to. (optional)"),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the comparison belongs to. (optional)"),
                        fieldWithPath("is_anonymized").description("Whether or not the comparison should be displayed as anonymous. (optional)"),
                        fieldWithPath("extraction_method").description("""The method used to extract the comparison resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (optional)""")
                    ).and(authorListFields("comparison"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related resource request, when service succeeds, it updates the comparison related resource")
    fun updateComparisonRelatedResource() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonService.updateComparisonRelatedResource(any()) } just runs

        documentedPutRequestTo("/api/comparisons/{id}/related-resources/{comparisonRelatedFigureId}", comparisonId, id)
            .content(updateComparisonRelatedResourceRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-resources/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison related resource can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The id of the comparison the comparison related resource belongs to."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related resource to update.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related resource. (optional)"),
                        fieldWithPath("image").description("The url to the image of the comparison related resource."),
                        fieldWithPath("url").description("The url of the comparison related resource."),
                        fieldWithPath("description").description("The description of the comparison related resource.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.updateComparisonRelatedResource(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related figure request, when service succeeds, it updates the comparison related figure")
    fun updateComparisonRelatedFigure() {
        val id = ThingId("R123")
        val comparisonId = ThingId("R100")
        every { comparisonService.updateComparisonRelatedFigure(any()) } just runs

        documentedPutRequestTo("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, id)
            .content(updateComparisonRelatedFigureRequest())
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId/related-figures/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated comparison related figure can be fetched from.")
                    ),
                    pathParameters(
                        parameterWithName("id").description("The id of the comparison the comparison related figure belongs to."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related figure to update.")
                    ),
                    requestFields(
                        fieldWithPath("label").description("The label of the comparison related figure. (optional)"),
                        fieldWithPath("image").description("The url to the image of the comparison related figure."),
                        fieldWithPath("description").description("The description of the comparison related figure.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { comparisonService.updateComparisonRelatedFigure(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related resource, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun relatedResourceDelete() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedResourceId = ThingId("R456")

        every {
            comparisonService.deleteComparisonRelatedResource(
                comparisonId = comparisonId,
                comparisonRelatedResourceId = comparisonRelatedResourceId,
                contributorId = any()
            )
        } just runs

        documentedDeleteRequestTo("/api/comparisons/{id}/related-resources/{comparisonRelatedResourceId}", comparisonId, comparisonRelatedResourceId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedResourceId").description("The identifier of the comparison related resource to delete.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonService.deleteComparisonRelatedResource(
                comparisonId = comparisonId,
                comparisonRelatedResourceId = comparisonRelatedResourceId,
                contributorId = ContributorId(MockUserId.USER)
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a comparison related figure, when deleting and service succeeds, then status is 204 NO CONTENT")
    fun relatedFigureDelete() {
        val comparisonId = ThingId("R123")
        val comparisonRelatedFigureId = ThingId("R456")

        every {
            comparisonService.deleteComparisonRelatedFigure(
                comparisonId = comparisonId,
                comparisonRelatedFigureId = comparisonRelatedFigureId,
                contributorId = any()
            )
        } just runs

        documentedDeleteRequestTo("/api/comparisons/{id}/related-figures/{comparisonRelatedFigureId}", comparisonId, comparisonRelatedFigureId)
            .accept(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/comparisons/$comparisonId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the comparison."),
                        parameterWithName("comparisonRelatedFigureId").description("The identifier of the comparison related figure to delete.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            comparisonService.deleteComparisonRelatedFigure(
                comparisonId = comparisonId,
                comparisonRelatedFigureId = comparisonRelatedFigureId,
                contributorId = ContributorId(MockUserId.USER)
            )
        }
    }

    private fun createComparisonRequest() =
        ComparisonController.CreateComparisonRequest(
            title = "test",
            description = "comparison description",
            researchFields = listOf(ThingId("R12")),
            authors = listOf(
                AuthorDTO(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI("https://example.org/author")
                ),
                AuthorDTO(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            sustainableDevelopmentGoals = setOf(
                ThingId("SDG_1"),
                ThingId("SDG_2")
            ),
            contributions = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120")),
            config = createComparisonConfig(),
            data = createComparisonData(),
            references = listOf("https://orkg.org/resources/R1000", "paper citation"),
            observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
            organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
            isAnonymized = false,
            extractionMethod = ExtractionMethod.UNKNOWN
        )

    private fun updateComparisonRequest() =
        ComparisonController.UpdateComparisonRequest(
            title = "test",
            description = "comparison description",
            researchFields = listOf(ThingId("R12")),
            authors = listOf(
                AuthorDTO(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI("https://example.org/author")
                ),
                AuthorDTO(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            sustainableDevelopmentGoals = setOf(
                ThingId("SDG_1"),
                ThingId("SDG_2")
            ),
            contributions = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120")),
            config = createComparisonConfig(),
            data = createComparisonData(),
            references = listOf("https://orkg.org/resources/R1000", "paper citation"),
            observatories = listOf(ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")),
            organizations = listOf(OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")),
            isAnonymized = false,
            extractionMethod = ExtractionMethod.UNKNOWN
        )

    private fun createComparisonRelatedResourceRequest() =
        ComparisonController.CreateComparisonRelatedResourceRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )

    private fun updateComparisonRelatedResourceRequest() =
        ComparisonController.UpdateComparisonRelatedResourceRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description"
        )

    private fun createComparisonRelatedFigureRequest() =
        ComparisonController.CreateComparisonRelatedFigureRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            description = "comparison related resource description"
        )

    private fun updateComparisonRelatedFigureRequest() =
        ComparisonController.UpdateComparisonRelatedFigureRequest(
            label = "related resource",
            image = "https://example.org/test.png",
            description = "comparison related resource description"
        )
}
