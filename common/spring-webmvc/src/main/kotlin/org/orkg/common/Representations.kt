package org.orkg.common

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page
import org.springframework.data.web.PagedModel

class PageRepresentation<T>(page: Page<T>) : PagedModel<T>(page) {
    val pageable: PageableRepresentation = page.pageable.toRepresentation()
    val last: Boolean = page.isLast
    val totalElements: Long = page.totalElements
    val totalPages: Int = page.totalPages
    val first: Boolean = page.isFirst
    val size: Int = page.size
    val number: Int = page.number
    val sort: SortRepresentation = page.sort.toRepresentation()
    val numberOfElements: Int = page.numberOfElements
    val empty: Boolean = page.isEmpty

    @JsonProperty(index = 0)
    override fun getContent(): MutableList<T> = super.getContent()
}

interface PageableRepresentation

data class PagedPageableRepresentation(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: SortRepresentation,
    val paged: Boolean,
    val unpaged: Boolean,
    val offset: Long,
) : PageableRepresentation

class UnpagedPageableRepresentation : PageableRepresentation

data class SortRepresentation(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean,
)
