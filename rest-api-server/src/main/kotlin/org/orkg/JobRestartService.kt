package org.orkg

import org.slf4j.LoggerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class JobRestartService(
    private val jobExplorer: JobExplorer,
    private val jobOperator: JobOperator,
    private val jobRepository: JobRepository,
    private val jobs: List<Job>,
    private val clock: Clock,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments?) {
        jobs.forEach { job ->
            if (!job.isRestartable) {
                return@forEach
            }
            jobExplorer.findRunningJobExecutions(job.name).forEach { jobExecution ->
                if (jobExecution.isRunning) {
                    logger.info("""Restarting job execution {}.""", jobExecution)
                    try {
                        jobOperator.stop(jobExecution.id)
                    } catch (e: Throwable) {
                        logger.error("Failed to stop job {}.", jobExecution, e)
                    }
                    jobExecution.stepExecutions.lastOrNull()?.apply {
                        status = BatchStatus.FAILED
                        endTime = LocalDateTime.now(clock)
                    }
                    jobExecution.status = BatchStatus.FAILED
                    jobExecution.endTime = LocalDateTime.now(clock)
                    jobRepository.update(jobExecution)
                    try {
                        jobOperator.restart(jobExecution.id)
                    } catch (e: Throwable) {
                        logger.error("Failed to restart job {}.", jobExecution, e)
                    }
                }
            }
        }
    }
}
