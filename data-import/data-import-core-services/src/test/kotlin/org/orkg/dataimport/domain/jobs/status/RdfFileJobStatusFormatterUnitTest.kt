package org.orkg.dataimport.domain.jobs.status

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.RDF_FILE_ID_FIELD
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import java.util.UUID

internal class RdfFileJobStatusFormatterUnitTest : MockkBaseTest {
    private val rdfFileJobStatusFormatter = RdfFileJobStatusFormatter()

    @Test
    fun `Given a job execution, when formatting job status context, it returns the correct result`() {
        val rdfFileId = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFileId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)
        rdfFileJobStatusFormatter.getContext(jobExecution) shouldBe mapOf("rdf_file_id" to rdfFileId)
    }
}
