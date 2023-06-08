package eu.tib.orkg.prototype.export.rdf.domain

import eu.tib.orkg.prototype.export.rdf.api.ExportRDFUseCase
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.forEach
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.attribute.PosixFilePermissions.fromString
import java.util.*
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val DEFAULT_FILE_NAME = "rdf-export-orkg.nt"

@Service
@Transactional
class RDFService(
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val resourceRepository: ResourceRepository,
    private val classesRepository: ClassRepository,
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
            it.toNTriple(writer)
        }, writer::flush)
        statementRepository.forEach({
            it.toNTriple(writer)
        }, writer::flush)
    }

    override fun dumpToNTriple(path: String?) {
        val filePath = resolveFilePath(path)
        if (filePath.absolute().parent.exists().not()) throw NoSuchFileException(
            file = filePath.toFile(),
            reason = "The directory ${filePath.parent} does not exist! Make sure it was created, and that permissions are correct.",
        )
        val temp = Files.createTempFile("", "", *fileAttributes())
        OutputStreamWriter(FileOutputStream(temp.toFile()), Charsets.UTF_8).use {
            dumpToNTriple(it)
        }
        if (temp.exists()) {
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun rdfModelForClass(id: ThingId): Optional<Model> {
        val clazz = classesRepository.findById(id).orElse(null) ?: return Optional.empty()
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
        val statements =
            statementRepository.findAllBySubject(resource.id, PageRequest.of(0, Int.MAX_VALUE)) // FIXME
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

    private fun fileAttributes(): Array<FileAttribute<out Any>> {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            return arrayOf(PosixFilePermissions.asFileAttribute(fromString("rw-r--r--")))
        }
        return emptyArray()
    }

    internal fun resolveFilePath(path: String?): Path {
        if (path == null) {
            return Paths.get(DEFAULT_FILE_NAME)
        }
        val file = Paths.get(path)
        if (file.isDirectory()) {
            return file.resolve(DEFAULT_FILE_NAME)
        }
        return file
    }
}

fun Class.toNTriple(writer: Writer, classHierarchyRepository: ClassHierarchyRepository) {
    val cPrefix = RdfConstants.CLASS_NS
    writer.write("<$cPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .\n")
    if (uri?.isValidForNTriple() == true) writer.write("<$cPrefix${this.id}> <${OWL.EQUIVALENTCLASS}> <$uri> .\n")
    writer.write("<$cPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
    classHierarchyRepository.findParent(id).ifPresent {
        writer.write("<$cPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <$cPrefix${it.id}> .\n")
    }
}

fun Predicate.toNTriple(writer: Writer) {
    val cPrefix = RdfConstants.CLASS_NS
    val pPrefix = RdfConstants.PREDICATE_NS
    val predicate = "<$pPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Predicate> .\n" + "<$pPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#label> \"${
        escapeLiterals(
            label
        )
    }\"^^<http://www.w3.org/2001/XMLSchema#string> .\n"
    writer.write(predicate)
}

fun Resource.toNTriple(writer: Writer) {
    val cPrefix = RdfConstants.CLASS_NS
    val rPrefix = RdfConstants.RESOURCE_NS
    writer.write("<$rPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <${cPrefix}Resource> .\n")
    classes.forEach { writer.write("<$rPrefix${this.id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <$cPrefix${it.value}> .\n") }
    writer.write("<$rPrefix${this.id}> <http://www.w3.org/2000/01/rdf-schema#label> \"${escapeLiterals(label)}\"^^<http://www.w3.org/2001/XMLSchema#string> .\n")
}

/**
 * Convert the triple to a statement in NTriple format.
 */
fun GeneralStatement.toNTriple(writer: Writer) {
    val pPrefix = RdfConstants.PREDICATE_NS
    val statement = "${serializeThing(subject)} <$pPrefix${predicate.id}> ${serializeThing(`object`)} .\n"
    if (statement[0] == '"')
        // Ignore literal
        // TODO: log this somewhere
        return
    writer.write(statement)
}

/**
 * Checks whether a URI is valid to be included in RDF .nt serialization.
 */
fun URI.isValidForNTriple(): Boolean {
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
