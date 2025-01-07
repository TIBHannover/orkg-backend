package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.asciidoc.legacyVisibilityFilterRequestParameters
import org.orkg.graph.testing.asciidoc.visibilityFilterRequestParameter
import org.orkg.graph.testing.fixtures.createComparisonResource
import org.orkg.graph.testing.fixtures.createPaperResource
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createVisualizationResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.toAsciidoc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// TODO: Move to a different place
val supportedClasses = setOf(
    "Paper",
    "Comparison",
    "Visualization",
    "LiteratureListPublished",
    "Problem",
    "SmartReviewPublished",
).sorted().toAsciidoc()

@ContextConfiguration(classes = [ResearchFieldController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class, WebMvcConfiguration::class])
@WebMvcTest(controllers = [ResearchFieldController::class])
internal class ResearchFieldControllerUnitTest : RestDocsTest("research-fields") {
    @MockkBean
    private lateinit var useCases: RetrieveResearchFieldUseCase

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var flags: FeatureFlagService

    @MockkBean
    private lateinit var comparisonRepository: ComparisonRepository

    @Test
    fun getPaged() {
        val id = ThingId("RF1234")
        val paper = createPaperResource()
        val visualization = createVisualizationResource()
        every {
            useCases.findAllEntitiesBasedOnClassesByResearchField(id, any(), any(), any(), any())
        } returns pageOf(listOf(paper, visualization))
        every {
            // The returned counts returned do not matter, we only need to satisfy the call.
            statementService.countIncomingStatements(setOf(paper.id, visualization.id))
        } returns mapOf(paper.id to 12, visualization.id to 3)

        documentedGetRequestTo("/api/research-fields/{id}", id)
            .param("classes", "Paper", "Visualization")
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[0]") // only test the first element, this should be fine
            .andDo(
                documentationHandler.document(
                    pathParameters(parameterWithName("id").description("The ID of the research field.")),
                    queryParameters(
                        parameterWithName("classes").description("A list of classes to filter against. The classes must support research fields. Must be one of $supportedClasses."),
                        *legacyVisibilityFilterRequestParameters(),
                        visibilityFilterRequestParameter(),
                    ),
                    responseFields(
                        subsectionWithPath("content").description("A (sorted) array of content type <<resource-representation,resources>>."),
                        *ignorePageableFieldsExceptContent(),
                    ),
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { useCases.findAllEntitiesBasedOnClassesByResearchField(id, any(), any(), any(), any()) }
        verify(exactly = 1) { statementService.countIncomingStatements(any<Set<ThingId>>()) }
    }

    @Test
    fun getSubfieldsPaged() {
        val id = ThingId("RF1234")
        val paper = createPaperResource()
        val visualization = createVisualizationResource()
        val mockedResult = listOf(paper, visualization)
        every {
            useCases.findAllEntitiesBasedOnClassesByResearchField(id, any(), any(), any(), any())
        } returns pageOf(mockedResult)
        every {
            // The returned counts returned do not matter, we only need to satisfy the call.
            statementService.countIncomingStatements(setOf(paper.id, visualization.id))
        } returns mapOf(paper.id to 12, visualization.id to 3)

        get("/api/research-fields/{id}/subfields", id)
            .param("classes", "Paper", "Visualization")
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[0]") // only test the first element, this should be fine

        verify(exactly = 1) { useCases.findAllEntitiesBasedOnClassesByResearchField(id, any(), any(), any(), any()) }
        verify(exactly = 1) { statementService.countIncomingStatements(any<Set<ThingId>>()) }
    }

    @Test
    fun getProblemsPerField() {
        val id = ThingId("RF1234")
        val fieldResource = createResource(id, classes = setOf(Classes.researchField), label = "Fancy research")
        val problemResource = createResource(ThingId("RP234"), classes = setOf(Classes.problem))

        every { resourceService.findById(id) } returns Optional.of(fieldResource)
        every { statementService.countIncomingStatements(setOf(problemResource.id)) } returns mapOf(id to 4)
        every { useCases.findAllResearchProblemsByResearchField(id, any(), any(), any()) } returns pageOf(problemResource)

        documentedGetRequestTo("/api/research-fields/{id}/research-problems", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        subsectionWithPath("content").description("A (sorted) array of problems resources."),
                        *ignorePageableFieldsExceptContent(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(id) }
        verify(exactly = 1) { statementService.countIncomingStatements(setOf(problemResource.id)) }
        verify(exactly = 1) { useCases.findAllResearchProblemsByResearchField(id, any(), any(), any()) }
    }

    @Test
    fun getPapersPerField() {
        val id = ThingId("RF1234")
        val fieldResource = createResource(id, classes = setOf(Classes.researchField), label = "Fancy research")
        val paper1 = createPaperResource(ThingId("P1"), title = "Some interesting title")
        val paper2 = createPaperResource(ThingId("P2"), title = "Even more interesting title")

        every { resourceService.findById(fieldResource.id) } returns Optional.of(fieldResource)
        every { statementService.countIncomingStatements(setOf(paper1.id, paper2.id)) } returns mapOf(paper1.id to 12, paper2.id to 13)
        every { useCases.findAllPapersByResearchField(fieldResource.id, any(), any(), any()) } returns pageOf(paper1, paper2)

        documentedGetRequestTo("/api/research-fields/{id}/papers", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        subsectionWithPath("content").description("A (sorted) array of paper resources."),
                        *ignorePageableFieldsExceptContent(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(id) }
        verify(exactly = 1) { statementService.countIncomingStatements(any<Set<ThingId>>()) }
        verify(exactly = 1) { useCases.findAllPapersByResearchField(id, any(), any(), any()) }
    }

    @Test
    fun getComparisonsPerField() {
        val id = ThingId("RF1234")
        val fieldResource = createResource(id, classes = setOf(Classes.researchField), label = "Fancy research")
        val comparison1 = createComparisonResource(ThingId("P1"))
        val comparison2 = createComparisonResource(ThingId("P2"))
        every { resourceService.findById(id) } returns Optional.of(fieldResource)
        every {
            statementService.countIncomingStatements(setOf(comparison1.id, comparison2.id))
        } returns mapOf(comparison1.id to 12, comparison2.id to 13)
        every {
            comparisonRepository.findAll(
                researchField = id,
                visibility = any(),
                includeSubfields = false,
                pageable = any()
            )
        } returns pageOf(comparison1, comparison2)

        documentedGetRequestTo("/api/research-fields/{id}/comparisons", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        subsectionWithPath("content").description("A (sorted) array of comparison resources."),
                        *ignorePageableFieldsExceptContent(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(id) }
        verify(exactly = 1) { statementService.countIncomingStatements(setOf(comparison1.id, comparison2.id)) }
        verify(exactly = 1) {
            comparisonRepository.findAll(
                researchField = id,
                visibility = any(),
                includeSubfields = false,
                pageable = any()
            )
        }
    }

    @Test
    fun getContributorsPerField() {
        val id = ThingId("RF1234")
        val fieldResource = createResource(id, classes = setOf(Classes.researchField), label = "Fancy research")
        val contributor1 = createContributor(
            id = ContributorId(UUID.fromString("3a6b2e25-5890-44cd-b6e7-16137f8b9c6a")),
            name = "Some One"
        )
        val contributor2 = createContributor(
            id = ContributorId(UUID.fromString("fd4a1478-ce49-4e8e-b04a-39a8cac9e33f")),
            name = "Another One"
        )
        every { resourceService.findById(id) } returns Optional.of(fieldResource)
        every { useCases.getContributorsExcludingSubFields(id, any()) } returns pageOf(contributor1, contributor2)

        documentedGetRequestTo("/api/research-fields/{id}/contributors", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        subsectionWithPath("content").description("A (sorted) array of contributors."), // TODO: link to contributors
                        *ignorePageableFieldsExceptContent(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(id) }
        verify(exactly = 1) { useCases.getContributorsExcludingSubFields(id, any()) }
    }
}
