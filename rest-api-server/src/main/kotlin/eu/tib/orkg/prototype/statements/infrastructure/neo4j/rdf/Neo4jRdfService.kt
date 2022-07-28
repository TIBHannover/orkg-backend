package eu.tib.orkg.prototype.statements.infrastructure.neo4j.rdf

import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.rdf.RdfService
import eu.tib.orkg.prototype.statements.domain.model.toNTriple
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.io.OutputStream
import java.util.*
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jRdfService(
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val classesRepository: ClassRepository
) : RdfService {
    override fun dumpToNTriple(out: OutputStream) {
        val everything = sequenceOf<String>() + // Just for aligning the code below…
            classesRepository.findAll().map(Class::toNTriple) +
            predicateRepository.findAll().map(Predicate::toNTriple) +
            resourceRepository.findAll().map(Resource::toNTriple) +
            statementRepository.findAll().map(GeneralStatement::toNTriple)
        everything.forEach { out.write(it.toByteArray()) }
    }

    override fun rdfModelFor(id: ClassId): Optional<Model> {
        val clazz = classesRepository.findByClassId(id).orElse(null) ?: return Optional.empty()
        with(clazz) {
            val model = ModelBuilder().apply {
                setNamespace("c", RdfConstants.CLASS_NS)
                setNamespace(RDF.NS)
                setNamespace(RDFS.NS)
                setNamespace(OWL.NS)
                subject("c:$id").add(RDFS.LABEL, label).add(RDF.TYPE, "owl:Class")
                if (uri != null) add(OWL.EQUIVALENTCLASS, uri)
            }.build()
            return Optional.of(model)
        }
    }

    override fun rdfModelFor(id: PredicateId): Optional<Model> {
        val predicate = predicateRepository.findByPredicateId(id).orElse(null) ?: return Optional.empty()
        with(predicate) {
            val model = ModelBuilder().apply {
                setNamespace("p", RdfConstants.PREDICATE_NS)
                setNamespace("c", RdfConstants.CLASS_NS)
                setNamespace(RDF.NS)
                setNamespace(RDFS.NS)
                subject("p:$id").add(RDFS.LABEL, label).add(RDF.TYPE, "c:Predicate")
            }.build()
            return Optional.of(model)
        }
    }

    override fun rdfModelFor(id: ResourceId): Optional<Model> {
        val resource = resourceRepository.findByResourceId(id).orElse(null) ?: return Optional.empty()
        val statements =
            statementRepository.findAllBySubject(resource.id!!.value, PageRequest.of(0, Int.MAX_VALUE)) // FIXME
        with(resource) {
            val model = ModelBuilder().apply {
                setNamespace("r", RdfConstants.RESOURCE_NS)
                setNamespace("p", RdfConstants.PREDICATE_NS)
                setNamespace("c", RdfConstants.CLASS_NS)
                setNamespace(RDF.NS)
                setNamespace(RDFS.NS)
                subject("r:$id").add(RDFS.LABEL, label).add(RDF.TYPE, "c:Resource")
                classes.forEach { add(RDF.TYPE, "c:${it.value}") }
                statements.forEach {
                    when (it.`object`) {
                        is Resource -> add("p:${it.predicate.id}", "r:${it.`object`.id}")
                        is Literal -> add("p:${it.predicate.id}", "\"${it.`object`.label}\"")
                        else -> throw IllegalStateException("Unable to convert statement while building RDF model. This is a bug.")
                    }
                }
            }.build()
            return Optional.of(model)
        }
    }
}
