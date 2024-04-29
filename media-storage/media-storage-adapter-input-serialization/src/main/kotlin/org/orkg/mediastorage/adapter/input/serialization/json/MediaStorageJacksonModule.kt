package org.orkg.mediastorage.adapter.input.serialization.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.mediastorage.domain.ImageId

class MediaStorageJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(ImageId::class.java, ImageIdSerializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(ImageId::class.java, ImageIdDeserializer())
        })
    }
}
