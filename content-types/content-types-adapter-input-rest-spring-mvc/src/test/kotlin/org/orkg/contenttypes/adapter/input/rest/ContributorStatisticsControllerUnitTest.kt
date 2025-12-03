package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.contenttypes.domain.testing.fixtures.createContributorRecord
import org.orkg.contenttypes.input.ContributorStatisticsUseCases
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.contributorRecordResponseFields
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

@ContextConfiguration(classes = [ContributorStatisticsController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ContributorStatisticsController::class])
internal class ContributorStatisticsControllerUnitTest : MockMvcBaseTest("contributor-statistics") {
    @MockkBean
    private lateinit var contributorStatisticsUseCases: ContributorStatisticsUseCases

    @Test
    fun `Given several contributions in the graph, when fetching contributor records, then status is 200 OK and contributor records are returned`() {
        every {
            contributorStatisticsUseCases.findAll(any(), any(), any())
        } returns pageOf(createContributorRecord())

        documentedGetRequestTo("/api/contributor-statistics")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributionRecord("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            contributorStatisticsUseCases.findAll(any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several contributions in the graph, when fetching contributor with several filters, then status is 200 OK and contributor records are returned")
    fun findAll() {
        val contributorRecord = createContributorRecord()
        every { contributorStatisticsUseCases.findAll(any(), any(), any()) } returns pageOf(contributorRecord)

        val after = OffsetDateTime.now(fixedClock).minusHours(1)
        val before = OffsetDateTime.now(fixedClock).plusHours(1)

        documentedGetRequestTo("/api/contributor-statistics")
            .param("after", after.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("before", before.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributionRecord("$.content[*]")
            .andDocument {
                summary("Listing contributor statistics")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of contribution records.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("after").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created after the specified date. (optional)").optional(),
                    parameterWithName("before").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created before the specified date. (optional)").optional(),
                )
                pagedResponseFields<ContributorRecordRepresentation>(contributorRecordResponseFields())
            }

        verify(exactly = 1) {
            contributorStatisticsUseCases.findAll(
                pageable = any(),
                after = after,
                before = before,
            )
        }
    }

    @Test
    @DisplayName("Given several contributions in the graph, when fetching contributor with several filters, then status is 200 OK and contributor records are returned")
    fun findAllByResearchFieldId() {
        val contributorRecord = createContributorRecord()
        every { contributorStatisticsUseCases.findAllByResearchFieldId(any(), any(), any(), any(), any()) } returns pageOf(contributorRecord)

        val researchFieldId = ThingId("R123")
        val after = OffsetDateTime.now(fixedClock).minusHours(1)
        val before = OffsetDateTime.now(fixedClock).plusHours(1)
        val includeSubfields = true

        documentedGetRequestTo("/api/research-fields/{id}/contributor-statistics", researchFieldId)
            .param("after", after.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("before", before.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("include_subfields", includeSubfields.toString())
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributionRecord("$.content[*]")
            .andDocument {
                summary("Listing contributor statistics by research field")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of contribution records for the given research field.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)").optional(),
                    parameterWithName("after").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created after the specified date. (optional)").optional(),
                    parameterWithName("before").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created before the specified date. (optional)").optional(),
                )
                pagedResponseFields<ContributorRecordRepresentation>(contributorRecordResponseFields())
            }

        verify(exactly = 1) {
            contributorStatisticsUseCases.findAllByResearchFieldId(
                pageable = any(),
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                after = after,
                before = before,
            )
        }
    }

    @Test
    @DisplayName("Given several contributions in the graph, when fetching contributor with several filters, then status is 200 OK and contributor records are returned")
    fun findAllByResearchProblemId() {
        val contributorRecord = createContributorRecord()
        every { contributorStatisticsUseCases.findAllByResearchProblemId(any(), any(), any(), any(), any()) } returns pageOf(contributorRecord)

        val researchProblemId = ThingId("R123")
        val after = OffsetDateTime.now(fixedClock).minusHours(1)
        val before = OffsetDateTime.now(fixedClock).plusHours(1)
        val includeSubproblems = true

        documentedGetRequestTo("/api/research-problems/{id}/contributor-statistics", researchProblemId)
            .param("include_subproblems", includeSubproblems.toString())
            .param("after", after.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("before", before.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectContributionRecord("$.content[*]")
            .andDocument {
                summary("Listing contributor statistics by research problem")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of contribution records for the given research problem.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("include_subproblems").description("Flag for whether subproblems are included in the search or not. (optional, default: false)").optional(),
                    parameterWithName("after").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created after the specified date. (optional)").optional(),
                    parameterWithName("before").description("Filter for the 'created at' timestamp, limiting exploration to resources that were created before the specified date. (optional)").optional(),
                )
                pagedResponseFields<ContributorRecordRepresentation>(contributorRecordResponseFields())
            }

        verify(exactly = 1) {
            contributorStatisticsUseCases.findAllByResearchProblemId(
                pageable = any(),
                researchProblem = researchProblemId,
                includeSubproblems = includeSubproblems,
                after = after,
                before = before,
            )
        }
    }
}
