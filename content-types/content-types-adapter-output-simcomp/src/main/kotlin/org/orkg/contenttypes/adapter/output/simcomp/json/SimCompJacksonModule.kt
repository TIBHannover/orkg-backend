package org.orkg.contenttypes.adapter.output.simcomp.json

import org.orkg.contenttypes.domain.PublishedContentType
import tools.jackson.databind.module.SimpleModule

class SimCompJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.setMixIn(PublishedContentType::class.java, PublishedContentTypeMixin::class.java)
    }
}
