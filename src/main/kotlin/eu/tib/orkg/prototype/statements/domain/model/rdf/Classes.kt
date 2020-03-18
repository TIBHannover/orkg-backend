package eu.tib.orkg.prototype.statements.domain.model.rdf

import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.Class
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.OWL
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import java.net.URI

typealias ClassModelExtension = SemanticModelExtension<Class>

/**
 * A decorator to describe [Class]es in RDF.
 */
data class RdfClass(
    private val c: Class,
    private val extendWith: List<ClassModelExtension> = listOf()
) : SemanticDescription {
    private val factory: SimpleValueFactory = SimpleValueFactory.getInstance()

    override fun describe(): Model {
        var description = ModelBuilder()
            .setNamespace("c", RdfConstants.CLASS_NS)
            .subject("c:${c.id}")
            .add(RDF.TYPE, OWL.CLASS)
            .add(RDFS.LABEL, c.label.toLiteral())
            .build()

        // Extend the model if a URI is present. Needs modification via a new builder,
        // since calling add() on "description" does not interpret namespaces. Bug?
        if (c.uri != null)
            description = ModelBuilder(description)
                .subject("c:${c.id}")
                .add(OWL.EQUIVALENTCLASS, c.uri.toIRI())
                .build()

        // Apply all extensions, if given
        if (extendWith.isNotEmpty()) {
            extendWith.fold(description) { model, extension ->
                extension.extend(model, c)
            }
        }
        return description
    }

    private fun String.toLiteral(): Literal = factory.createLiteral(this)

    private fun URI.toIRI(): IRI = factory.createIRI(this.toString())
}

/**
 * Strategy to extend an [RdfClass] with ontology information.
 */
class ClassOntology : ClassModelExtension {
    override fun extend(model: Model, thing: Class) = model
}

/**
 * Strategy to extend an [RdfClass] with provenance information.
 */
class ClassProvenance : ClassModelExtension {
    override fun extend(model: Model, thing: Class) = model
}
