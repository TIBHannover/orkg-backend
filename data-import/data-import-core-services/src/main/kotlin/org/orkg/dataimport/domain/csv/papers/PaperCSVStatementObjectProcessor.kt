package org.orkg.dataimport.domain.csv.papers

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.CSV_PAPERS_RESOURCE_LABEL_TO_ID_FIELD
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.extractContributorId
import org.orkg.dataimport.domain.getOrDefault
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor

open class PaperCSVStatementObjectProcessor(
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : ItemProcessor<PaperCSVRecord, PaperCSVRecord>,
    StepExecutionListener {
    private lateinit var resourceLabelToId: MutableMap<String, ThingId>
    private lateinit var contributorId: ContributorId

    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        resourceLabelToId = jobExecutionContext.getOrDefault(CSV_PAPERS_RESOURCE_LABEL_TO_ID_FIELD, ::mutableMapOf)
        contributorId = extractContributorId(stepExecution)
    }

    override fun process(item: PaperCSVRecord): PaperCSVRecord? {
        val statements = item.statements.map { statement ->
            if (statement.`object`.namespace == "orkg") {
                statement
            } else {
                val value = statement.`object`.value!!
                val type = statement.`object`.type
                val id = when (type) {
                    Classes.resource, Classes.problem -> resourceLabelToId.getOrPut(value) {
                        unsafeResourceUseCases.create(
                            CreateResourceUseCase.CreateCommand(
                                classes = if (type != Classes.resource) setOf(type) else emptySet(),
                                contributorId = contributorId,
                                label = value,
                                extractionMethod = item.extractionMethod,
                            )
                        )
                    }
                    else -> unsafeLiteralUseCases.create(
                        CreateLiteralUseCase.CreateCommand(
                            contributorId = contributorId,
                            label = value,
                            datatype = Literals.XSD.fromClass(type)!!.prefixedUri
                        )
                    )
                }
                statement.copy(`object` = TypedValue("orkg", id.value, Classes.thing))
            }
        }
        return item.copy(statements = statements.toSet())
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        jobExecutionContext.put(CSV_PAPERS_RESOURCE_LABEL_TO_ID_FIELD, resourceLabelToId)
        return super.afterStep(stepExecution)
    }
}
