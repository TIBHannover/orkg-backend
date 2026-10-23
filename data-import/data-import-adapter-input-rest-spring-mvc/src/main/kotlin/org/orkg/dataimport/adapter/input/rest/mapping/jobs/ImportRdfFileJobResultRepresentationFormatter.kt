package org.orkg.dataimport.adapter.input.rest.mapping.jobs

import org.orkg.dataimport.adapter.input.rest.RdfFileImportResultRepresentation
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFormatter
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.rdf.RdfFileImportResult
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ImportRdfFileJobResultRepresentationFormatter : JobResultRepresentationFormatter {
    override fun getRepresentation(jobResult: JobResult): Optional<Any> {
        if (jobResult.status == Status.DONE || jobResult.status == Status.STOPPED) {
            return jobResult.value.map { result -> (result as RdfFileImportResult).toRdfFileImportResultRepresentation() }
        }
        return super.getRepresentation(jobResult)
    }

    private fun RdfFileImportResult.toRdfFileImportResultRepresentation(): RdfFileImportResultRepresentation =
        RdfFileImportResultRepresentation(classCount, predicateCount, resourceCount, statementCount)

    override fun jobNames(): Set<String> = setOf(JobNames.IMPORT_RDF_FILE)
}
