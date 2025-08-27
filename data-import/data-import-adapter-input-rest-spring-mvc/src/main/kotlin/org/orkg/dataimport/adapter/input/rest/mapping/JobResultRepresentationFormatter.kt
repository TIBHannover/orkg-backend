package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.domain.jobs.JobResult
import java.util.Optional

interface JobResultRepresentationFormatter {
    fun getRepresentation(jobResult: JobResult): Optional<Any> = jobResult.value

    fun jobNames(): Set<String>

    companion object {
        val DEFAULT = object : JobResultRepresentationFormatter {
            override fun jobNames(): Set<String> = emptySet()
        }
    }
}
