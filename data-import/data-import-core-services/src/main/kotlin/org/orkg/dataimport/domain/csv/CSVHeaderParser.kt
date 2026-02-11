package org.orkg.dataimport.domain.csv

import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.internal.SchemaBasedCSVRecordParser
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import java.io.StringReader

/**
 * Reads the first line of a CSV, parses the headers, and saves them to the job execution context.
 */
open class CSVHeaderParser(
    private val csvId: CSVID,
    private val csvRepository: CSVRepository,
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val csv = csvRepository.findById(csvId).orElseThrow { CSVNotFound(csvId) }
        val csvType = csv.type
        val csvFormat = csv.format
        val headers = StringReader(csv.data).use { reader ->
            val csvParser = csvFormat.csvFormat.parse(reader)
            try {
                SchemaBasedCSVRecordParser(csvType.schema).parseHeader(csvParser.first())
            } catch (e: SimpleMessageException) {
                e.body.setProperty("csv_id", csvId)
                throw e
            }
        }
        val jobExecutionContext = contribution.stepExecution.jobExecution.executionContext
        jobExecutionContext.put(CSV_HEADERS_FIELD, headers)
        return RepeatStatus.FINISHED
    }
}
