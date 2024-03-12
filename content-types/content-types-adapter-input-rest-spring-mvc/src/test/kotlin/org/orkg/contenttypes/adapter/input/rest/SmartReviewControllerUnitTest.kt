package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import org.orkg.contenttypes.domain.testing.fixtures.createDummySmartReview
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.SmartReviewUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectSmartReview
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [SmartReviewController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [SmartReviewController::class])
@DisplayName("Given a SmartReview controller")
internal class SmartReviewControllerUnitTest : RestDocsTest("smart-reviews") {

    @MockkBean
    private lateinit var smartReviewService: SmartReviewUseCases

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(smartReviewService, contributionService)
    }

    @Test
    @DisplayName("Given a smart review, when it is fetched by id and service succeeds, then status is 200 OK and smart review is returned")
    fun getSingle() {
        val smartReview = createDummySmartReview()
        every { smartReviewService.findById(smartReview.id) } returns Optional.of(smartReview)

        documentedGetRequestTo("/api/smart-reviews/{id}", smartReview.id)
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectSmartReview()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the smart review to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the smart review."),
                        fieldWithPath("title").description("The title of the smart review."),
                        fieldWithPath("research_fields").description("The list of research fields the smart review is assigned to."),
                        fieldWithPath("research_fields[].id").description("The id of the research field."),
                        fieldWithPath("research_fields[].label").description("The label of the research field."),
                        fieldWithPath("versions.head").description("The head version of the smart review."),
                        fieldWithPath("versions.head.id").description("The id of the head version."),
                        fieldWithPath("versions.head.label").description("The label of the head version."),
                        timestampFieldWithPath("versions.head.created_at", "the head version was created"),
                        fieldWithPath("versions.published").description("The list of published versions of the smart review."),
                        fieldWithPath("versions.published[].id").description("The id of the published version."),
                        fieldWithPath("versions.published[].label").description("The label of the published version."),
                        timestampFieldWithPath("versions.published[].created_at", "the published version was created"),
                        fieldWithPath("versions.published[].changelog").description("The changelog of the published version."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the smart review belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the smart review belongs to."),
                        fieldWithPath("extraction_method").description("""The method used to extract the smart review resource. Can be one of "unknown", "manual" or "automatic"."""),
                        timestampFieldWithPath("created_at", "the smart review resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this smart review."),
                        fieldWithPath("visibility").description("""Visibility of the smart review. Can be one of "default", "featured", "unlisted" or "deleted"."""),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this smart review.").optional(),
                        fieldWithPath("published").description("Whether the literature is published or not."),
                        fieldWithPath("sections").description("The list of sections of the smart review."),
                        fieldWithPath("sections[].id").description("The id of the section."),
                        fieldWithPath("sections[].heading").description("The heading of the section.").optional(),
                        fieldWithPath("sections[].type").description("""The type of the section. Either of "text", "comparison", "visualization", "resource", "property" or "ontology"."""),
                        fieldWithPath("sections[].comparison").description("The linked comparison of a comparison section.").optional(),
                        fieldWithPath("sections[].comparison.id").description("The id of the linked comparison.").optional(),
                        fieldWithPath("sections[].comparison.label").description("The label of the linked comparison.").optional(),
                        fieldWithPath("sections[].comparison.classes").description("The classes of the linked comparison.").optional(),
                        fieldWithPath("sections[].visualization").description("The linked visualization of a visualization section.").optional(),
                        fieldWithPath("sections[].visualization.id").description("The id of the linked visualization.").optional(),
                        fieldWithPath("sections[].visualization.label").description("The label of the linked visualization.").optional(),
                        fieldWithPath("sections[].visualization.classes").description("The classes of the linked visualization.").optional(),
                        fieldWithPath("sections[].resource").description("The linked resource of a resource section.").optional(),
                        fieldWithPath("sections[].resource.id").description("The id of the linked resource.").optional(),
                        fieldWithPath("sections[].resource.label").description("The label of the linked resource.").optional(),
                        fieldWithPath("sections[].resource.classes").description("The classes of the linked resource.").optional(),
                        fieldWithPath("sections[].predicate").description("The linked resource of a predicate section.").optional(),
                        fieldWithPath("sections[].predicate.id").description("The id of the linked predicate.").optional(),
                        fieldWithPath("sections[].predicate.label").description("The label of the linked predicate.").optional(),
                        fieldWithPath("sections[].predicate.description").description("The description of the linked predicate.").optional(),
                        fieldWithPath("sections[].entities").description("The entities that should be shown in the ontology section. They can either be a resource or a predicate.").optional(),
                        fieldWithPath("sections[].entities[].id").description("The id of the entity.").optional(),
                        fieldWithPath("sections[].entities[].label").description("The label of the entity.").optional(),
                        fieldWithPath("sections[].entities[].classes").description("The classes of the entity, if the entity is a resource.").optional(),
                        fieldWithPath("sections[].entities[].description").description("The description of the entity, if the entity is a predicate.").optional(),
                        fieldWithPath("sections[].predicates").description("The predicates that should be shown in the ontology section.").optional(),
                        fieldWithPath("sections[].predicates[].id").description("The id of the predicate.").optional(),
                        fieldWithPath("sections[].predicates[].label").description("The label of the predicate.").optional(),
                        fieldWithPath("sections[].predicates[].description").description("The description of the predicate.").optional(),
                        fieldWithPath("sections[].text").description("The text contents of the text section.").optional(),
                        fieldWithPath("sections[].classes").description("The additional classes of the text section.").optional(),
                        fieldWithPath("references").description("The list of bibtex references of the smart review.").optional()
                    ).and(authorListFields("smart review"))
                        .and(sustainableDevelopmentGoalsFields("smart review"))
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { smartReviewService.findById(smartReview.id) }
    }

    @Test
    @DisplayName("Given several smart reviews, when they are fetched, then status is 200 OK and smart reviews are returned")
    fun getPaged() {
        every {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createDummySmartReview())

        documentedGetRequestTo("/api/smart-reviews")
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectSmartReview("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several smart reviews, when filtering by several parameters, then status is 200 OK and smart reviews are returned")
    fun getPagedWithParameters() {
        every {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createDummySmartReview())

        val title = "label"
        val exact = true
        val visibility = VisibilityFilter.ALL_LISTED
        val createdBy = ContributorId(UUID.randomUUID())
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId(UUID.randomUUID())
        val organizationId = OrganizationId(UUID.randomUUID())
        val published = true
        val sdg = ThingId("SDG_1")

        documentedGetRequestTo("/api/smart-reviews")
            .param("title", title)
            .param("exact", exact.toString())
            .param("visibility", visibility.name)
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("published", published.toString())
            .param("sdg", sdg.value)
            .accept(SMART_REVIEW_JSON_V1)
            .contentType(SMART_REVIEW_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectSmartReview("$.content[*]")
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("title").description("A search term that must be contained in the title of the smart review. (optional)"),
                        parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED"."""),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this smart review. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned resource can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned resource can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the resource belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the resource belongs to. (optional)"),
                        parameterWithName("published").description("Filter for the publication status of the smart reviews. (optional)"),
                        parameterWithName("sdg").description("Filter for the sustainable development goal that the smart review belongs to. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            smartReviewService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe title
                },
                visibility = visibility,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                published = published,
                sustainableDevelopmentGoal = sdg
            )
        }
    }

    @Test
    fun `Given several smart reviews, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        mockMvc.perform(get("/api/smart-reviews?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/smart-reviews"))

        verify(exactly = 1) {
            smartReviewService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }
}
