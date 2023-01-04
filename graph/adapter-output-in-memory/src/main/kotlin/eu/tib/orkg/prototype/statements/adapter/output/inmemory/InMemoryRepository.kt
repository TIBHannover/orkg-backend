package eu.tib.orkg.prototype.statements.adapter.output.inmemory

import eu.tib.orkg.prototype.statements.spi.EntityRepository
import org.springframework.data.domain.Page
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
        predicate: (T) -> Boolean
    ): Page<T> {
        val total = entities.values
            .filter(predicate)
            .sortedWith(defaultComparator)
        val content = total
            .drop(pageable.pageNumber * pageable.pageSize)
            .take(pageable.pageSize)
        return PageImpl(
            content,
            PageRequest.of(pageable.pageNumber, pageable.pageSize),
            total.size.toLong()
        )
    }
}
