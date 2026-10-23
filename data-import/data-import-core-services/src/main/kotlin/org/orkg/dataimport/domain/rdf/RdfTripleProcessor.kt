package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.eclipse.rdf4j.model.vocabulary.XSD
import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.RDF_CLASS_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_PREDICATE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_RESOURCE_IRI_TO_ID
import org.orkg.dataimport.domain.RDF_STATEMENT_IDS
import org.orkg.dataimport.domain.getOrDefault
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.output.ThingRepository
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ItemProcessor

open class RdfTripleProcessor(
    private val contributorId: ContributorId,
    private val extractionMethod: ExtractionMethod,
    private val modifiable: Boolean,
    private val thingRepository: ThingRepository,
    private val statementUseCases: StatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
) : ItemProcessor<RdfTriple, RdfTriple>,
    StepExecutionListener {
    private lateinit var iriToId: Map<String, ThingId>
    private lateinit var statementIds: MutableSet<StatementId>

    override fun beforeStep(stepExecution: StepExecution) {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        iriToId = jobExecutionContext.getOrDefault<Map<String, ThingId>>(RDF_RESOURCE_IRI_TO_ID, ::mutableMapOf)
            .plus(jobExecutionContext.getOrDefault(RDF_CLASS_IRI_TO_ID, ::mutableMapOf))
            .plus(jobExecutionContext.getOrDefault(RDF_PREDICATE_IRI_TO_ID, ::mutableMapOf))
        statementIds = jobExecutionContext.getOrDefault(RDF_STATEMENT_IDS, ::mutableSetOf)
    }

    override fun process(triple: RdfTriple): RdfTriple? {
        if (triple.predicate == RDFS.SUBCLASSOF || triple.predicate == RDF.TYPE || triple.predicate == RDFS.LABEL) {
            return null
        }
        val subjectId = lookup(triple.subject)
        val predicateId = lookup(triple.predicate)
        val objectId by lazy {
            if (triple.`object`.isLiteral) {
                val literal = triple.`object` as Literal
                unsafeLiteralUseCases.create(
                    CreateLiteralUseCase.CreateCommand(
                        contributorId = contributorId,
                        label = literal.label,
                        datatype = literal.datatype.toString().replace(XSD.NAMESPACE, "${XSD.PREFIX}:"),
                        extractionMethod = extractionMethod,
                        modifiable = modifiable,
                    ),
                )
            } else {
                lookup(triple.`object` as Resource)
            }
        }
        val statements = if (triple.`object`.isLiteral) {
            statementUseCases.findAll(
                subjectId = subjectId,
                predicateId = predicateId,
                objectLabel = (triple.`object` as Literal).label,
                pageable = PageRequests.SINGLE,
            )
        } else {
            statementUseCases.findAll(
                subjectId = subjectId,
                predicateId = predicateId,
                objectId = objectId,
                pageable = PageRequests.SINGLE,
            )
        }
        if (statements.isEmpty) {
            val id = statementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = predicateId,
                    objectId = objectId,
                    extractionMethod = extractionMethod,
                    modifiable = modifiable,
                ),
            )
            statementIds.add(id)
        }
        return null
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val jobExecutionContext = stepExecution.jobExecution.executionContext
        jobExecutionContext.put(RDF_STATEMENT_IDS, statementIds)
        return super.afterStep(stepExecution)
    }

    private fun lookup(resource: Resource): ThingId {
        val resourceString = resource.toString()
        if (resourceString.startsWith(ORKG_VOCAB_PREFIX)) {
            return ThingId(resourceString.substringAfterLast("/"))
        }
        return iriToId.getOrElse(resourceString) {
            val things = thingRepository.findAll(
                pageable = PageRequests.SINGLE,
                uri = IRI.create(resourceString),
            )
            things.single().id
        }
    }
}
