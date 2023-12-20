package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.asciidoc.legacyVisibilityFilterRequestParameters
import org.orkg.graph.testing.asciidoc.visibilityFilterRequestParameter
import org.orkg.graph.testing.fixtures.createComparisonResource
import org.orkg.graph.testing.fixtures.createPaperResource
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createVisualizationResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResource
import org.orkg.testing.annotations.UsesMocking
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.toAsciidoc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.beneathPath
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
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

@ContextConfiguration(classes = [ResearchFieldController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ResearchFieldController::class])
@UsesMocking
class ResearchFieldControllerUnitTest : RestDocsTest("research-fields") {
    @MockkBean
    private lateinit var useCases: RetrieveResearchFieldUseCase

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @MockkBean
    private lateinit var flags: FeatureFlagService

    @BeforeEach
    fun ignoreFormattedLabels() {
        every { flags.isFormattedLabelsEnabled() } returns false
    }

    @Test
    fun getPaged() {
        val id = ThingId("RF1234")
        `given a research field with a paper and a visualization`(id)

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
                    requestParameters(
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
    }

    @Test
    fun getSubfieldsPaged() {
        val id = ThingId("RF1234")
        `given a research field with subfields containing a paper and a visualization`(id)

        documentedGetRequestTo("/api/research-fields/{id}/subfields", id)
            .param("classes", "Paper", "Visualization")
            .accept(APPLICATION_JSON)
            .contentType(APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[0]") // only test the first element, this should be fine
        // There is no documentation, because everything is exactly the same as without subfields
    }

    @Test
    fun getProblemsPerField() {
        val id = ThingId("RF1234")
        `given a research field with a research problem with several papers`(id)

        documentedGetRequestTo("/api/research-fields/{id}/problems", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    responseFields(
                        beneathPath("content[]").withSubsectionId("papers-per-problem"),
                        *problemsPerFieldResponseFields(),
                    ),
                    responseFields(
                        subsectionWithPath("content").description("A (sorted) array of problems and paper counts (see below)."),
                        *ignorePageableFieldsExceptContent(),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun getPapersPerField() {
        val id = ThingId("RF1234")
        `given a research field with several papers`(id)

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
    }

    @Test
    fun getComparisonsPerField() {
        val id = ThingId("RF1234")
        `given a research field with several comparisons`(id)

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
    }

    @Test
    fun getContributorsPerField() {
        val id = ThingId("RF1234")
        `given a research field with several contributors`(id)

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
    }

    private fun problemsPerFieldResponseFields(): Array<FieldDescriptor> = arrayOf(
        subsectionWithPath("problem").description("A research problem resource, i. e. a <<resources,resource>> of class `Problem`."),
        fieldWithPath("papers").description("The number of papers that address this research problem."),
    )

    private fun `given a research field with a paper and a visualization`(id: ThingId) {
        val paper = createPaperResource()
        val visualization = createVisualizationResource()
        val mockedResult = listOf(paper, visualization)
        every {
            useCases.findAllEntitiesBasedOnClassesByResearchField(id, any(), any(), any(), any())
        } returns pageOf(mockedResult)
        every {
            // The returned counts returned do not matter, we only need to satisfy the call.
            statementService.countStatementsAboutResources(setOf(paper.id, visualization.id))
        } returns mapOf(paper.id to 12, visualization.id to 3)
    }

    private fun `given a research field with subfields containing a paper and a visualization`(id: ThingId) =
        `given a research field with a paper and a visualization`(id)

    private fun `given a research field with a research problem with several papers`(id: ThingId) {
        val fieldResource = createResource(id = id, classes = setOf(Classes.researchField), label = "Fancy research")
        val problemResource = createResource(id = ThingId("RP234"), classes = setOf(Classes.problem))
        val papersPerProblem = PaperCountPerResearchProblem(problemResource, 5)

        every { resourceService.findById(fieldResource.id) } returns Optional.of(fieldResource)
        every { statementService.countStatementsAboutResources(setOf(fieldResource.id)) } returns mapOf(fieldResource.id to 12)
        every { statementService.countStatementsAboutResources(setOf(problemResource.id)) } returns mapOf(fieldResource.id to 4)
        every { useCases.getResearchProblemsOfField(fieldResource.id, any()) } returns pageOf(papersPerProblem)
    }

    private fun `given a research field with several papers`(id: ThingId) {
        val fieldResource = createResource(id = id, classes = setOf(Classes.researchField), label = "Fancy research")
        val paper1 = createPaperResource(id = ThingId("P1"), title = "Some interesting title")
        val paper2 = createPaperResource(id = ThingId("P2"), title = "Even more interesting title")

        every { resourceService.findById(fieldResource.id) } returns Optional.of(fieldResource)
        every {
            statementService.countStatementsAboutResources(setOf(paper1.id, paper2.id))
        } returns mapOf(paper1.id to 12, paper2.id to 13)
        every { useCases.findAllPapersByResearchField(fieldResource.id, any(), any(), any()) } returns pageOf(
            paper1,
            paper2
        )
    }

    private fun `given a research field with several comparisons`(id: ThingId) {
        val fieldResource = createResource(id = id, classes = setOf(Classes.researchField), label = "Fancy research")
        val comparison1 = createComparisonResource(id = ThingId("P1"))
        val comparison2 = createComparisonResource(id = ThingId("P2"))

        every { resourceService.findById(fieldResource.id) } returns Optional.of(fieldResource)
        every {
            statementService.countStatementsAboutResources(setOf(comparison1.id, comparison2.id))
        } returns mapOf(comparison1.id to 12, comparison2.id to 13)
        every { useCases.findAllComparisonsByResearchField(fieldResource.id, any(), any(), any()) } returns pageOf(
            comparison1,
            comparison2
        )
    }

    private fun `given a research field with several contributors`(id: ThingId) {
        val fieldResource = createResource(id = id, classes = setOf(Classes.researchField), label = "Fancy research")
        val contributor1 = createContributor(
            id = ContributorId(UUID.fromString("3a6b2e25-5890-44cd-b6e7-16137f8b9c6a")),
            name = "Some One"
        )
        val contributor2 = createContributor(
            id = ContributorId(UUID.fromString("fd4a1478-ce49-4e8e-b04a-39a8cac9e33f")),
            name = "Another One"
        )

        every { resourceService.findById(fieldResource.id) } returns Optional.of(fieldResource)
        every { useCases.getContributorsExcludingSubFields(fieldResource.id, any()) } returns pageOf(
            contributor1,
            contributor2,
        )
    }
}
