package eu.tib.orkg.prototype.statements.spi

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

interface EntityRepository<T, I> {
    fun findAll(pageable: Pageable): Page<T>
    fun exists(id: I): Boolean
}

/**
 * Performs the given `action` on each element in the repository.
 * Elements are loaded in chunks of size `chunkSize`.
 */
fun <T, I> EntityRepository<T, I>.forEach(action: (T) -> Unit, afterChunk: () -> Unit = {}, chunkSize: Int = 10_000) {
    var page: Page<T> = findAll(PageRequest.of(0, chunkSize))
    page.forEach(action)
    while (page.hasNext()) {
        page = findAll(page.nextPageable())
        page.forEach(action)
        afterChunk()
    }
}
