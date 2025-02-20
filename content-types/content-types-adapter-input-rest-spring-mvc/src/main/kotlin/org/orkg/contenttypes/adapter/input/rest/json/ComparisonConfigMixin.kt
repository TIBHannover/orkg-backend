package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

abstract class ComparisonConfigMixin(
    @field:JsonProperty("short_codes")
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
    val shortCodes: List<String>,
)
