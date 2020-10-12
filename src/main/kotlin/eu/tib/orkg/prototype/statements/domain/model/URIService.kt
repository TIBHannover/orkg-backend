package eu.tib.orkg.prototype.statements.domain.model

import java.net.URI
import java.util.Optional

/**
 * A service that provides functionality for entities addressable by a [URI].
 */
interface URIService<T> {
    /**
     * Find an entity by its URI.
     *
     * URIs need to be unique, so that at most one element is returned.
     * The provided URI must match exactly.
     *
     * @param uri The [URI] to be searched.
     * @return An [Optional] containing the found entity, or [Optional.EMPTY] otherwise.
     */
    fun findByURI(uri: URI): Optional<out T>
}
