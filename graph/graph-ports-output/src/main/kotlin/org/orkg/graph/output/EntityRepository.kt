package org.orkg.graph.output

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

interface EntityRepository<T : Any, I> {
    fun findAll(pageable: Pageable): Page<T>

    fun existsById(id: I): Boolean
}

/**
 * Performs the given `action` on each element in the repository.
 * Elements are loaded in chunks of size `chunkSize`.
 */
fun <T : Any, I> EntityRepository<T, I>.forEach(action: (T) -> Unit, afterChunk: () -> Unit = {}, chunkSize: Int = 10_000) {
    var page: Page<T> = findAll(PageRequest.of(0, chunkSize))
    page.forEach(action)
    while (page.hasNext()) {
        page = findAll(page.nextPageable())
        page.forEach(action)
        afterChunk()
    }
}
