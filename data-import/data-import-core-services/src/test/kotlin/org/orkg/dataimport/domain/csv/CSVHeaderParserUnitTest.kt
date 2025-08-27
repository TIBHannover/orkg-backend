package org.orkg.dataimport.domain.csv

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVHeaders
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.scope.context.StepContext
import org.springframework.batch.repeat.RepeatStatus
import java.util.Optional

internal class CSVHeaderParserUnitTest : MockkBaseTest {
    private val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
    private val csvRepository: CSVRepository = mockk()

    private val csvHeaderParser = CSVHeaderParser(csvId, csvRepository)

    @Test
    fun `Given a csv id, when parsing the headers of the csv, it saves the headers to the job execution context`() {
        val csv = createCSV()
        val stepExecution = StepExecution("test", JobExecution(123))
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))

        every { csvRepository.findById(csvId) } returns Optional.of(csv)

        csvHeaderParser.execute(contribution, chunkContext) shouldBe RepeatStatus.FINISHED

        contribution.stepExecution.jobExecution.executionContext.get(CSV_HEADERS_FIELD) shouldBe createPaperCSVHeaders()

        verify(exactly = 1) { csvRepository.findById(csvId) }
    }

    @Test
    fun `Given a csv id, when parsing the headers of the csv, and csv does not exist, it throws an exception`() {
        val stepExecution = StepExecution("test", JobExecution(123))
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))

        every { csvRepository.findById(csvId) } returns Optional.empty()

        shouldThrow<CSVNotFound> { csvHeaderParser.execute(contribution, chunkContext) }

        verify(exactly = 1) { csvRepository.findById(csvId) }
    }
}
