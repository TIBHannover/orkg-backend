package org.orkg.mediastorage.adapter.input.serialization.json

import org.orkg.mediastorage.domain.ImageId
import tools.jackson.databind.module.SimpleDeserializers
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.module.SimpleSerializers

class MediaStorageJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(
            SimpleSerializers().apply {
                addSerializer(ImageId::class.java, ImageIdSerializer())
            }
        )
        context?.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(ImageId::class.java, ImageIdDeserializer())
            }
        )
    }
}
