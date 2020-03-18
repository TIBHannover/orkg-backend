package eu.tib.orkg.prototype.statements.domain.model.rdf

import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.WriterConfig
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL
import java.io.ByteArrayOutputStream

/**
 * Base interface for strategies to extend semantic RDF [Model]s.
 */
interface SemanticModelExtension<in T : Thing> {
    fun extend(model: Model, thing: T): Model
}

/**
 * A semantic description of information, using an RDF [Model].
 */
interface SemanticDescription {
    /**
     * Describe the object instance as an RDF [Model].
     *
     * @return The description of the object instance as RDF [Model].
     */
    fun describe(): Model
}

/**
 * A basic interface to render objects to [String] representations.
 */
interface Representation {
    fun render(): String
}

class RdfRepresentation(
    private val desc: SemanticDescription,
    private val format: RDFFormat = RDFFormat.NTRIPLES
) : Representation {
    override fun render(): String {
        val out = ByteArrayOutputStream()
        val config = WriterConfig().set(XSD_STRING_TO_PLAIN_LITERAL, false)
        val model = desc.describe()
        Rio.write(model, out, format, config)
        return out.toString()
    }
}
