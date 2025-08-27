package org.orkg.dataimport.domain.testing.fixtures

import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus
import java.util.Optional

fun createJobStatus(
    jobId: JobId = JobId(123),
    name: String = "example-job",
    context: Map<String, Any?> = mapOf("some_value" to "could be anything"),
    status: JobStatus.Status = JobStatus.Status.DONE,
) = JobStatus(jobId, name, context, status)

fun createJobResult(
    jobId: JobId = JobId(123),
    jobName: String = "example-job",
    status: JobStatus.Status = JobStatus.Status.DONE,
    value: Optional<Any> = Optional.of("could be anything"),
) = JobResult(jobId, jobName, status, value)
