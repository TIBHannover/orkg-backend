package org.orkg.dataimport.domain.jobs

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.RDF_FILE_ID_FIELD
import org.orkg.dataimport.domain.RdfFileNotFound
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.domain.rdf.RdfFile.State
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createRdfFile
import org.orkg.dataimport.output.RdfFileRepository
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import java.util.Optional
import java.util.UUID

internal class RdfFileStateUpdaterUnitTest : MockkBaseTest {
    private val rdfFileRepository: RdfFileRepository = mockk()
    private val startState: State = State.UPLOADED
    private val successState: State = State.VALIDATION_DONE
    private val stoppedState: State = State.VALIDATION_STOPPED
    private val failureState: State = State.VALIDATION_STOPPED
    private val jobIdSetter: (RdfFile, JobId) -> RdfFile = mockk()

    private val rdfFileStateUpdater = RdfFileStateUpdater(rdfFileRepository, startState, successState, stoppedState, failureState, jobIdSetter)

    @Test
    fun `Given a job execution, when before the job execution, it sets the current job id and updates the rdfFile state to startState`() {
        val jobId = JobId(123)
        val rdfFile = createRdfFile()
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters)

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { rdfFileRepository.save(any()) } just runs
        every { jobIdSetter.invoke(any(), any()) } returns rdfFile.copy(importJobId = jobId)

        rdfFileStateUpdater.beforeJob(jobExecution)

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
        verify(exactly = 1) { jobIdSetter.invoke(any(), any()) }
        verify(exactly = 1) {
            rdfFileRepository.save(
                withArg {
                    it.importJobId shouldBe jobId
                    it.state shouldBe startState
                },
            )
        }
    }

    @Test
    fun `Given a job execution, when before the job execution, and rdfFile is already in startState, it only sets the current job id`() {
        val jobId = JobId(123)
        val rdfFile = createRdfFile().copy(state = startState)
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters)

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { rdfFileRepository.save(any()) } just runs
        every { jobIdSetter.invoke(any(), any()) } returns rdfFile.copy(importJobId = jobId)

        rdfFileStateUpdater.beforeJob(jobExecution)

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
        verify(exactly = 1) { jobIdSetter.invoke(any(), any()) }
        verify(exactly = 1) {
            rdfFileRepository.save(
                withArg {
                    it.importJobId shouldBe jobId
                    it.state shouldBe startState
                },
            )
        }
    }

    @Test
    fun `Given a job execution, when before the job execution, and rdfFile does not exist, it throws an exception`() {
        val rdfFileId = UUID.fromString("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFileId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)

        every { rdfFileRepository.findById(rdfFileId) } returns Optional.empty()

        shouldThrow<RdfFileNotFound> { rdfFileStateUpdater.beforeJob(jobExecution) }

        verify(exactly = 1) { rdfFileRepository.findById(rdfFileId) }
    }

    @Test
    fun `Given a job execution, when before the job execution, and rdfFile state is final, it throws an exception`() {
        val jobId = JobId(123)
        val rdfFile = createRdfFile().copy(state = State.IMPORT_DONE)
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters)

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { jobIdSetter.invoke(any(), any()) } returns rdfFile.copy(importJobId = jobId)

        shouldThrow<IllegalStateException> { rdfFileStateUpdater.beforeJob(jobExecution) }

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
        verify(exactly = 1) { jobIdSetter.invoke(any(), any()) }
    }

    @ParameterizedTest
    @EnumSource(BatchStatus::class, names = ["COMPLETED"])
    fun `Given a job execution, when the job execution completed successfully, it sets the rdfFile state to successState`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_RUNNING)
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters).apply {
            status = batchStatus
        }

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { rdfFileRepository.save(any()) } just runs

        rdfFileStateUpdater.afterJob(jobExecution)

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
        verify(exactly = 1) {
            rdfFileRepository.save(
                withArg {
                    it.state shouldBe successState
                },
            )
        }
    }

    @ParameterizedTest
    @EnumSource(BatchStatus::class, names = ["STOPPING", "STOPPED"])
    fun `Given a job execution, when the job execution has stopped, it sets the rdfFile state to stoppedState`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_RUNNING)
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters).apply {
            status = batchStatus
        }

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { rdfFileRepository.save(any()) } just runs

        rdfFileStateUpdater.afterJob(jobExecution)

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
        verify(exactly = 1) {
            rdfFileRepository.save(
                withArg {
                    it.state shouldBe stoppedState
                },
            )
        }
    }

    @ParameterizedTest
    @EnumSource(BatchStatus::class, names = ["FAILED", "ABANDONED", "UNKNOWN"])
    fun `Given a job execution, when the job execution has failed, it sets the rdfFile state to failureState`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val rdfFile = createRdfFile().copy(state = State.VALIDATION_RUNNING)
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters).apply {
            status = batchStatus
        }

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)
        every { rdfFileRepository.save(any()) } just runs

        rdfFileStateUpdater.afterJob(jobExecution)

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
        verify(exactly = 1) {
            rdfFileRepository.save(
                withArg {
                    it.state shouldBe failureState
                },
            )
        }
    }

    @Test
    fun `Given a job execution, when after the job execution, and target state is not directly after current rdfFile state, it throws an exception`() {
        val jobId = JobId(123)
        val rdfFile = createRdfFile()
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters).apply {
            status = BatchStatus.COMPLETED
        }

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<IllegalStateException> { rdfFileStateUpdater.afterJob(jobExecution) }

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
    }

    @Test
    fun `Given a job execution, when after the job execution, and rdfFile state is final, it throws an exception`() {
        val jobId = JobId(123)
        val rdfFile = createRdfFile().copy(state = State.IMPORT_DONE)
        val jobParameters = JobParametersBuilder().add(RDF_FILE_ID_FIELD, rdfFile.id).toJobParameters()
        val jobExecution = createJobExecution(id = jobId.value, jobParameters = jobParameters).apply {
            status = BatchStatus.COMPLETED
        }

        every { rdfFileRepository.findById(rdfFile.id) } returns Optional.of(rdfFile)

        shouldThrow<IllegalStateException> { rdfFileStateUpdater.afterJob(jobExecution) }

        verify(exactly = 1) { rdfFileRepository.findById(rdfFile.id) }
    }
}
