package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.contenttypes.domain.testing.asciidoc.allowedContentTypeClassValues
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReview
import org.orkg.contenttypes.domain.testing.fixtures.createTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createVisualization
import org.orkg.contenttypes.input.ContentTypeUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.allowedVisibilityFilterValues
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectComparison
import org.orkg.testing.andExpectLiteratureList
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.andExpectResource
import org.orkg.testing.andExpectSmartReview
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.andExpectVisualization
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@ContextConfiguration(
    classes = [
        ContentTypeController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class,
    ]
)
@WebMvcTest(controllers = [ContentTypeController::class])
internal class ContentTypeControllerUnitTest : MockMvcBaseTest("content-types") {
    @MockkBean
    private lateinit var contentTypeService: ContentTypeUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Test
    @DisplayName("Given several content types, when they are fetched, then status is 200 OK and content types are returned")
    fun getPaged() {
        every {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(
            createPaper(),
            createComparison(),
            createVisualization(),
            createTemplate(),
            createLiteratureList(),
            createSmartReview()
        )

        documentedGetRequestTo("/api/content-types")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPaper("$.content[0]")
            .andExpectComparison("$.content[1]")
            .andExpectVisualization("$.content[2]")
            .andExpectTemplate("$.content[3]")
            .andExpectLiteratureList("$.content[4]")
            .andExpectSmartReview("$.content[5]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several content types, when filtering by several parameters, then status is 200 OK and content types are returned")
    fun getPagedWithParameters() {
        every {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(
            createPaper(),
            createComparison(),
            createVisualization(),
            createTemplate(),
            createLiteratureList(),
            createSmartReview()
        )

        val classes = ContentTypeClass.entries.toSet()
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val sdg = ThingId("SDG_1")
        val authorId = ThingId("147")

        documentedGetRequestTo("/api/content-types")
            .param("classes", classes.joinToString(","))
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("sdg", sdg.value)
            .param("author_id", authorId.value)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPaper("$.content[0]")
            .andExpectComparison("$.content[1]")
            .andExpectVisualization("$.content[2]")
            .andExpectTemplate("$.content[3]")
            .andExpectLiteratureList("$.content[4]")
            .andExpectSmartReview("$.content[5]")
            .andDo(
                documentationHandler.document(
                    queryParametersFindAll()
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            contentTypeService.findAll(
                pageable = any(),
                classes = classes,
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                sustainableDevelopmentGoal = sdg,
                authorId = authorId,
                authorName = null
            )
        }
    }

    @Test
    fun `Given several content types, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        get("/api/content-types")
            .param("sort", "unknown")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Given several content types, when using incompatible filtering parameters, then status is 400 BAD REQUEST`() {
        get("/api/content-types")
            .param("author_id", "R123")
            .param("author_name", "Author")
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
    }

    @Test
    @DisplayName("Given several content types, when they are fetched as resources, then status is 200 OK and content type resources are returned")
    fun getPagedAsResource() {
        every {
            contentTypeService.findAllAsResource(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(
            createResource(ThingId("R1"), classes = setOf(Classes.paper)),
            createResource(ThingId("R2"), classes = setOf(Classes.comparison)),
            createResource(ThingId("R3"), classes = setOf(Classes.visualization)),
            createResource(ThingId("R4"), classes = setOf(Classes.nodeShape)),
            createResource(ThingId("R5"), classes = setOf(Classes.literatureList)),
            createResource(ThingId("R6"), classes = setOf(Classes.smartReview)),
        )
        every { statementService.countAllIncomingStatementsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/content-types")
            .accept(RESOURCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[0]") // only test first element, because jsonPath().value() aggregates fields to lists
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            contentTypeService.findAllAsResource(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any()) }
    }

    @Test
    @DisplayName("Given several content types, when filtering by several parameters as resources, then status is 200 OK and content type resources are returned")
    fun getPagedAsResourceWithParameters() {
        every {
            contentTypeService.findAllAsResource(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(
            createResource(ThingId("R1"), classes = setOf(Classes.paper)),
            createResource(ThingId("R2"), classes = setOf(Classes.comparison)),
            createResource(ThingId("R3"), classes = setOf(Classes.visualization)),
            createResource(ThingId("R4"), classes = setOf(Classes.nodeShape)),
            createResource(ThingId("R5"), classes = setOf(Classes.literatureList)),
            createResource(ThingId("R6"), classes = setOf(Classes.smartReview)),
        )
        every { statementService.countAllIncomingStatementsById(any()) } returns emptyMap()

        val classes = ContentTypeClass.entries.toSet()
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val sdg = ThingId("SDG_1")
        val authorId = ThingId("147")

        documentedGetRequestTo("/api/content-types")
            .param("classes", classes.joinToString(","))
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("sdg", sdg.value)
            .param("author_id", authorId.value)
            .accept(RESOURCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[0]") // only test first element, because jsonPath().value() aggregates fields to lists
            .andDo(
                documentationHandler.document(
                    queryParametersFindAll()
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            contentTypeService.findAllAsResource(
                pageable = any(),
                classes = classes,
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                sustainableDevelopmentGoal = sdg,
                authorId = authorId,
                authorName = null
            )
        }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any()) }
    }

    @Test
    fun `Given several content types, when using incompatible filtering parameters as resource, then status is 400 BAD REQUEST`() {
        get("/api/content-types")
            .param("author_id", "R123")
            .param("author_name", "Author")
            .accept(RESOURCE_JSON_V1)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
    }

    private fun queryParametersFindAll() =
        queryParameters(
            parameterWithName("classes").description("Filter for the content type classes. Available classes are $allowedContentTypeClassValues (optional)"),
            parameterWithName("visibility").description("Filter for visibility. Either of $allowedVisibilityFilterValues. (optional)"),
            parameterWithName("created_by").description("Filter for the UUID of the user or service who created this content type. (optional)"),
            parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned content type can have. (optional)"),
            parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned content type can have. (optional)"),
            parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the content type belongs to. (optional)"),
            parameterWithName("organization_id").description("Filter for the UUID of the organization that the content type belongs to. (optional)"),
            parameterWithName("research_field").description("Filter for research field id that the content type belongs to. (optional)"),
            parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
            parameterWithName("sdg").description("Filter for the sustainable development goal that the content type belongs to. (optional)"),
            parameterWithName("author_id").description("Filter for the author of the content type. Cannot be used in combination with `author_name`. (optional)"),
            parameterWithName("author_name").description("Filter for the name of the author of the content type. Cannot be used in combination with `author_id`. (optional)").optional(),
        )
}
