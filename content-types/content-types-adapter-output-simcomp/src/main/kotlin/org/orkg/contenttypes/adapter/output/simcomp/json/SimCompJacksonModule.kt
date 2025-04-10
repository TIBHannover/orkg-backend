package org.orkg.contenttypes.adapter.output.simcomp.json

import com.fasterxml.jackson.databind.module.SimpleModule
import org.orkg.contenttypes.domain.PublishedContentType

class SimCompJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.setMixInAnnotations(PublishedContentType::class.java, PublishedContentTypeMixin::class.java)
    }
}
