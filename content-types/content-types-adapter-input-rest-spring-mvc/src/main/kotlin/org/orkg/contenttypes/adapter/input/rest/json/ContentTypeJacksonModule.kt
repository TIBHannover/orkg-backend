package org.orkg.contenttypes.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.contenttypes.adapter.input.rest.IdentifierMapDTO
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRepresentation
import org.orkg.contenttypes.adapter.input.rest.TemplatePropertyRequest

class ContentTypeJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(IdentifierMapDTO::class.java, IdentifierMapDTODeserializer())
            addDeserializer(TemplatePropertyRequest::class.java, TemplatePropertyRequestDeserializer())
            addDeserializer(TemplatePropertyRepresentation::class.java, TemplatePropertyRepresentationDeserializer())
        })
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(IdentifierMapDTO::class.java, IdentifierMapDTOSerializer())
        })
    }
}
