package org.orkg.common.json

import com.fasterxml.jackson.databind.util.StdConverter
import org.springframework.data.domain.Page
import org.springframework.data.web.PagedModel

class PageModelConverter : StdConverter<Page<*>?, PagedModel<*>?>() {
    override fun convert(value: Page<*>?): PagedModel<*>? = value?.let { PagedModel(value) }
}
