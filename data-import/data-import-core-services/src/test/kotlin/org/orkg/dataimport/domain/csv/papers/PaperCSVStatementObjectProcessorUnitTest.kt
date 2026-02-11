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
import org.orkg.dataimport.domain.CSV_PAPERS_RESOURCE_LABEL_TO_ID_FIELD
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.add
import org.orkg.dataimport.domain.getAndCast
import org.orkg.dataimport.domain.testing.fixtures.createJobExecution
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord
import org.orkg.dataimport.domain.testing.fixtures.createStepExecution
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.testing.MockUserId
import org.springframework.batch.core.job.parameters.JobParametersBuilder

internal class PaperCSVStatementObjectProcessorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases = mockk()

    private val paperCSVStatementObjectProcessor = PaperCSVStatementObjectProcessor(unsafeResourceUseCases, unsafeLiteralUseCases)

    @Test
    fun `Given a paper csv record, when record contains entities that do not exist in the graph, it creates the respective entities`() {
        val record = createPaperCSVRecord().copy(
            statements = setOf(
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(namespace = "resource", value = "DOI", type = Classes.resource)
                ),
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(namespace = null, value = "New Problem", type = Classes.problem)
                ),
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(namespace = "orkg", value = "R123", type = Classes.thing)
                ),
                ContributionStatement(
                    predicate = Either.right("result"),
                    `object` = TypedValue(namespace = null, value = "5", type = Classes.integer)
                ),
            )
        )
        val contributorId = ContributorId(MockUserId.USER)
        val jobParameters = JobParametersBuilder().add(CONTRIBUTOR_ID_FIELD, contributorId).toJobParameters()
        val jobExecution = createJobExecution(jobParameters = jobParameters)
        val stepExecution = createStepExecution(jobExecution = jobExecution)
        val resourceId = ThingId("R1")
        val newProblemId = ThingId("R2")
        val literalId = ThingId("L1")
        val createResourceCommand = CreateResourceUseCase.CreateCommand(
            classes = emptySet(),
            contributorId = contributorId,
            label = "DOI",
            extractionMethod = record.extractionMethod,
        )
        val createNewProblemCommand = CreateResourceUseCase.CreateCommand(
            classes = setOf(Classes.problem),
            contributorId = contributorId,
            label = "New Problem",
            extractionMethod = record.extractionMethod,
        )
        val createLiteralCommand = CreateLiteralUseCase.CreateCommand(
            contributorId = contributorId,
            label = "5",
            datatype = Literals.XSD.INT.prefixedUri
        )

        paperCSVStatementObjectProcessor.beforeStep(stepExecution)

        every { unsafeResourceUseCases.create(createResourceCommand) } returns resourceId
        every { unsafeResourceUseCases.create(createNewProblemCommand) } returns newProblemId
        every { unsafeLiteralUseCases.create(createLiteralCommand) } returns literalId

        paperCSVStatementObjectProcessor.process(record) shouldBe record.copy(
            statements = setOf(
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(namespace = "orkg", value = resourceId.value, type = Classes.thing)
                ),
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(namespace = "orkg", value = newProblemId.value, type = Classes.thing)
                ),
                ContributionStatement(
                    predicate = Either.left(Predicates.employs),
                    `object` = TypedValue(namespace = "orkg", value = "R123", type = Classes.thing)
                ),
                ContributionStatement(
                    predicate = Either.right("result"),
                    `object` = TypedValue(namespace = "orkg", value = literalId.value, type = Classes.thing)
                ),
            )
        )

        verify(exactly = 1) { unsafeResourceUseCases.create(createResourceCommand) }
        verify(exactly = 1) { unsafeResourceUseCases.create(createNewProblemCommand) }
        verify(exactly = 1) { unsafeLiteralUseCases.create(createLiteralCommand) }

        paperCSVStatementObjectProcessor.afterStep(stepExecution).asClue {
            val resourceLabelToId = jobExecution.executionContext
                .getAndCast<Map<String, ThingId>>(CSV_PAPERS_RESOURCE_LABEL_TO_ID_FIELD)
                .shouldNotBeNull()
            resourceLabelToId.size shouldBe 2
            resourceLabelToId shouldContain ("DOI" to resourceId)
            resourceLabelToId shouldContain ("New Problem" to newProblemId)
        }
    }
}
