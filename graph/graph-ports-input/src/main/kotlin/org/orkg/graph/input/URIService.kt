package org.orkg.graph.input

import org.orkg.common.IRI
import java.util.Optional

/**
 * A service that provides functionality for entities addressable by a [IRI].
 */
interface URIService<T> {
    /**
     * Find an entity by its URI.
     *
     * URIs need to be unique, so that at most one element is returned.
     * The provided URI must match exactly.
     *
     * @param uri The [IRI] to be searched.
     * @return An [Optional] containing the found entity, or [Optional.EMPTY] otherwise.
     */
    fun findByURI(uri: IRI): Optional<T>
}
