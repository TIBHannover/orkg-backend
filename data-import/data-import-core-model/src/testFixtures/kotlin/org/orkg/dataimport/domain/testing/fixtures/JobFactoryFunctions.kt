package org.orkg.dataimport.domain.testing.fixtures

import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.step.StepExecution
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

fun createJobExecution(
    id: Long = 123,
    instance: JobInstance = createJobInstance(),
    jobParameters: JobParameters = JobParametersBuilder().toJobParameters(),
) = JobExecution(id, instance, jobParameters)

fun createJobInstance(
    id: Long = 123,
    name: String = "test-job",
) = JobInstance(id, name)

fun createStepExecution(
    id: Long = 123,
    name: String = "test-step-execution",
    jobExecution: JobExecution = createJobExecution(),
) = StepExecution(id, name, jobExecution)
