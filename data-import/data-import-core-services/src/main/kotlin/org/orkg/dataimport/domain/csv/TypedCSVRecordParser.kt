package org.orkg.dataimport.domain.csv

import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.CSV_TYPE_FIELD
import org.orkg.dataimport.domain.get
import org.orkg.dataimport.domain.getAndCast
import org.orkg.dataimport.domain.internal.SchemaBasedCSVRecordParser
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor
import java.util.UUID

/**
 * Parses a csv record to a typed csv record
 */
open class TypedCSVRecordParser(
    private val csvId: CSVID,
) : ItemProcessor<PositionAwareCSVRecord, TypedCSVRecord>,
    StepExecutionListener {
    private lateinit var parser: SchemaBasedCSVRecordParser
    private lateinit var headers: List<CSVHeader>

    @BeforeStep
    override fun beforeStep(stepExecution: StepExecution) {
        headers = stepExecution.jobExecution.executionContext.getAndCast(CSV_HEADERS_FIELD)!!
        parser = SchemaBasedCSVRecordParser(stepExecution.jobParameters.get<CSV.Type>(CSV_TYPE_FIELD).schema)
    }

    override fun process(item: PositionAwareCSVRecord): TypedCSVRecord? =
        TypedCSVRecord(
            id = UUID.randomUUID(),
            csvId = csvId,
            lineNumber = item.lineNumber,
            itemNumber = item.itemNumber,
            values = try {
                parser.parseRecord(item.record, headers)
            } catch (e: SimpleMessageException) {
                e.body.setProperty("csv_id", csvId)
                throw e
            }
        )
}
