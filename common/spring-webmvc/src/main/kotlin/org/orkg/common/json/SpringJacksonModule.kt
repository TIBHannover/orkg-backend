package org.orkg.common.json

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.common.UnpagedPageableRepresentation
import org.springframework.data.domain.PageImpl
import org.springframework.data.web.PagedModel

class SpringJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.setMixInAnnotations(PageImpl::class.java, PageImplMixin::class.java)
        context?.setMixInAnnotations(PagedModel.PageMetadata::class.java, PageMetadataMixin::class.java)
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(UnpagedPageableRepresentation::class.java, UnpagedPageableRepresentationSerializer())
        })
    }
}
