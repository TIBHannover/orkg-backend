package org.orkg.dataimport.adapter.input.rest.mapping.jobs

import org.orkg.dataimport.adapter.input.rest.ResourceToTypeMappingRepresentation
import org.orkg.dataimport.adapter.input.rest.mapping.JobResultRepresentationFormatter
import org.orkg.dataimport.domain.jobs.JobNames
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ValidateRdfFileJobResultRepresentationFormatter : JobResultRepresentationFormatter {
    override fun getRepresentation(jobResult: JobResult): Optional<Any> {
        if (jobResult.status == Status.DONE || jobResult.status == Status.STOPPED) {
            return jobResult.value.map { page ->
                (page as Page<*>).map { element ->
                    (element as ResourceToTypeMapping).toResourceToTypeMappingRepresentation()
                }
            }
        }
        return super.getRepresentation(jobResult)
    }

    private fun ResourceToTypeMapping.toResourceToTypeMappingRepresentation(): ResourceToTypeMappingRepresentation =
        ResourceToTypeMappingRepresentation(iri, type)

    override fun jobNames(): Set<String> = setOf(JobNames.VALIDATE_RDF_FILE)
}
