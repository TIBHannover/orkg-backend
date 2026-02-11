package org.orkg.common.json

import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PagedModel
import tools.jackson.databind.module.SimpleModule

class SpringJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.setMixIn(PageImpl::class.java, PageImplMixin::class.java)
        context?.setMixIn(PagedModel::class.java, PagedModelMixin::class.java)
        context?.setMixIn(PagedModel.PageMetadata::class.java, PageMetadataMixin::class.java)
    }
}
