package org.orkg.dataimport.domain.csv

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.apache.commons.csv.CSVRecord
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.CSV_TYPE_FIELD
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.testing.fixtures.createCSVRecord
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVHeaders
import org.orkg.dataimport.domain.testing.fixtures.createTypedCSVRecord
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.StepExecution
import java.util.UUID

internal class TypedCSVRecordParserUnitTest : MockkBaseTest {
    private val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")

    private val typedCSVRecordParser = TypedCSVRecordParser(csvId)

    @Test
    fun `Given a csv id, when parsing position aware csv records, it returns typed csv records`() {
        val id = UUID.fromString("e7de95a8-d1f5-4837-9a1f-a2eb8b45a254")
        val headers = createPaperCSVHeaders()
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, CSV.Type.PAPER).toJobParameters()
        val jobExecution = JobExecution(123, jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
        }
        val stepExecution = StepExecution("test", jobExecution)
        // Mock CSVRecord, because its constructor is package private
        val csvRecord = mockk<CSVRecord>()
        val record = PositionAwareCSVRecord(
            itemNumber = 1,
            lineNumber = 2,
            record = csvRecord,
        )
        val expected = createTypedCSVRecord()

        mockkStatic(UUID::class) {
            every { UUID.randomUUID() } returns id
            every { csvRecord.recordNumber } returns 2
            every { csvRecord.toList() } returns createCSVRecord()

            typedCSVRecordParser.beforeStep(stepExecution)
            typedCSVRecordParser.process(record) shouldBe expected

            verify(exactly = 1) { UUID.randomUUID() }
            verify(exactly = 1) { csvRecord.recordNumber }
            verify(exactly = 1) { csvRecord.toList() }
        }
    }
}
