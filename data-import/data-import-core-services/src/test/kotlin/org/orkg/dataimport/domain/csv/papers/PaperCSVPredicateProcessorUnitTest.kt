package org.orkg.dataimport.domain.csv.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.dataimport.domain.CONTRIBUTOR_ID_FIELD
import org.orkg.dataimport.domain.CSV_PAPERS_PREDICATE_LABEL_TO_ID_FIELD
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.getAndCast
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.testing.MockUserId
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.StepExecution

internal class PaperCSVPredicateProcessorUnitTest : MockkBaseTest {
    private val unsafePredicateUseCases: UnsafePredicateUseCases = mockk()

    private val paperCSVPredicateProcessor = PaperCSVPredicateProcessor(unsafePredicateUseCases)

    @Test
    fun `Given a paper csv record, it creates missing predicates, and saves their ids in the job execution context`() {
        val record = createPaperCSVRecord()
        val contributorId = ContributorId(MockUserId.USER)
        val jobParameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = JobExecution(123, jobParameters)
        val stepExecution = StepExecution("test", jobExecution)
        val predicateId = ThingId("P123")

        paperCSVPredicateProcessor.beforeStep(stepExecution)

        every { unsafePredicateUseCases.create(any()) } returns predicateId

        paperCSVPredicateProcessor.process(record).asClue {
            it.shouldNotBeNull()
            it.statements shouldBe setOf(
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(
                        namespace = "resource",
                        value = "DOI",
                        type = Classes.resource,
                    )
                ),
                ContributionStatement(
                    predicate = Either.left(predicateId),
                    `object` = TypedValue(
                        namespace = "resource",
                        value = "Result",
                        type = Classes.resource,
                    )
                )
            )
        }

        verify(exactly = 1) {
            unsafePredicateUseCases.create(
                withArg {
                    it.label shouldBe "result"
                    it.contributorId shouldBe contributorId
                }
            )
        }

        paperCSVPredicateProcessor.afterStep(stepExecution).asClue {
            val predicateLabelToId = jobExecution.executionContext
                .getAndCast<Map<String, ThingId>>(CSV_PAPERS_PREDICATE_LABEL_TO_ID_FIELD)
                .shouldNotBeNull()
            predicateLabelToId.size shouldBe 1
            predicateLabelToId shouldContain ("result" to predicateId)
        }
    }
}
