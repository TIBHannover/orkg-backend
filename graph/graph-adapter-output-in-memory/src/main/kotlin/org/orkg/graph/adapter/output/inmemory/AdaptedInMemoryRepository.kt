package org.orkg.graph.adapter.output.inmemory

import org.orkg.graph.output.EntityRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

abstract class AdaptedInMemoryRepository<ID, T>(
    private val defaultComparator: Comparator<T>
) : EntityRepository<T, ID> {
    abstract val entities: InMemoryEntityAdapter<ID, T>

    override fun findAll(pageable: Pageable): Page<T> =
        findAllFilteredAndPaged(pageable) { true }

    override fun exists(id: ID): Boolean = entities.contains(id)

    protected fun findAllFilteredAndPaged(
        pageable: Pageable,
        comparator: Comparator<T> = defaultComparator,
        predicate: (T) -> Boolean
    ): Page<T> = entities.values
        .filter(predicate)
        .sortedWith(comparator)
        .paged(pageable)
}

interface InMemoryEntityAdapter<ID, T> {
    val values: Collection<T>
    val size: Int get() = values.size

    fun remove(key: ID): T?
    fun clear()

    operator fun contains(id: ID): Boolean
    operator fun get(key: ID): T?
    operator fun set(key: ID, value: T): T?
}
