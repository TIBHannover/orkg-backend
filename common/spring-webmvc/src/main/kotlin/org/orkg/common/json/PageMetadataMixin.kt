package org.orkg.common.json

import com.fasterxml.jackson.annotation.JsonProperty

abstract class PageMetadataMixin(
    @field:JsonProperty("total_elements")
    val totalElements: Long,
    @field:JsonProperty("total_pages")
    val totalPages: Long
)
