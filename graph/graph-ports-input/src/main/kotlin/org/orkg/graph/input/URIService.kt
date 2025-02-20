package org.orkg.graph.input

import org.eclipse.rdf4j.common.net.ParsedIRI
import java.util.Optional

/**
 * A service that provides functionality for entities addressable by a [ParsedIRI].
 */
interface URIService<T> {
    /**
     * Find an entity by its URI.
     *
     * URIs need to be unique, so that at most one element is returned.
     * The provided URI must match exactly.
     *
     * @param uri The [ParsedIRI] to be searched.
     * @return An [Optional] containing the found entity, or [Optional.EMPTY] otherwise.
     */
    fun findByURI(uri: ParsedIRI): Optional<T>
}
