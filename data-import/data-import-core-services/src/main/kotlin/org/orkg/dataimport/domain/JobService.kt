package org.orkg.dataimport.domain

import org.orkg.common.ContributorId
import org.orkg.community.output.ContributorRepository
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.jobs.result.JobResultFactory
import org.orkg.dataimport.domain.jobs.status.JobStatusFactory
import org.orkg.dataimport.input.JobUseCases
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException
import org.springframework.batch.core.launch.JobExecutionNotRunningException
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.batch.core.repository.JobRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class JobService(
    private val jobRepository: JobRepository,
    private val jobOperator: JobOperator,
    private val jobStatusFactory: JobStatusFactory,
    private val jobResultFactory: JobResultFactory,
    private val contributorRepository: ContributorRepository,
    jobs: List<Job>,
) : JobUseCases {
    private val jobsByName = jobs.associateBy { it.name }

    override fun runJob(jobName: String, jobParameters: JobParameters): JobId {
        val job = jobsByName[jobName] ?: throw IllegalArgumentException("""Job "$jobName" not found.""")
        try {
            val jobExecution = jobOperator.start(job, jobParameters)
            return JobId(jobExecution.id)
        } catch (_: JobExecutionAlreadyRunningException) {
            throw JobAlreadyRunning(findJobIdByNameAndParameters(jobName, jobParameters)!!)
        } catch (e: JobRestartException) {
            throw JobRestartFailed(findJobIdByNameAndParameters(jobName, jobParameters)!!, e)
        } catch (_: JobInstanceAlreadyCompleteException) {
            throw JobAlreadyComplete(findJobIdByNameAndParameters(jobName, jobParameters)!!)
        }
    }

    override fun findJobStatusById(jobId: JobId, contributorId: ContributorId): Optional<JobStatus> =
        Optional.of(jobStatusFactory.format(findJobExecutionById(jobId, contributorId)))

    override fun findJobResultById(
        jobId: JobId,
        contributorId: ContributorId,
        pageable: Pageable,
    ): Optional<JobResult> {
        val jobExecution = findJobExecutionById(jobId, contributorId)
        val status = JobStatus.Status.fromJobExecution(jobExecution)
        if (status != Status.DONE && status != Status.FAILED) {
            return Optional.empty()
        }
        val result = JobResult(
            jobId = jobId,
            jobName = jobExecution.jobInstance.jobName,
            status = status,
            value = jobResultFactory.getResult(jobExecution, status, pageable),
        )
        return Optional.of(result)
    }

    override fun stopJob(jobId: JobId, contributorId: ContributorId) {
        val jobExecution = findJobExecutionById(jobId, contributorId)
        if (!jobExecution.isRunning) {
            throw JobNotRunning(jobId)
        }
        try {
            jobOperator.stop(jobExecution)
        } catch (_: JobExecutionNotRunningException) {
            throw JobNotRunning(jobId)
        }
    }

    private fun findJobExecutionById(jobId: JobId, contributorId: ContributorId): JobExecution {
        val jobExecution = jobRepository.getJobExecution(jobId.value)
        if (jobExecution == null || (extractContributorId(jobExecution) != contributorId && !contributorRepository.isAdmin(contributorId))) {
            throw JobNotFound(jobId)
        }
        return jobExecution
    }

    private fun findJobIdByNameAndParameters(jobName: String, jobParameters: JobParameters): JobId? =
        jobRepository.getJobInstance(jobName, jobParameters)?.id?.let(::JobId)
}
