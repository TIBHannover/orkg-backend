package org.orkg.dataimport.domain.jobs.result

import com.fasterxml.jackson.databind.ObjectMapper
import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.springframework.batch.core.JobExecution
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ImportPaperCSVJobResultFormatter(
    private val paperCSVRecordImportResultRepository: PaperCSVRecordImportResultRepository,
) : JobResultFormatter {
    override fun getResult(jobExecution: JobExecution, status: Status, pageable: Pageable, objectMapper: ObjectMapper): Optional<Any> {
        if (status == Status.DONE) {
            return Optional.of(paperCSVRecordImportResultRepository.findAllByCSVID(extractCSVID(jobExecution), pageable))
        }
        return super.getResult(jobExecution, status, pageable, objectMapper)
    }

    override fun jobNames(): Set<String> = setOf(JobNames.IMPORT_PAPER_CSV)
}
