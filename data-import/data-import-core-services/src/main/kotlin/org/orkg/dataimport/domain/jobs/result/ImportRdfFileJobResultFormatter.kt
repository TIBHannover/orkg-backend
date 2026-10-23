package org.orkg.dataimport.domain.jobs.result

import org.orkg.common.ThingId
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_PREDICATE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_RESOURCE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_STATEMENT_IDS
import org.orkg.dataimport.domain.getAndCast
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.rdf.RdfFileImportResult
import org.orkg.graph.domain.StatementId
import org.springframework.batch.core.job.JobExecution
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.util.Optional

@Component
class ImportRdfFileJobResultFormatter : JobResultFormatter {
    override fun getResult(jobExecution: JobExecution, status: Status, pageable: Pageable, objectMapper: ObjectMapper): Optional<Any> {
        if (status == Status.DONE) {
            return Optional.of(
                RdfFileImportResult(
                    jobExecution.executionContext.getAndCast<Map<String, ThingId>>(RDF_CLASS_IRI_TO_ID)!!.size,
                    jobExecution.executionContext.getAndCast<Map<String, ThingId>>(RDF_PREDICATE_IRI_TO_ID)!!.size,
                    jobExecution.executionContext.getAndCast<Map<String, ThingId>>(RDF_RESOURCE_IRI_TO_ID)!!.size,
                    jobExecution.executionContext.getAndCast<Set<StatementId>>(RDF_STATEMENT_IDS)!!.size,
                ),
            )
        }
        return super.getResult(jobExecution, status, pageable, objectMapper)
    }

    override fun jobNames(): Set<String> = setOf(JobNames.IMPORT_RDF_FILE)
}
