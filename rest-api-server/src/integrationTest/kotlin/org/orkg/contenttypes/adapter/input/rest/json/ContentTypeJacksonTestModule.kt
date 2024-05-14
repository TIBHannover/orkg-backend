package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleModule
import org.orkg.contenttypes.domain.LiteralReference
import org.orkg.contenttypes.domain.ThingReference
import org.springframework.stereotype.Component

@Component
class ContentTypeJacksonTestModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.setMixInAnnotations(ThingReference::class.java, ThingReferenceMixin::class.java)
        context?.setMixInAnnotations(LiteralReference::class.java, LiteralReferenceMixin::class.java)
    }
}
