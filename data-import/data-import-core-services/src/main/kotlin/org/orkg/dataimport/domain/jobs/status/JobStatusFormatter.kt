package org.orkg.dataimport.domain.jobs.status

import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.springframework.batch.core.job.JobExecution

interface JobStatusFormatter {
    fun getContext(jobExecution: JobExecution): Map<String, Any?> = emptyMap()

    fun getStatus(jobExecution: JobExecution): Status =
        Status.fromJobExecution(jobExecution)

    fun jobNames(): Set<String>

    companion object {
        val DEFAULT = object : JobStatusFormatter {
            override fun jobNames(): Set<String> = emptySet()
        }
    }
}
