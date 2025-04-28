package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.orkg.community.domain.ConferenceSeriesId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.ContributorIdentifier
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.ObservatoryFilterId

class CommunityJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(
            SimpleSerializers().apply {
                addSerializer(ConferenceSeriesId::class.java, ConferenceSeriesIdSerializer())
                addSerializer(ObservatoryFilterId::class.java, ObservatoryFilterIdSerializer())
                addSerializer(ContributorIdentifier.Type::class.java, ContributorIdentifierTypeSerializer())
            }
        )
        context?.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(ConferenceSeriesId::class.java, ConferenceSeriesIdDeserializer())
                addDeserializer(ObservatoryFilterId::class.java, ObservatoryFilterIdDeserializer())
                addDeserializer(Observatory::class.java, ObservatoryDeserializer())
                addDeserializer(Contributor::class.java, ContributorDeserializer())
                addDeserializer(ContributorIdentifier.Type::class.java, ContributorIdentifierTypeDeserializer())
            }
        )
    }
}
