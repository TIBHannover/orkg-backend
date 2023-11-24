package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.community.domain.ConferenceSeriesId

class CommunityJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(ConferenceSeriesId::class.java, ConferenceSeriesIdSerializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(ConferenceSeriesId::class.java, ConferenceSeriesIdDeserializer())
        })
    }
}
