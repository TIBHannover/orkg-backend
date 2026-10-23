package org.orkg.dataimport.domain.rdf

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_RESOURCE_IRI_TO_ID
import org.orkg.dataimport.domain.getOrDefault
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ThingRepository
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor

open class RdfResourceProcessor(
    private val contributorId: ContributorId,
    private val extractionMethod: ExtractionMethod,
    private val modifiable: Boolean,
    private val thingRepository: ThingRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
) : ItemProcessor<RdfResource, RdfResource>,
    StepExecutionListener {
    private lateinit var classIriToId: MutableMap<String, ThingId>
    private lateinit var resourceIriToId: MutableMap<String, ThingId>

    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        resourceIriToId = jobExecutionContext.getOrDefault(RDF_RESOURCE_IRI_TO_ID, ::mutableMapOf)
        classIriToId = jobExecutionContext.getOrDefault(RDF_CLASS_IRI_TO_ID, ::mutableMapOf)
    }

    override fun process(resource: RdfResource): RdfResource? {
        val things by lazy { thingRepository.findAll(PageRequests.SINGLE, uri = IRI.create(resource.iri.toString())) }
        if (resource.iri.isBNode || things.isEmpty) {
            val classes = resource.classes.mapTo(mutableSetOf()) { classIri ->
                classIriToId.getOrElse(classIri.toString()) {
                    thingRepository.findAll(
                        pageable = PageRequests.SINGLE,
                        uri = IRI.create(classIri.toString()),
                    ).single().id
                }
            }
            val uri = resource.iri.takeUnless { it.isBNode }
                ?.let { IRI.create(resource.iri.toString()) }
            val id = unsafeResourceUseCases.create(
                CreateResourceUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = resource.label ?: "",
                    classes = classes,
                    uri = uri,
                    extractionMethod = extractionMethod,
                    modifiable = modifiable,
                ),
            )
            resourceIriToId[resource.iri.toString()] = id
        }
        return null
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        jobExecutionContext.put(RDF_RESOURCE_IRI_TO_ID, resourceIriToId)
        return super.afterStep(stepExecution)
    }
}
