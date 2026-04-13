package org.orkg.common

import org.eclipse.rdf4j.common.net.ParsedIRI
import java.io.Serial
import java.io.Serializable

/**
 * A class representing an Internationalized Resource Identifier, compatible with RFC 3987.
 *
 * It is currently implemented as a small wrapper around [ParsedIRI] from the RDF4J library.
 */
data class IRI(private val parsedIRI: ParsedIRI) : Serializable {
    constructor(iri: String) : this(ParsedIRI(iri))

    val isAbsolute: Boolean = parsedIRI.isAbsolute

    override fun toString(): String = parsedIRI.toString()

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4173230818027947904

        fun create(str: String): IRI = IRI(str)
    }
}
