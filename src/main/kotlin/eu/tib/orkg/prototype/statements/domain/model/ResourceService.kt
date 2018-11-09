package eu.tib.orkg.prototype.statements.domain.model

import java.util.*

interface ResourceService {
    /**
     * Create a new resource with a given label.
     *
     * @return the newly created resource
     */
    fun create(label: String): Resource

    /**
     * Find all resources.
     */
    fun findAll(): Iterable<Resource>

    /**
     * Find a resource by its ID.
     */
    fun findById(id: ResourceId?): Optional<Resource>

    /**
     * Find all resources matching a label.
     */
    fun findAllByLabel(label: String): Iterable<Resource>

    /**
     * Find all resources matching a label partially.
     */
    fun findAllByLabelContaining(part: String): Iterable<Resource>
}
