package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.domain.PaperAuthor
import org.orkg.contenttypes.domain.SimpleAuthor
import org.orkg.contenttypes.input.AuthorUseCases
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.restdocs.ignorePageableFieldsExceptContent
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        ProblemController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class
    ]
)
@WebMvcTest(controllers = [ProblemController::class])
internal class ProblemControllerUnitTest : MockMvcBaseTest("research-problems") {
    @MockkBean
    private lateinit var researchProblemService: ResearchProblemUseCases

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var contributorService: RetrieveContributorUseCase

    @MockkBean
    private lateinit var authorService: AuthorUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Test
    @DisplayName("Given a research problem, when fetching its related users, then status is 200 OK and users are returned")
    fun getUsersPerProblem() {
        val id = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        every { researchProblemService.findAllContributorsPerProblem(id, any()) } returns listOf(ContributorPerProblem(MockUserId.USER, 10))
        every { contributorService.findById(contributorId) } returns Optional.of(createContributor(contributorId))

        documentedGetRequestTo("/api/problems/{id}/users", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research problem.")
                    ),
                    queryParameters(
                        parameterWithName("page").description("The page number to fetch. (optional, default: 0)").optional(),
                        parameterWithName("size").description("The number of items to fetch per page. (optional, default: 10)").optional()
                    ),
                    responseFields(
                        subsectionWithPath("[].user").description("The <<contributor-fetch_response_fields,contributor>> object."),
                        fieldWithPath("[].contributions").description("The number of contributions the user contributed."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { researchProblemService.findAllContributorsPerProblem(id, any()) }
        verify(exactly = 1) { contributorService.findById(contributorId) }
    }

    @Test
    @DisplayName("Given a research problem, when fetching its related authors, then status is 200 OK and authors are returned")
    fun getAuthorsPerProblem() {
        val id = ThingId("R123")

        every { authorService.findAllByProblemId(id, any()) } returns pageOf(
            PaperAuthor(
                author = SimpleAuthor.ResourceAuthor(
                    createResource(label = "Author A", classes = setOf(Classes.author))
                ),
                papers = 2
            ),
            PaperAuthor(
                author = SimpleAuthor.LiteralAuthor("Author B"),
                papers = 5
            )
        )
        every { statementService.countAllIncomingStatementsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/problems/{id}/authors", id)
            .perform()
            .andExpect(status().isOk)
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the research problem.")
                    ),
                    responseFields(
                        fieldWithPath("content[].author").description("The author which is using the research problem"),
                        fieldWithPath("content[].author.value").type("String").description("The name of the author."),
                        subsectionWithPath("content[].author.value").type("Resource").description("The author <<resources-fetch,resource>>."),
                        fieldWithPath("content[].papers").description("The number of papers composed by the author."),
                        *ignorePageableFieldsExceptContent()
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { authorService.findAllByProblemId(id, any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any()) }
    }
}
