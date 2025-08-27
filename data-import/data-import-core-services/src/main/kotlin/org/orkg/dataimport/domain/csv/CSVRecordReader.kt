package org.orkg.dataimport.domain.csv

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader
import java.io.StringReader

open class CSVRecordReader(
    private val csvId: CSVID,
    private val csvRepository: CSVRepository,
) : AbstractItemCountingItemStreamItemReader<PositionAwareCSVRecord>() {
    private lateinit var csvParser: CSVParser
    private lateinit var iterator: Iterator<CSVRecord>

    override fun doRead(): PositionAwareCSVRecord? {
        if (iterator.hasNext()) {
            return PositionAwareCSVRecord(
                record = iterator.next(),
                itemNumber = currentItemCount.toLong(),
                lineNumber = csvParser.currentLineNumber,
            )
        }
        return null
    }

    override fun doOpen() {
        val csv = csvRepository.findById(csvId).orElseThrow { CSVNotFound(csvId) }
        name = csvId.toString()
        csvParser = csv.format.csvFormat.parse(StringReader(csv.data))
        iterator = csvParser.iterator()
        // skip header
        if (iterator.hasNext()) {
            iterator.next()
        }
    }

    override fun doClose() {
        csvParser.close()
    }
}
