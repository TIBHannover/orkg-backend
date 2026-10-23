package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.model.Triple
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory

class RemappingValueFactory(
    private val iriMapping: Map<String, String>,
) : SimpleValueFactory() {
    override fun createIRI(iri: String): IRI =
        super.createIRI(remapIRI(iri))

    override fun createIRI(namespace: String, localName: String): IRI {
        val joined = namespace + localName
        return if (joined in iriMapping) {
            super.createIRI(remapIRI(joined))
        } else {
            super.createIRI(namespace, localName)
        }
    }

    override fun createStatement(subject: Resource, predicate: IRI, `object`: Value): Statement =
        super.createStatement(remapResource(subject), remapIRI(predicate), remapValue(`object`), null)

    override fun createStatement(subject: Resource, predicate: IRI, `object`: Value, context: Resource?): Statement =
        super.createStatement(remapResource(subject), remapIRI(predicate), remapValue(`object`), context?.let(::remapResource))

    override fun createTriple(subject: Resource, predicate: IRI, `object`: Value): Triple =
        super.createTriple(remapResource(subject), remapIRI(predicate), remapValue(`object`))

    private fun remapIRI(iri: String): String =
        iriMapping.getOrDefault(iri, iri)

    private fun remapIRI(iri: IRI): IRI =
        if (iri.toString() in iriMapping) {
            super.createIRI(iriMapping[iri.toString()])
        } else {
            iri
        }

    private fun remapResource(resource: Resource): Resource =
        when {
            resource.isIRI -> remapIRI(resource as IRI)
            resource.isTriple -> remapTriple(resource as Triple)
            else -> resource
        }

    private fun remapValue(value: Value): Value =
        when {
            value.isResource -> remapResource(value as Resource)
            value.isLiteral -> value
            else -> throw IllegalStateException("Unexpected value type ${value.javaClass.canonicalName}")
        }

    private fun remapTriple(triple: Triple): Triple =
        super.createTriple(remapResource(triple.subject), remapIRI(triple.predicate), remapValue(triple.`object`))
}
