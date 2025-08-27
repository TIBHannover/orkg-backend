package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.domain.JobResultNotFound
import org.orkg.dataimport.domain.jobs.JobResult
import org.springframework.stereotype.Component

@Component
class JobResultRepresentationFactory(jobResultAdapters: List<JobResultRepresentationFormatter>) {
    private val jobNameToResultReprensentationAdapter =
        jobResultAdapters.flatMap { formatter -> formatter.jobNames().map { it to formatter } }.toMap()

    fun getResultRepresentation(result: JobResult): Any? {
        val representationAdapter = jobNameToResultReprensentationAdapter[result.jobName]
            ?: JobResultRepresentationFormatter.DEFAULT
        return representationAdapter.getRepresentation(result).orElseThrow { JobResultNotFound(result.jobId) }
    }
}
