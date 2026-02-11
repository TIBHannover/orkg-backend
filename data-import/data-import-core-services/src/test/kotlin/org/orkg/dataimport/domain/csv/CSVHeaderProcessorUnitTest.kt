package org.orkg.dataimport.domain.csv

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.CSV_HEADER_TO_PREDICATE_FIELD
import org.orkg.dataimport.domain.CSV_TYPE_FIELD
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.pageOf
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.scope.context.StepContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.data.domain.Page
import java.util.Optional

internal class CSVHeaderProcessorUnitTest : MockkBaseTest {
    private val predicateRepository: PredicateRepository = mockk()

    private val csvHeaderProcessor = CSVHeaderProcessor(predicateRepository)

    @Test
    fun `Given a csv header, when processing predicates, and namespaces is orkg, it looks up predicates by id and saves the predicate to id mapping to the job execution context`() {
        val predicateId = ThingId("P123")
        val headers = listOf(
            CSVHeader(
                namespace = "orkg",
                column = 1,
                name = predicateId.value,
                columnType = null
            )
        )
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, CSV.Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))
        val predicate = createPredicate(id = predicateId)

        every { predicateRepository.findById(predicateId) } returns Optional.of(predicate)

        csvHeaderProcessor.execute(contribution, chunkContext) shouldBe RepeatStatus.FINISHED

        val headerToPredicate = contribution.stepExecution.jobExecution.executionContext.get(CSV_HEADER_TO_PREDICATE_FIELD)
        headerToPredicate shouldBe mapOf<CSVHeader, Either<ThingId, String>>(headers[0] to Either.left(predicateId))

        verify(exactly = 1) { predicateRepository.findById(predicateId) }
    }

    @Test
    fun `Given a csv header, when processing predicates, and namespaces is orkg, it looks up predicates by id, and predicate does not exist, it throws an exception`() {
        val predicateId = ThingId("P123")
        val headers = listOf(
            CSVHeader(
                namespace = "orkg",
                column = 1,
                name = predicateId.value,
                columnType = null
            )
        )
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, CSV.Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))

        every { predicateRepository.findById(predicateId) } returns Optional.empty()

        shouldThrow<PredicateNotFound> { csvHeaderProcessor.execute(contribution, chunkContext) }

        verify(exactly = 1) { predicateRepository.findById(predicateId) }
    }

    @Test
    fun `Given a csv header, when processing predicates, and namespaces is closed, it does nothing`() {
        val headers = listOf(
            CSVHeader(
                namespace = "paper",
                column = 1,
                name = "title",
                columnType = null
            )
        )
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, CSV.Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))

        csvHeaderProcessor.execute(contribution, chunkContext) shouldBe RepeatStatus.FINISHED

        val headerToPredicate = contribution.stepExecution.jobExecution.executionContext.get(CSV_HEADER_TO_PREDICATE_FIELD)
        headerToPredicate shouldBe emptyMap<CSVHeader, Either<ThingId, String>>()
    }

    @Test
    @DisplayName("Given a csv header, when processing predicates, and namespaces is open, it looks up predicates by label, and predicate exists, it saves the predicate to id mapping to the job execution context")
    fun givenACsvHeader_whenProcessingPredicates_andNamespaceIsOpen_itLooksupPredicatesByLabel_andPredicateExists_itSavesThePredicateToIdMappingToTheJobExecutionContext() {
        val label = "predicate label"
        val headers = listOf(
            CSVHeader(
                namespace = "some-open-namespace",
                column = 1,
                name = label,
                columnType = null
            )
        )
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, CSV.Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))
        val predicate = createPredicate(label = label)

        every { predicateRepository.findAll(label = any(), pageable = PageRequests.SINGLE) } returns pageOf(predicate)

        csvHeaderProcessor.execute(contribution, chunkContext) shouldBe RepeatStatus.FINISHED

        val headerToPredicate = contribution.stepExecution.jobExecution.executionContext.get(CSV_HEADER_TO_PREDICATE_FIELD)
        headerToPredicate shouldBe mapOf<CSVHeader, Either<ThingId, String>>(headers[0] to Either.left(predicate.id))

        verify(exactly = 1) {
            predicateRepository.findAll(
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>()
                    it.input shouldBe label
                },
                pageable = PageRequests.SINGLE
            )
        }
    }

    @Test
    @DisplayName("Given a csv header, when processing predicates, and namespaces is open, it looks up predicates by label, and predicate does not exist, it saves the predicate to id mapping to the job execution context")
    fun `givenACsvHeader_whenProcessingPredicates_andNamespaceIsOpen_itLooksupPredicatesByLabel_andPredicateDoesNotExist_itSavesThePredicateToIdMappingToTheJobExecutionContext`() {
        val label = "predicate label"
        val headers = listOf(
            CSVHeader(
                namespace = "some-open-namespace",
                column = 1,
                name = label,
                columnType = null
            )
        )
        val jobParameters = JobParametersBuilder().add(CSV_TYPE_FIELD, CSV.Type.PAPER).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters).apply {
            executionContext.put(CSV_HEADERS_FIELD, headers)
        }
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val contribution = StepContribution(stepExecution)
        val chunkContext = ChunkContext(StepContext(stepExecution))

        every { predicateRepository.findAll(label = any(), pageable = PageRequests.SINGLE) } returns Page.empty()

        csvHeaderProcessor.execute(contribution, chunkContext) shouldBe RepeatStatus.FINISHED

        val headerToPredicate = contribution.stepExecution.jobExecution.executionContext.get(CSV_HEADER_TO_PREDICATE_FIELD)
        headerToPredicate shouldBe mapOf<CSVHeader, Either<ThingId, String>>(headers[0] to Either.right(label))

        verify(exactly = 1) {
            predicateRepository.findAll(
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>()
                    it.input shouldBe label
                },
                pageable = PageRequests.SINGLE
            )
        }
    }
}
