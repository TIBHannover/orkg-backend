package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRequest

class ContentTypeJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(TemplatePropertyRequest::class.java, TemplatePropertyRequestDeserializer())
            addDeserializer(TemplatePropertyRepresentation::class.java, TemplatePropertyRepresentationDeserializer())
        })
    }
}
