package org.orkg.dataimport.domain.jobs.result

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.springframework.batch.core.job.JobExecution
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ValidatePaperCSVJobResultFormatter(
    private val paperCSVRecordRepository: PaperCSVRecordRepository,
) : JobResultFormatter {
    override fun getResult(jobExecution: JobExecution, status: Status, pageable: Pageable, objectMapper: ObjectMapper): Optional<Any> {
        if (status == Status.DONE) {
            return Optional.of(paperCSVRecordRepository.findAllByCSVID(extractCSVID(jobExecution), pageable))
        }
        return super.getResult(jobExecution, status, pageable, objectMapper)
    }

    override fun jobNames(): Set<String> = setOf(JobNames.VALIDATE_PAPER_CSV)
}
