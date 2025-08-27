package org.orkg.dataimport.domain.jobs

import java.util.Optional

data class JobResult(
    val jobId: JobId,
    val jobName: String,
    val status: JobStatus.Status,
    val value: Optional<Any>,
)
