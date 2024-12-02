package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryFilterId

class CommunityJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(ConferenceSeriesId::class.java, ConferenceSeriesIdSerializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(ConferenceSeriesId::class.java, ConferenceSeriesIdDeserializer())
        })
        context?.addSerializers(SimpleSerializers().apply {
            addSerializer(ObservatoryFilterId::class.java, ObservatoryFilterIdSerializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(ObservatoryFilterId::class.java, ObservatoryFilterIdDeserializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(Observatory::class.java, ObservatoryDeserializer())
        })
        context?.addDeserializers(SimpleDeserializers().apply {
            addDeserializer(Contributor::class.java, ContributorDeserializer())
        })
    }
}
