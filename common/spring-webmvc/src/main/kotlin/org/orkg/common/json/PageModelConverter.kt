package org.orkg.common.json

import com.fasterxml.jackson.databind.util.StdConverter
import org.orkg.common.PageRepresentation
import org.springframework.data.domain.Page

class PageModelConverter : StdConverter<Page<*>?, PageRepresentation<*>?>() {
    override fun convert(value: Page<*>?): PageRepresentation<*>? = value?.let { PageRepresentation(value) }
}
