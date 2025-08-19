package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional
import java.util.UUID

@ContextConfiguration(
    classes = [
        ResearchFieldController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class
    ]
)
@WebMvcTest(controllers = [ResearchFieldController::class])
internal class ResearchFieldControllerUnitTest : MockMvcBaseTest("research-fields") {
    @MockkBean
    private lateinit var useCases: ResearchFieldUseCases

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var comparisonRepository: ComparisonRepository

    @Test
    fun getProblemsPerField() {
        val id = ThingId("RF1234")
        val fieldResource = createResource(id, classes = setOf(Classes.researchField), label = "Fancy research")
        val problemResource = createResource(ThingId("RP234"), classes = setOf(Classes.problem))

        every { resourceService.findById(id) } returns Optional.of(fieldResource)
        every { statementService.countAllIncomingStatementsById(setOf(problemResource.id)) } returns mapOf(id to 4)
        every { useCases.findAllResearchProblemsByResearchField(id, any(), any(), any()) } returns pageOf(problemResource)

        documentedGetRequestTo("/api/research-fields/{id}/research-problems", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research field.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(id) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(setOf(problemResource.id)) }
        verify(exactly = 1) { useCases.findAllResearchProblemsByResearchField(id, any(), any(), any()) }
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
        every { useCases.findAllContributorsExcludingSubFields(id, any()) } returns pageOf(contributor1, contributor2)

        documentedGetRequestTo("/api/research-fields/{id}/contributors", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research field.")
                    ),
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { resourceService.findById(id) }
        verify(exactly = 1) { useCases.findAllContributorsExcludingSubFields(id, any()) }
    }
}
