package org.orkg.dataimport.domain.jobs.status

import org.orkg.dataimport.domain.extractCSVID
import org.orkg.dataimport.domain.jobs.JobNames
import org.springframework.batch.core.job.JobExecution
import org.springframework.stereotype.Component

@Component
class PaperCSVJobStatusFormatter : JobStatusFormatter {
    override fun getContext(jobExecution: JobExecution): Map<String, Any?> =
        mapOf("csv_id" to extractCSVID(jobExecution))

    override fun jobNames(): Set<String> = setOf(JobNames.IMPORT_PAPER_CSV, JobNames.VALIDATE_PAPER_CSV)
}
