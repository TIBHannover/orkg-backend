package org.orkg.contenttypes.adapter.input.rest.json

import org.orkg.contenttypes.domain.LiteralReference
import org.orkg.contenttypes.domain.ThingReference
import org.springframework.stereotype.Component
import tools.jackson.databind.module.SimpleModule

@Component
class ContentTypeJacksonTestModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.setMixIn(ThingReference::class.java, ThingReferenceMixin::class.java)
        context?.setMixIn(LiteralReference::class.java, LiteralReferenceMixin::class.java)
    }
}
