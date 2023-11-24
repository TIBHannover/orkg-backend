package org.orkg.graph.adapter.output.inmemory

import org.orkg.graph.output.EntityRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

abstract class InMemoryRepository<ID, T>(
    private val defaultComparator: Comparator<T>
) : EntityRepository<T, ID> {
    protected val entities: MutableMap<ID, T> = mutableMapOf()

    override fun findAll(pageable: Pageable) =
        findAllFilteredAndPaged(pageable) { true }

    override fun exists(id: ID) = entities.contains(id)

    protected fun findAllFilteredAndPaged(
        pageable: Pageable,
        comparator: Comparator<T> = defaultComparator,
        predicate: (T) -> Boolean
    ) = entities.values
        .filter(predicate)
        .sortedWith(comparator)
        .paged(pageable)
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
