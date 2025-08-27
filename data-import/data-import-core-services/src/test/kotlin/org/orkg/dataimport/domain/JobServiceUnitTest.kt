package org.orkg.dataimport.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.community.output.ContributorRepository
import org.orkg.community.testing.fixtures.createContributor
import org.orkg.dataimport.domain.jobs.JobId
import org.orkg.dataimport.domain.jobs.JobResult
import org.orkg.dataimport.domain.jobs.JobStatus.Status
import org.orkg.dataimport.domain.jobs.result.JobResultFactory
import org.orkg.dataimport.domain.jobs.status.JobStatusFactory
import org.orkg.dataimport.domain.testing.fixtures.createJobStatus
import org.orkg.testing.MockUserId
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.job.SimpleJob
import org.springframework.batch.core.launch.JobExecutionNotRunningException
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.repository.JobRestartException
import org.springframework.data.domain.PageRequest
import java.util.Optional

internal class JobServiceUnitTest : MockkBaseTest {
    private val jobExplorer: JobExplorer = mockk()
    private val jobLauncher: JobLauncher = mockk()
    private val jobOperator: JobOperator = mockk()
    private val jobStatusFactory: JobStatusFactory = mockk()
    private val jobResultFactory: JobResultFactory = mockk()
    private val contributorRepository: ContributorRepository = mockk()
    private val testJob: Job = SimpleJob("test-job")

    private val service = JobService(
        jobExplorer,
        jobLauncher,
        jobOperator,
        jobStatusFactory,
        jobResultFactory,
        contributorRepository,
        listOf(testJob),
    )

    @Test
    fun `Given a job name and job parameters, when starting a new job, it returns success`() {
        val parameters = JobParametersBuilder().add("parameter", "testing123").toJobParameters()

        every { jobLauncher.run(testJob, parameters) } returns JobExecution(123, parameters)

        service.runJob(testJob.name, parameters) shouldBe JobId(123)

        verify(exactly = 1) { jobLauncher.run(testJob, parameters) }
    }

    @Test
    fun `Given a job name and job parameters, when starting a new job, but job does not exist, it throws an exception`() {
        val parameters = JobParametersBuilder().add("parameter", "testing123").toJobParameters()

        shouldThrow<IllegalArgumentException> { service.runJob("unknown-job", parameters) }
    }

    @Test
    fun `Given a job name and job parameters, when starting a new job, but job launcher signals job is already running, it throws an exception`() {
        val parameters = JobParametersBuilder().add("parameter", "testing123").toJobParameters()

        every { jobLauncher.run(testJob, parameters) } throws JobExecutionAlreadyRunningException("Job already running")
        every { jobExplorer.getJobInstance(testJob.name, parameters) } returns JobInstance(123, testJob.name)

        shouldThrow<JobAlreadyRunning> { service.runJob(testJob.name, parameters) }

        verify(exactly = 1) { jobLauncher.run(testJob, parameters) }
        verify(exactly = 1) { jobExplorer.getJobInstance(testJob.name, parameters) }
    }

    @Test
    fun `Given a job name and job parameters, when starting a new job, but job launcher signals job restart failed, it throws an exception`() {
        val parameters = JobParametersBuilder().add("parameter", "testing123").toJobParameters()

        every { jobLauncher.run(testJob, parameters) } throws JobRestartException("Job restart failed")
        every { jobExplorer.getJobInstance(testJob.name, parameters) } returns JobInstance(123, testJob.name)

        shouldThrow<JobRestartFailed> { service.runJob(testJob.name, parameters) }

        verify(exactly = 1) { jobLauncher.run(testJob, parameters) }
        verify(exactly = 1) { jobExplorer.getJobInstance(testJob.name, parameters) }
    }

    @Test
    fun `Given a job name and job parameters, when starting a new job, but job launcher signals job already complete, it throws an exception`() {
        val parameters = JobParametersBuilder().add("parameter", "testing123").toJobParameters()

        every { jobLauncher.run(testJob, parameters) } throws JobInstanceAlreadyCompleteException("Job already complete")
        every { jobExplorer.getJobInstance(testJob.name, parameters) } returns JobInstance(123, testJob.name)

        shouldThrow<JobAlreadyComplete> { service.runJob(testJob.name, parameters) }

        verify(exactly = 1) { jobLauncher.run(testJob, parameters) }
        verify(exactly = 1) { jobExplorer.getJobInstance(testJob.name, parameters) }
    }

    @Test
    fun `Given a job id, when fetching the job status, and user is the owner of the job, it returns the job status`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters)
        val jobStatus = createJobStatus()

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { jobStatusFactory.format(jobExecution) } returns jobStatus

        service.findJobStatusById(jobId, contributorId) shouldBe Optional.of(jobStatus)

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { jobStatusFactory.format(jobExecution) }
    }

    @Test
    fun `Given a job id, when fetching the job status, and user is not the owner of the job and not an admin, it throws an exception`() {
        val jobId = JobId(123)
        val otherUser = createContributor()
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, ContributorId(MockUserId.USER)).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters)

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<JobNotFound> { service.findJobStatusById(jobId, otherUser.id) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given a job id, when fetching the job status, and user is an admin, it returns the job status`() {
        val jobId = JobId(123)
        val admin = createContributor(isAdmin = true)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, ContributorId(MockUserId.USER)).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters)
        val jobStatus = createJobStatus()

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { jobStatusFactory.format(jobExecution) } returns jobStatus

        service.findJobStatusById(jobId, admin.id)

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { jobStatusFactory.format(jobExecution) }
    }

    @Test
    fun `Given a job id, when fetching the job status, but job does not exist, it throws an exception`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)

        every { jobExplorer.getJobExecution(jobId.value) } returns null

        shouldThrow<JobNotFound> { service.findJobStatusById(jobId, contributorId) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
    }

    @ParameterizedTest
    @EnumSource(value = BatchStatus::class, names = arrayOf("COMPLETED", "FAILED", "ABANDONED"))
    fun `Given a job id, when fetching job results, and user is the owner of the job, it returns the job result`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply {
            status = batchStatus
            jobInstance = JobInstance(jobId.value, testJob.name)
        }
        val pageable = PageRequest.of(0, 10)
        val value = Optional.of<Any>("result")

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { jobResultFactory.getResult(jobExecution, any(), pageable) } returns value

        val result = service.findJobResultById(jobId, contributorId, pageable)
        result.isPresent shouldBe true
        result.get() shouldBe JobResult(
            jobId = jobId,
            jobName = testJob.name,
            status = if (batchStatus == BatchStatus.COMPLETED) Status.DONE else Status.FAILED,
            value = value,
        )

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { jobResultFactory.getResult(jobExecution, any(), pageable) }
    }

    @ParameterizedTest
    @EnumSource(value = BatchStatus::class, names = arrayOf("COMPLETED", "FAILED", "ABANDONED"))
    fun `Given a job id, when fetching job results, and user is not the owner of the job and not an admin, it throws an exception`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val otherUser = createContributor()
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, ContributorId(MockUserId.USER)).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = batchStatus }
        val pageable = PageRequest.of(0, 10)

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<JobNotFound> { service.findJobResultById(jobId, otherUser.id, pageable) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @ParameterizedTest
    @EnumSource(value = BatchStatus::class, names = arrayOf("COMPLETED", "FAILED", "ABANDONED"))
    fun `Given a job id, when fetching job results, and user is an admin, it returns the job result`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val admin = createContributor(isAdmin = true)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, ContributorId(MockUserId.USER)).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply {
            status = batchStatus
            jobInstance = JobInstance(jobId.value, testJob.name)
        }
        val pageable = PageRequest.of(0, 10)
        val value = Optional.of<Any>("result")

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { jobResultFactory.getResult(jobExecution, any(), pageable) } returns value

        val result = service.findJobResultById(jobId, admin.id, pageable)
        result.isPresent shouldBe true
        result.get() shouldBe JobResult(
            jobId = jobId,
            jobName = testJob.name,
            status = if (batchStatus == BatchStatus.COMPLETED) Status.DONE else Status.FAILED,
            value = value,
        )

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { jobResultFactory.getResult(jobExecution, any(), pageable) }
    }

    @Test
    fun `Given a job id, when fetching job results, but job does not exist, it throws an exception`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val pageable = PageRequest.of(0, 10)

        every { jobExplorer.getJobExecution(jobId.value) } returns null

        shouldThrow<JobNotFound> { service.findJobResultById(jobId, contributorId, pageable) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
    }

    @ParameterizedTest
    @EnumSource(value = BatchStatus::class, names = arrayOf("STARTING", "STARTED", "STOPPING", "STOPPED", "UNKNOWN"))
    fun `Given a job id, when fetching job results, but job has not finished, it returns an empty result`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = batchStatus }
        val pageable = PageRequest.of(0, 10)

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution

        service.findJobResultById(jobId, contributorId, pageable).isPresent shouldBe false

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
    }

    @Test
    fun `Given a job id, when stopping the job, and user is the owner of the job, it returns the job status`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = BatchStatus.STARTED }

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { jobOperator.stop(jobId.value) } returns true

        service.stopJob(jobId, contributorId)

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { jobOperator.stop(jobId.value) }
    }

    @Test
    fun `Given a job id, when stopping the job, and user is not the owner of the job and not an admin, it throws an exception`() {
        val jobId = JobId(123)
        val otherUser = createContributor()
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, ContributorId(MockUserId.USER)).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = BatchStatus.STARTED }

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { contributorRepository.findById(otherUser.id) } returns Optional.of(otherUser)

        shouldThrow<JobNotFound> { service.stopJob(jobId, otherUser.id) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { contributorRepository.findById(otherUser.id) }
    }

    @Test
    fun `Given a job id, when stopping the job, and user is an admin, it returns the job status`() {
        val jobId = JobId(123)
        val admin = createContributor(isAdmin = true)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, ContributorId(MockUserId.USER)).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = BatchStatus.STARTED }

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { contributorRepository.findById(admin.id) } returns Optional.of(admin)
        every { jobOperator.stop(jobId.value) } returns true

        service.stopJob(jobId, admin.id)

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { contributorRepository.findById(admin.id) }
        verify(exactly = 1) { jobOperator.stop(jobId.value) }
    }

    @Test
    fun `Given a job id, when stopping the job, but job does not exist, it throws an exception`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)

        every { jobExplorer.getJobExecution(jobId.value) } returns null

        shouldThrow<JobNotFound> { service.stopJob(jobId, contributorId) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
    }

    @Test
    fun `Given a job id, when stopping the job, and job execution is not running, it throws an exception`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = BatchStatus.STOPPED }

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution

        shouldThrow<JobNotRunning> { service.stopJob(jobId, contributorId) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
    }

    @Test
    fun `Given a job id, when stopping the job, and job operator signals that the job is not running, it throws an exception`() {
        val jobId = JobId(123)
        val contributorId = ContributorId(MockUserId.USER)
        val parameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(jobId.value, parameters).apply { status = BatchStatus.STOPPING }

        every { jobExplorer.getJobExecution(jobId.value) } returns jobExecution
        every { jobOperator.stop(jobId.value) } throws JobExecutionNotRunningException("Job not running")

        shouldThrow<JobNotRunning> { service.stopJob(jobId, contributorId) }

        verify(exactly = 1) { jobExplorer.getJobExecution(jobId.value) }
        verify(exactly = 1) { jobOperator.stop(jobId.value) }
    }
}
