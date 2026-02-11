package org.orkg.common.json

import org.springframework.data.domain.Page
import org.springframework.data.web.PagedModel
import tools.jackson.databind.util.StdConverter

class PageModelConverter : StdConverter<Page<*>?, PagedModel<*>?>() {
    override fun convert(value: Page<*>?): PagedModel<*>? = value?.let { PagedModel(value) }
}
