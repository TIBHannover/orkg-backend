package eu.tib.orkg.prototype.statements.domain.model

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
}
