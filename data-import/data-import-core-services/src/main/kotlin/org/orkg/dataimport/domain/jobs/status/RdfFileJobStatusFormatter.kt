package org.orkg.dataimport.domain.jobs.status

import org.orkg.dataimport.domain.extractRdfFileId
import org.orkg.dataimport.domain.jobs.JobNames
import org.springframework.batch.core.job.JobExecution
import org.springframework.stereotype.Component

@Component
class RdfFileJobStatusFormatter : JobStatusFormatter {
    override fun getContext(jobExecution: JobExecution): Map<String, Any?> =
        mapOf("rdf_file_id" to extractRdfFileId(jobExecution))

    override fun jobNames(): Set<String> = setOf(JobNames.VALIDATE_RDF_FILE, JobNames.IMPORT_RDF_FILE)
}
