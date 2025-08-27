package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.adapter.input.rest.JobStatusRepresentation
import org.orkg.dataimport.domain.jobs.JobStatus
import java.util.Optional

interface JobStatusRepresentationAdapter {
    fun Optional<JobStatus>.mapToJobStatusRepresentation(): Optional<JobStatusRepresentation> =
        map { it.toJobStatusRepresentation() }

    fun JobStatus.toJobStatusRepresentation(): JobStatusRepresentation =
        JobStatusRepresentation(jobId, jobName, context, status)
}
