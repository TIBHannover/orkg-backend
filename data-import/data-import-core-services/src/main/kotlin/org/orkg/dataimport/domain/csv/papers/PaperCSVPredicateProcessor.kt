package org.orkg.dataimport.domain.csv.papers

import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.Either.Companion.merge
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.CSV_PAPERS_PREDICATE_LABEL_TO_ID_FIELD
import org.orkg.dataimport.domain.extractContributorId
import org.orkg.dataimport.domain.getOrDefault
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UnsafePredicateUseCases
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor

open class PaperCSVPredicateProcessor(
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
) : ItemProcessor<PaperCSVRecord, PaperCSVRecord>,
    StepExecutionListener {
    private lateinit var predicateLabelToId: MutableMap<String, ThingId>
    private lateinit var contributorId: ContributorId

    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        predicateLabelToId = jobExecutionContext.getOrDefault(CSV_PAPERS_PREDICATE_LABEL_TO_ID_FIELD, ::mutableMapOf)
        contributorId = extractContributorId(stepExecution)
    }

    override fun process(item: PaperCSVRecord): PaperCSVRecord? {
        val statements = item.statements.map { statement ->
            if (statement.predicate.isLeft) {
                statement
            } else {
                val id = statement.predicate.mapRight { label ->
                    predicateLabelToId.getOrPut(label) {
                        val command = CreatePredicateUseCase.CreateCommand(
                            label = label,
                            contributorId = contributorId,
                        )
                        unsafePredicateUseCases.create(command)
                    }
                }.merge()
                statement.copy(predicate = Either.left(id))
            }
        }
        return item.copy(statements = statements.toSet())
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        jobExecutionContext.put(CSV_PAPERS_PREDICATE_LABEL_TO_ID_FIELD, predicateLabelToId)
        return super.afterStep(stepExecution)
    }
}
