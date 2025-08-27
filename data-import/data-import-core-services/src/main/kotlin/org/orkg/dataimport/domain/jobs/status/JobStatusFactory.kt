package org.orkg.dataimport.domain.jobs.status

import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobStatus
import org.springframework.batch.core.JobExecution
import org.springframework.stereotype.Component

@Component
class JobStatusFactory(statusFormatters: List<JobStatusFormatter>) {
    private val jobNameToStatusFormatter =
        statusFormatters.flatMap { formatter -> formatter.jobNames().map { it to formatter } }.toMap()

    fun format(jobExecution: JobExecution): JobStatus {
        val formatter = jobNameToStatusFormatter[jobExecution.jobInstance.jobName]
            ?: JobStatusFormatter.DEFAULT
        return JobStatus(
            jobId = JobId(jobExecution.id),
            jobName = jobExecution.jobInstance.jobName,
            context = formatter.getContext(jobExecution),
            status = formatter.getStatus(jobExecution)
        )
    }
}
