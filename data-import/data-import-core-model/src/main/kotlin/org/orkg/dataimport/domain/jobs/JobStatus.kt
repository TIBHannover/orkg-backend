package org.orkg.dataimport.domain.jobs

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution

data class JobStatus(
    val jobId: JobId,
    val jobName: String,
    val context: Map<String, Any?>,
    val status: Status,
) {
    enum class Status {
        PENDING,
        RUNNING,
        STOPPED,
        FAILED,
        DONE,
        UNKNOWN,
        ;

        companion object {
            fun fromJobExecution(jobExecution: JobExecution): Status =
                when (jobExecution.status) {
                    BatchStatus.COMPLETED -> Status.DONE
                    BatchStatus.STARTING -> Status.PENDING
                    BatchStatus.STARTED, BatchStatus.STOPPING -> Status.RUNNING
                    BatchStatus.STOPPED -> Status.STOPPED
                    BatchStatus.FAILED, BatchStatus.ABANDONED -> Status.FAILED
                    else -> Status.UNKNOWN
                }
        }
    }
}
