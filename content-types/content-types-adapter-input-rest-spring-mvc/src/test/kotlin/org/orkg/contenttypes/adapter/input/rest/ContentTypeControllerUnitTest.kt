package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.contenttypes.domain.testing.fixtures.createDummyComparison
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.domain.testing.fixtures.createDummyTemplate
import org.orkg.contenttypes.domain.testing.fixtures.createDummyVisualization
import org.orkg.contenttypes.input.ContentTypeUseCases
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectComparison
import org.orkg.testing.andExpectLiteratureList
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.andExpectSmartReview
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.andExpectVisualization
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ContentTypeController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ContentTypeController::class])
internal class ContentTypeControllerUnitTest : RestDocsTest("content-types") {

    @MockkBean
    private lateinit var contentTypeService: ContentTypeUseCases

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(contentTypeService)
    }

    @Test
    @DisplayName("Given several content types, when they are fetched, then status is 200 OK and content types are returned")
    fun getPaged() {
        every {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(
            createDummyPaper(),
            createDummyComparison(),
            createDummyVisualization(),
            createDummyTemplate(),
            createDummyLiteratureList(),
            createDummySmartReview()
        )

        documentedGetRequestTo("/api/content-types")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
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
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several content types, when filtering by several parameters, then status is 200 OK and content types are returned")
    fun getPagedWithParameters() {
        every {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(
            createDummyPaper(),
            createDummyComparison(),
            createDummyVisualization(),
            createDummyTemplate(),
            createDummyLiteratureList(),
            createDummySmartReview()
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
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
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
                    queryParameters(
                        parameterWithName("classes").description("Filter for the content type classes. Available classes are ${ContentTypeClass.entries.joinToString(",") { "\"$it\"" }} (optional)"),
                        parameterWithName("visibility").description("Filter for visibility. Either of ${VisibilityFilter.entries.joinToString(",") { "\"$it\"" }}. (optional)"),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this content type. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned content type can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned content type can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the content type belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the content type belongs to. (optional)"),
                        parameterWithName("research_field").description("Filter for research field id that the content type belongs to. (optional)"),
                        parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
                        parameterWithName("sdg").description("Filter for the sustainable development goal that the content type belongs to. (optional)"),
                        parameterWithName("author_id").description("Filter for the author of the content type. (optional)"),
                    )
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
                authorId = authorId
            )
        }
    }

    @Test
    fun `Given several content types, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        mockMvc.perform(get("/api/content-types?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/content-types"))

        verify(exactly = 1) {
            contentTypeService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }
}
