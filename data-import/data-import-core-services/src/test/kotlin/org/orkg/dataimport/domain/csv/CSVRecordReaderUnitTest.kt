package org.orkg.dataimport.domain.csv

import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.domain.testing.fixtures.createCSVRecord
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.item.ExecutionContext
import java.util.Optional

internal class CSVRecordReaderUnitTest : MockkBaseTest {
    private val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
    private val csvRepository: CSVRepository = mockk()

    private val csvRecordReader = CSVRecordReader(csvId, csvRepository)

    @Test
    fun `Given a csv id, when reading records of the csv, it returns the correct result`() {
        val csv = createCSV()

        every { csvRepository.findById(csvId) } returns Optional.of(csv)

        csvRecordReader.open(ExecutionContext())
        csvRecordReader.read().asClue {
            it.shouldNotBeNull()
            it.shouldBeInstanceOf<PositionAwareCSVRecord>()
            it.itemNumber shouldBe 1
            it.lineNumber shouldBe 2
            it.record.values() shouldBe createCSVRecord().toTypedArray()
            it.record.recordNumber shouldBe 2
        }
        csvRecordReader.close()

        verify(exactly = 1) { csvRepository.findById(csvId) }
    }
}
