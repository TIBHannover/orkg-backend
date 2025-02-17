package org.orkg.graph.adapter.output.inmemory

import org.orkg.graph.output.EntityRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

abstract class InMemoryRepository<ID, T>(
    private val defaultComparator: Comparator<T>
) : EntityRepository<T, ID> {
    abstract val entities: InMemoryEntityAdapter<ID, T>

    override fun findAll(pageable: Pageable): Page<T> =
        findAllFilteredAndPaged(pageable) { true }

    override fun existsById(id: ID): Boolean = entities.contains(id)

    protected fun findAllFilteredAndPaged(
        pageable: Pageable,
        comparator: Comparator<T> = defaultComparator,
        predicate: (T) -> Boolean
    ): Page<T> = entities.values
        .filter(predicate)
        .sortedWith(comparator)
        .paged(pageable)
}

interface InMemoryEntityAdapter<ID, T> : Iterable<T> {
    val keys: Collection<ID>
    val values: Collection<T>
    val size: Int get() = values.size

    fun remove(key: ID): T?
    fun clear()

    operator fun contains(id: ID): Boolean = get(id) != null
    operator fun get(key: ID): T?
    operator fun set(key: ID, value: T): T?

    override fun iterator(): Iterator<T> = values.iterator()
}

fun <T> List<T>.paged(pageable: Pageable): PageImpl<T> {
    val content = this
        .drop(pageable.pageNumber * pageable.pageSize)
        .take(pageable.pageSize)
    return PageImpl(
        content,
        PageRequest.of(pageable.pageNumber, pageable.pageSize),
        this.size.toLong()
    )
}
