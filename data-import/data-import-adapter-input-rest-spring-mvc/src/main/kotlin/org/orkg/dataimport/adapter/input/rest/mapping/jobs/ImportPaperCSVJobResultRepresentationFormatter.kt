package org.orkg.dataimport.adapter.input.rest.mapping.jobs

import org.orkg.dataimport.adapter.input.rest.PaperCSVRecordImportResultRepresentation
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFormatter
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ImportPaperCSVJobResultRepresentationFormatter : JobResultRepresentationFormatter {
    override fun getRepresentation(jobResult: JobResult): Optional<Any> {
        if (jobResult.status == Status.DONE || jobResult.status == Status.STOPPED) {
            return jobResult.value.map { page ->
                (page as Page<*>).map { element ->
                    (element as PaperCSVRecordImportResult).toPaperCSVRecordImportResultRepresentation()
                }
            }
        }
        return super.getRepresentation(jobResult)
    }

    private fun PaperCSVRecordImportResult.toPaperCSVRecordImportResultRepresentation(): PaperCSVRecordImportResultRepresentation =
        PaperCSVRecordImportResultRepresentation(id, importedEntityId, importedEntityType, csvId, itemNumber, lineNumber)

    override fun jobNames(): Set<String> = setOf(JobNames.IMPORT_PAPER_CSV)
}
