package eu.tib.orkg.prototype.export.rdf.domain

import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.export.rdf.api.ExportRDFUseCase
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.forEach
import java.net.URI
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
class RDFService(
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val classesRepository: ClassRepository
) : ExportRDFUseCase {
    override fun dumpToNTriple(): String = buildString {
        classesRepository.forEach { append(it.toNTriple()) }
        predicateRepository.forEach { append(it.toNTriple()) }
        resourceRepository.forEach { append(it.toNTriple()) }
        statementRepository.forEach { append(it.toNTriple()) }
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
                if (uri?.isValidForNTriple() == true) add(OWL.EQUIVALENTCLASS, uri)
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

internal fun Class.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val sb = StringBuilder()
    sb.append("<$cPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .\n")
    if (uri?.isValidForNTriple() == true) sb.append("<$cPrefix$id> <${OWL.EQUIVALENTCLASS}> <$uri> .\n")
    sb.append("<$cPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    return sb.toString()
}

internal fun Predicate.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    return "<$pPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Predicate> .\n" + "<$pPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${
        escapeLiterals(
            label
        )
    }\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"
}

internal fun Resource.toNTriple(): String {
    val cPrefix = RdfConstants.CLASS_NS
    val rPrefix = RdfConstants.RESOURCE_NS
    val sb = StringBuilder()
    sb.append("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
    classes.forEach { sb.append("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
    sb.append("<$rPrefix$id> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    return sb.toString()
}

/**
 * Convert the triple to a statement in NTriple format.
 */
internal fun GeneralStatement.toNTriple(): String {
    val pPrefix = RdfConstants.PREDICATE_NS
    val result = "${serializeThing(subject)} <$pPrefix${predicate.id}> ${serializeThing(`object`)} .\n"
    if (result[0] == '"')
    // Ignore literal
    // TODO: log this somewhere
        return ""
    return result
}

/**
 * Checks whether a URI is valid to be included in RDF .nt serialization.
 */
internal fun URI.isValidForNTriple(): Boolean {
    // FIXME: what makes a URI valid to the N-Triple format ? See #349 and #220
    return !toString().equals("null", ignoreCase = true)
}

private fun serializeThing(thing: Thing): String {
    val rPrefix = RdfConstants.RESOURCE_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    val cPrefix = RdfConstants.CLASS_NS
    return when (thing) {
        is Resource -> "<$rPrefix${thing.id}>"
        is Predicate -> "<$pPrefix${thing.id}>"
        is Class -> "<$cPrefix${thing.id}>"
        is Literal -> "\"${escapeLiterals(thing.label)}\"^^<http://www.w3.org/2001/XMLSchema#string>"
    }
}
