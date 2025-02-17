package org.orkg.export.domain

import java.io.Writer
import java.util.*
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.export.input.ExportRDFUseCase
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.forEach
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private const val DEFAULT_FILE_NAME = "rdf-export-orkg.nt"

@Service
class RDFService(
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val classesRepository: ClassRepository,
    private val fileExportService: FileExportService,
    private val classHierarchyRepository: ClassHierarchyRepository
) : ExportRDFUseCase {
    override fun dumpToNTriple(writer: Writer) {
        classesRepository.forEach({
            it.toNTriple(writer, classHierarchyRepository)
        }, writer::flush)
        predicateRepository.forEach({
            it.toNTriple(writer)
        }, writer::flush)
        resourceRepository.forEach({
            if (Classes.rosettaStoneStatement !in it.classes)
                it.toNTriple(writer)
        }, writer::flush)
        statementRepository.forEach({
            it.toNTriple(writer)
        }, writer::flush)
    }

    override fun dumpToNTriple(path: String?) =
        fileExportService.writeToFile(path, DEFAULT_FILE_NAME) { dumpToNTriple(it) }

    override fun rdfModelForClass(id: ThingId): Optional<Model> {
        val clazz = classesRepository.findById(id).orElse(null) ?: return Optional.empty()
        with(clazz) {
            val model = ModelBuilder().apply {
                setNamespace("c", RdfConstants.CLASS_NS)
                setNamespace(RDF.NS)
                setNamespace(RDFS.NS)
                setNamespace(OWL.NS)
                subject("c:$id").add(RDFS.LABEL, label).add(RDF.TYPE, "owl:Class")
                if (uri != null) {
                    add(OWL.EQUIVALENTCLASS, uri)
                }
            }.build()
            return Optional.of(model)
        }
    }

    override fun rdfModelForPredicate(id: ThingId): Optional<Model> {
        val predicate = predicateRepository.findById(id).orElse(null) ?: return Optional.empty()
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

    override fun rdfModelForResource(id: ThingId): Optional<Model> {
        val resource = resourceRepository.findById(id).orElse(null) ?: return Optional.empty()
        val statements = statementRepository.findAll(subjectId = resource.id, pageable = PageRequests.ALL)
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
                        is Class -> add("p:${it.predicate.id}", "c:${it.`object`.id}")
                        is Predicate -> add("p:${it.predicate.id}", "p:${it.`object`.id}")
                    }
                }
            }.build()
            return Optional.of(model)
        }
    }
}

fun Class.toNTriple(writer: Writer, classHierarchyRepository: ClassHierarchyRepository) {
    val cPrefix = RdfConstants.CLASS_NS
    writer.write("<$cPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .\n")
    if (uri != null) {
        writer.write("<$cPrefix${this.id}> <${OWL.EQUIVALENTCLASS}> <$uri> .\n")
    }
    writer.write("<$cPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiteral(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    classHierarchyRepository.findParentByChildId(id).ifPresent {
        writer.write("<$cPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <$cPrefix${it.id}> .\n")
    }
}

fun Predicate.toNTriple(writer: Writer) {
    val cPrefix = RdfConstants.CLASS_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    val predicate = "<$pPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Predicate> .\n" + "<$pPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#label> \"${
        escapeLiteral(
            label
        )
    }\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"
    writer.write(predicate)
}

fun Resource.toNTriple(writer: Writer) {
    val cPrefix = RdfConstants.CLASS_NS
    val rPrefix = RdfConstants.RESOURCE_NS
    if (Classes.list in classes) {
        writer.write("<$rPrefix$id> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq> .\n")
    } else {
        writer.write("<$rPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
        classes.forEach { writer.write("<$rPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
    }
    writer.write("<$rPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiteral(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
}

private val logger = LoggerFactory.getLogger(RDFService::class.java)

/**
 * Convert the triple to a statement in NTriple format.
 */
fun GeneralStatement.toNTriple(writer: Writer) {
    val pPrefix = RdfConstants.PREDICATE_NS
    val statement = if (predicate.id == Predicates.hasListElement && index != null && subject is Resource && Classes.list in (subject as Resource).classes) {
        "${serializeThing(subject)} <http://www.w3.org/1999/02/22-rdf-syntax-ns#_${index!! + 1}> ${serializeThing(`object`)} .\n"
    } else "${serializeThing(subject)} <$pPrefix${predicate.id}> ${serializeThing(`object`)} .\n"
    if (statement[0] == '"') {
        logger.warn("Encountered statement with literal subject: {}", id)
        return
    }
    writer.write(statement)
}

private fun serializeThing(thing: Thing): String {
    val rPrefix = RdfConstants.RESOURCE_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    val cPrefix = RdfConstants.CLASS_NS
    return when (thing) {
        is Resource -> "<$rPrefix${thing.id}>"
        is Predicate -> "<$pPrefix${thing.id}>"
        is Class -> "<$cPrefix${thing.id}>"
        is Literal -> "\"${escapeLiteral(thing.label)}\"^^<${thing.datatype.toDatatypeURL()}>"
    }
}

private fun String.toDatatypeURL(): String =
    // This will have issues if other prefixes are used.
    if (this.startsWith("xsd:"))
        "http://www.w3.org/2001/XMLSchema#${replace("xsd:", "")}"
    else
        this
