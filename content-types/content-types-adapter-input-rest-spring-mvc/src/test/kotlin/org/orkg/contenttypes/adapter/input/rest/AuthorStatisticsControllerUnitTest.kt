package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.testing.fixtures.createAuthorRecord
import org.orkg.contenttypes.input.AuthorStatisticsUseCases
import org.orkg.contenttypes.input.testing.fixtures.authorRecordResponseFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.testing.andExpectContributionRecord
import org.orkg.testing.andExpectPage
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@ContextConfiguration(classes = [AuthorStatisticsController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [AuthorStatisticsController::class])
internal class AuthorStatisticsControllerUnitTest : MockMvcBaseTest("author-statistics") {
    @MockkBean
    private lateinit var authorStatisticsUseCases: AuthorStatisticsUseCases

    @Test
    @DisplayName("Given several contributions in the graph, when fetching authors by research problem id, then status is 200 OK and author records are returned")
    fun findAllByResearchProblemId() {
        val authorRecord = createAuthorRecord()
        every { authorStatisticsUseCases.findAllByResearchProblemId(any(), any(), any(), any()) } returns pageOf(authorRecord)

        val researchProblemId = ThingId("R123")
        val after = OffsetDateTime.now(fixedClock).minusHours(1)
        val before = OffsetDateTime.now(fixedClock).plusHours(1)

        documentedGetRequestTo("/api/research-problems/{id}/author-statistics", researchProblemId)
            .param("after", after.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("before", before.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributionRecord("$.content[*]")
            .andDocument {
                summary("Listing author statistics by research problem")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of author records for the given research problem.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("after").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created after the specified date. (optional)").optional(),
                    parameterWithName("before").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created before the specified date. (optional)").optional(),
                )
                pagedResponseFields<AuthorRecordRepresentation>(authorRecordResponseFields())
            }

        verify(exactly = 1) {
            authorStatisticsUseCases.findAllByResearchProblemId(
                pageable = any(),
                researchProblem = researchProblemId,
                after = after,
                before = before,
            )
        }
    }
}
