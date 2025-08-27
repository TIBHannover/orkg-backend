package org.orkg.dataimport.domain.jobs.status

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.CSV_ID_FIELD
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.csv.CSVID
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder

internal class PaperCSVJobStatusFormatterUnitTest : MockkBaseTest {
    private val paperCSVJobStatusFormatter = PaperCSVJobStatusFormatter()

    @Test
    fun `Given a job execution, when formatting job status context, it returns the correct result`() {
        val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csvId).toJobParameters()
        val jobExecution = JobExecution(123, jobParameters)
        paperCSVJobStatusFormatter.getContext(jobExecution) shouldBe mapOf("csv_id" to csvId)
    }
}
