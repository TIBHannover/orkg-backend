package org.orkg.dataimport.domain.rdf

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.RDF_PREDICATE_IRI_TO_ID
import org.orkg.dataimport.domain.getOrDefault
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.output.ThingRepository
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor

open class RdfPredicateProcessor(
    private val contributorId: ContributorId,
    private val extractionMethod: ExtractionMethod,
    private val modifiable: Boolean,
    private val thingRepository: ThingRepository,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
) : ItemProcessor<RdfPredicate, RdfPredicate>,
    StepExecutionListener {
    private lateinit var predicateIriToId: MutableMap<String, ThingId>

    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        predicateIriToId = jobExecutionContext.getOrDefault(RDF_PREDICATE_IRI_TO_ID, ::mutableMapOf)
    }

    override fun process(predicate: RdfPredicate): RdfPredicate? {
        val things = thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(predicate.iri.toString()))
        if (things.isEmpty) {
            val id = unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = predicate.label ?: "",
                    uri = IRI.create(predicate.iri.toString()),
                    extractionMethod = extractionMethod,
                    modifiable = modifiable,
                ),
            )
            predicateIriToId[predicate.iri.toString()] = id
        }
        return null
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        jobExecutionContext.put(RDF_PREDICATE_IRI_TO_ID, predicateIriToId)
        return super.afterStep(stepExecution)
    }
}
