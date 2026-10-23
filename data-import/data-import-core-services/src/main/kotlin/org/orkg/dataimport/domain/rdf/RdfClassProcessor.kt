package org.orkg.dataimport.domain.rdf

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.getOrDefault
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.output.ThingRepository
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor

open class RdfClassProcessor(
    private val contributorId: ContributorId,
    private val extractionMethod: ExtractionMethod,
    private val modifiable: Boolean,
    private val thingRepository: ThingRepository,
    private val unsafeClassUseCases: UnsafeClassUseCases,
) : ItemProcessor<RdfClass, RdfClass>,
    StepExecutionListener {
    private lateinit var classIriToId: MutableMap<String, ThingId>

    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        classIriToId = jobExecutionContext.getOrDefault(RDF_CLASS_IRI_TO_ID, ::mutableMapOf)
    }

    override fun process(`class`: RdfClass): RdfClass? {
        val things = thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(`class`.iri.toString()))
        if (things.isEmpty) {
            val id = unsafeClassUseCases.create(
                CreateClassUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = `class`.label ?: "",
                    uri = IRI.create(`class`.iri.toString()),
                    extractionMethod = extractionMethod,
                    modifiable = modifiable,
                ),
            )
            classIriToId[`class`.iri.toString()] = id
        }
        return null
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        jobExecutionContext.put(RDF_CLASS_IRI_TO_ID, classIriToId)
        return super.afterStep(stepExecution)
    }
}
