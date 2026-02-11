package org.orkg.dataimport.domain.csv

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.scope.context.StepContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus

internal class TypedCSVRecordDeleterUnitTest : MockkBaseTest {
    private val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
    private val typedCSVRecordRepository: TypedCSVRecordRepository = mockk()

    private val typedCSVRecordDeleter = TypedCSVRecordDeleter(csvId, typedCSVRecordRepository)

    @Test
    fun `Given a csv id, it deletes all typed csv records`() {
        val stepExecution = createStepExecution()
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))

        every { typedCSVRecordRepository.deleteAllByCSVID(csvId) } just runs

        typedCSVRecordDeleter.execute(contribution, chunkContext) shouldBe RepeatStatus.FINISHED

        verify(exactly = 1) { typedCSVRecordRepository.deleteAllByCSVID(csvId) }
    }
}
