package org.orkg.dataimport.domain.csv

import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.stereotype.Component

@Component
open class TypedCSVRecordDeleter(
    private val csvId: CSVID,
    private val typedCSVRecordRepository: TypedCSVRecordRepository,
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        typedCSVRecordRepository.deleteAllByCSVID(csvId)
        return RepeatStatus.FINISHED
    }
}
