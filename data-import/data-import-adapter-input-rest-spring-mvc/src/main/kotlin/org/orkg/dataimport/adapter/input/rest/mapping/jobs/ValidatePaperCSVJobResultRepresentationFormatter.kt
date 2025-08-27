package org.orkg.dataimport.adapter.input.rest.mapping.jobs

import org.orkg.contenttypes.adapter.input.rest.mapping.AuthorRepresentationAdapter
import org.orkg.dataimport.adapter.input.rest.PaperCSVRecordRepresentation
import org.orkg.dataimport.adapter.input.rest.mapping.ContributionStatementRepresentationAdapter
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFormatter
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ValidatePaperCSVJobResultRepresentationFormatter :
    JobResultRepresentationFormatter,
    AuthorRepresentationAdapter,
    ContributionStatementRepresentationAdapter {
    override fun getRepresentation(jobResult: JobResult): Optional<Any> {
        if (jobResult.status == Status.DONE || jobResult.status == Status.STOPPED) {
            return jobResult.value.map { page ->
                (page as Page<*>).map { element ->
                    (element as PaperCSVRecord).toPaperCSVRecordRepresentation()
                }
            }
        }
        return super.getRepresentation(jobResult)
    }

    private fun PaperCSVRecord.toPaperCSVRecordRepresentation(): PaperCSVRecordRepresentation =
        PaperCSVRecordRepresentation(
            id = id,
            csvId = csvId,
            itemNumber = itemNumber,
            lineNumber = lineNumber,
            title = title,
            authors = authors.mapToAuthorRepresentation(),
            publishedMonth = publicationMonth,
            publishedYear = publicationYear,
            publishedIn = publishedIn,
            url = url,
            doi = doi,
            researchFieldId = researchFieldId,
            extractionMethod = extractionMethod,
            statements = statements.mapToContributionStatementRepresentation()
        )

    override fun jobNames(): Set<String> = setOf(JobNames.VALIDATE_PAPER_CSV)
}
