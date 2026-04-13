package org.orkg.common.json

import org.orkg.common.ContributorId
import org.orkg.common.IRI
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import tools.jackson.databind.module.SimpleDeserializers
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.module.SimpleSerializers

class CommonJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(
            SimpleSerializers().apply {
                addSerializer(ContributorId::class.java, ContributorIdSerializer())
                addSerializer(ObservatoryId::class.java, ObservatoryIdSerializer())
                addSerializer(OrganizationId::class.java, OrganizationIdSerializer())
                addSerializer(ThingId::class.java, ThingIdSerializer())
                addSerializer(IRI::class.java, IRISerializer())
            },
        )
        context?.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(ContributorId::class.java, ContributorIdDeserializer())
                addDeserializer(ObservatoryId::class.java, ObservatoryIdDeserializer())
                addDeserializer(OrganizationId::class.java, OrganizationIdDeserializer())
                addDeserializer(ThingId::class.java, ThingIdDeserializer())
                addDeserializer(IRI::class.java, IRIDeserializer())
            },
        )
    }
}
