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
import org.orkg.dataimport.domain.CSVNotFound
import org.orkg.dataimport.domain.CSV_ID_FIELD
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.testing.fixtures.createCSV
import org.orkg.dataimport.output.CSVRepository
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import java.util.Optional

internal class CSVStateUpdaterUnitTest : MockkBaseTest {
    private val csvRepository: CSVRepository = mockk()
    private val startState: State = State.UPLOADED
    private val successState: State = State.VALIDATION_DONE
    private val stoppedState: State = State.VALIDATION_STOPPED
    private val failureState: State = State.VALIDATION_STOPPED
    private val jobIdSetter: (CSV, JobId) -> CSV = mockk()

    private val csvStateUpdater = CSVStateUpdater(csvRepository, startState, successState, stoppedState, failureState, jobIdSetter)

    @Test
    fun `Given a job execution, when before the job execution, it sets the current job id and updates the csv state to startState`() {
        val jobId = JobId(123)
        val csv = createCSV()
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters)

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)
        every { csvRepository.save(any()) } just runs
        every { jobIdSetter.invoke(any(), any()) } returns csv.copy(importJobId = jobId)

        csvStateUpdater.beforeJob(jobExecution)

        verify(exactly = 1) { csvRepository.findById(csv.id) }
        verify(exactly = 1) { jobIdSetter.invoke(any(), any()) }
        verify(exactly = 1) {
            csvRepository.save(
                withArg {
                    it.importJobId shouldBe jobId
                    it.state shouldBe startState
                }
            )
        }
    }

    @Test
    fun `Given a job execution, when before the job execution, and csv is alaredy in startState, it only sets the current job id`() {
        val jobId = JobId(123)
        val csv = createCSV().copy(state = startState)
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters)

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)
        every { csvRepository.save(any()) } just runs
        every { jobIdSetter.invoke(any(), any()) } returns csv.copy(importJobId = jobId)

        csvStateUpdater.beforeJob(jobExecution)

        verify(exactly = 1) { csvRepository.findById(csv.id) }
        verify(exactly = 1) { jobIdSetter.invoke(any(), any()) }
        verify(exactly = 1) {
            csvRepository.save(
                withArg {
                    it.importJobId shouldBe jobId
                    it.state shouldBe startState
                }
            )
        }
    }

    @Test
    fun `Given a job execution, when before the job execution, and csv does not exist, it throws an exception`() {
        val csvId = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837")
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csvId).toJobParameters()
        val jobExecution = JobExecution(123, jobParameters)

        every { csvRepository.findById(csvId) } returns Optional.empty()

        shouldThrow<CSVNotFound> { csvStateUpdater.beforeJob(jobExecution) }

        verify(exactly = 1) { csvRepository.findById(csvId) }
    }

    @Test
    fun `Given a job execution, when before the job execution, and csv state is final, it throws an exception`() {
        val jobId = JobId(123)
        val csv = createCSV().copy(state = State.IMPORT_DONE)
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters)

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)
        every { jobIdSetter.invoke(any(), any()) } returns csv.copy(importJobId = jobId)

        shouldThrow<IllegalStateException> { csvStateUpdater.beforeJob(jobExecution) }

        verify(exactly = 1) { csvRepository.findById(csv.id) }
        verify(exactly = 1) { jobIdSetter.invoke(any(), any()) }
    }

    @ParameterizedTest
    @EnumSource(BatchStatus::class, names = ["COMPLETED"])
    fun `Given a job execution, when the job execution completed successfully, it sets the csv state to successState`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val csv = createCSV().copy(state = State.VALIDATION_RUNNING)
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters).apply {
            status = batchStatus
        }

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)
        every { csvRepository.save(any()) } just runs

        csvStateUpdater.afterJob(jobExecution)

        verify(exactly = 1) { csvRepository.findById(csv.id) }
        verify(exactly = 1) {
            csvRepository.save(
                withArg {
                    it.state shouldBe successState
                }
            )
        }
    }

    @ParameterizedTest
    @EnumSource(BatchStatus::class, names = ["STOPPING", "STOPPED"])
    fun `Given a job execution, when the job execution has stopped, it sets the csv state to stoppedState`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val csv = createCSV().copy(state = State.VALIDATION_RUNNING)
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters).apply {
            status = batchStatus
        }

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)
        every { csvRepository.save(any()) } just runs

        csvStateUpdater.afterJob(jobExecution)

        verify(exactly = 1) { csvRepository.findById(csv.id) }
        verify(exactly = 1) {
            csvRepository.save(
                withArg {
                    it.state shouldBe stoppedState
                }
            )
        }
    }

    @ParameterizedTest
    @EnumSource(BatchStatus::class, names = ["FAILED", "ABANDONED", "UNKNOWN"])
    fun `Given a job execution, when the job execution has failed, it sets the csv state to failureState`(batchStatus: BatchStatus) {
        val jobId = JobId(123)
        val csv = createCSV().copy(state = State.VALIDATION_RUNNING)
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters).apply {
            status = batchStatus
        }

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)
        every { csvRepository.save(any()) } just runs

        csvStateUpdater.afterJob(jobExecution)

        verify(exactly = 1) { csvRepository.findById(csv.id) }
        verify(exactly = 1) {
            csvRepository.save(
                withArg {
                    it.state shouldBe failureState
                }
            )
        }
    }

    @Test
    fun `Given a job execution, when after the job execution, and target state is not directly after current csv state, it throws an exception`() {
        val jobId = JobId(123)
        val csv = createCSV()
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters).apply {
            status = BatchStatus.COMPLETED
        }

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)

        shouldThrow<IllegalStateException> { csvStateUpdater.afterJob(jobExecution) }

        verify(exactly = 1) { csvRepository.findById(csv.id) }
    }

    @Test
    fun `Given a job execution, when after the job execution, and csv state is final, it throws an exception`() {
        val jobId = JobId(123)
        val csv = createCSV().copy(state = State.IMPORT_DONE)
        val jobParameters = JobParametersBuilder().add(CSV_ID_FIELD, csv.id).toJobParameters()
        val jobExecution = JobExecution(jobId.value, jobParameters).apply {
            status = BatchStatus.COMPLETED
        }

        every { csvRepository.findById(csv.id) } returns Optional.of(csv)

        shouldThrow<IllegalStateException> { csvStateUpdater.afterJob(jobExecution) }

        verify(exactly = 1) { csvRepository.findById(csv.id) }
    }
}
