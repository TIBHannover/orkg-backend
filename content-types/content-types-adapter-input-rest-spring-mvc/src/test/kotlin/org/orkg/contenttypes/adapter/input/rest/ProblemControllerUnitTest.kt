package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.community.testing.fixtures.contributorResponseFields
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.contenttypes.domain.PaperAuthor
import org.orkg.contenttypes.domain.SimpleAuthor
import org.orkg.contenttypes.input.AuthorUseCases
import org.orkg.contenttypes.input.LegacyResearchProblemUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.graph.adapter.input.rest.PaperAuthorRepresentation
import org.orkg.graph.adapter.input.rest.testing.fixtures.resourceResponseFields
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.MockUserId
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.applyPathPrefix
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(classes = [LegacyProblemController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [LegacyProblemController::class])
internal class ProblemControllerUnitTest : MockMvcBaseTest("research-problems") {
    @MockkBean
    private lateinit var researchProblemService: LegacyResearchProblemUseCases

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
    fun findAllContributorsByResearchProblemId() {
        val id = ThingId("R123")
        val contributorId = ContributorId(MockUserId.USER)

        every { researchProblemService.findAllContributorsPerProblem(id, any()) } returns listOf(ContributorPerProblem(MockUserId.USER, 10))
        every { contributorService.findById(contributorId) } returns Optional.of(createContributor(contributorId))

        documentedGetRequestTo("/api/problems/{id}/users", id)
            .perform()
            .andExpect(status().isOk)
            .andDocument {
                summary("Listing contributors of research problems")
                description(
                    """
                    A `GET` request to get a <<sorting-and-pagination,paged>> list of <<contributor-fetch,contributors>> that contributed to contributions where a problem is being addressed.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the research problem."),
                )
                pagedQueryParameters()
                listResponseFields<ContributorWithContributionCountRepresentation>(
                    fieldWithPath("user").description("The <<contributor-fetch_response_fields,contributor>> object."),
                    *applyPathPrefix("user.", contributorResponseFields()).toTypedArray(),
                    fieldWithPath("contributions").description("The number of contributions the user contributed."),
                )
            }

        verify(exactly = 1) { researchProblemService.findAllContributorsPerProblem(id, any()) }
        verify(exactly = 1) { contributorService.findById(contributorId) }
    }

    @Test
    @DisplayName("Given a research problem, when fetching its related authors, then status is 200 OK and authors are returned")
    fun findAllAuthorsByResearchProblemId() {
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
            .andDocument {
                summary("Listing paper authors of research problems")
                description(
                    """
                    A `GET` request provides a <<sorting-and-pagination,paged>> list of authors that have papers addressing a certain research problem.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the research problem."),
                )
                pagedQueryParameters()
                pagedResponseFields<PaperAuthorRepresentation>(
                    fieldWithPath("author").description("The author which is using the research problem"),
                    fieldWithPath("author.value").type("String").description("The name of the author."),
                    *applyPathPrefix("author.value.", resourceResponseFields()).toTypedArray(),
                    fieldWithPath("papers").description("The number of papers composed by the author.")
                )
            }

        verify(exactly = 1) { authorService.findAllByProblemId(id, any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any()) }
    }
}
