package org.orkg.common.json

import com.fasterxml.jackson.annotation.JsonProperty

abstract class PagedModelMixin<T> {
    @JsonProperty(index = 0)
    abstract fun getContent(): MutableList<T>
}
