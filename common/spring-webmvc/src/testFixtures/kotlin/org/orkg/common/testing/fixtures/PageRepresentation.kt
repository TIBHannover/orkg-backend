package org.orkg.common.testing.fixtures

import com.fasterxml.jackson.annotation.JsonProperty

data class PageRepresentation<T>(
    val content: List<T>,
    @field:JsonProperty("page")
    val pageInfo: PageInfo,
) {
    data class PageInfo(
        val number: Int,
        val size: Int,
        @field:JsonProperty("total_elements")
        val totalElements: Long,
        @field:JsonProperty("total_pages")
        val totalPages: Long,
    )
}
