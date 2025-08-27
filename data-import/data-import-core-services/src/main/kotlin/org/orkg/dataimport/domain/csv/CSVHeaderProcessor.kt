package org.orkg.dataimport.domain.csv

import org.orkg.common.Either
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.CSV_HEADERS_FIELD
import org.orkg.dataimport.domain.CSV_HEADER_TO_PREDICATE_FIELD
import org.orkg.dataimport.domain.extractCSVType
import org.orkg.dataimport.domain.getAndCast
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.PredicateRepository
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

open class CSVHeaderProcessor(
    private val predicateRepository: PredicateRepository,
) : Tasklet {
    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val jobExecutionContext = contribution.stepExecution.jobExecution.executionContext
        val headers: List<CSVHeader> = jobExecutionContext.getAndCast(CSV_HEADERS_FIELD)!!
        val type: CSV.Type = extractCSVType(contribution.stepExecution)
        val headerToPredicate: Map<CSVHeader, Either<ThingId, String>> = headers
            .map { header -> header to header.namespace?.let { type.schema.headers[it] } }
            .filter { (_, namespace) -> namespace?.closed != true }
            .associate { (header, namespace) ->
                if (namespace?.name == "orkg") {
                    val id = ThingId(header.name)
                    predicateRepository.findById(id).orElseThrow { PredicateNotFound(id) }
                    header to Either.left(id)
                } else {
                    val label = header.name
                    val predicates = predicateRepository.findAll(
                        label = SearchString.of(label, exactMatch = true),
                        pageable = PageRequests.SINGLE
                    )
                    val predicateId = predicates.singleOrNull()?.id
                    if (predicateId != null) {
                        header to Either.left(predicateId)
                    } else {
                        header to Either.right(label)
                    }
                }
            }
        jobExecutionContext.put(CSV_HEADER_TO_PREDICATE_FIELD, headerToPredicate)
        return RepeatStatus.FINISHED
    }
}
