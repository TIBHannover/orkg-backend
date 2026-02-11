package org.orkg.dataimport.input

import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.data.domain.Pageable
import java.util.Optional

interface JobUseCases {
    fun runJob(jobName: String, jobParameters: JobParameters): JobId

    fun findJobStatusById(jobId: JobId, contributorId: ContributorId): Optional<JobStatus>

    fun findJobResultById(jobId: JobId, contributorId: ContributorId, pageable: Pageable): Optional<JobResult>

    fun stopJob(jobId: JobId, contributorId: ContributorId)
}
