package org.orkg.common.json

import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId

class CommonJacksonModule : SimpleModule() {
    override fun setupModule(context: SetupContext?) {
        context?.addSerializers(
            SimpleSerializers().apply {
                addSerializer(ContributorId::class.java, ContributorIdSerializer())
                addSerializer(ObservatoryId::class.java, ObservatoryIdSerializer())
                addSerializer(OrganizationId::class.java, OrganizationIdSerializer())
                addSerializer(ThingId::class.java, ThingIdSerializer())
                addSerializer(ParsedIRI::class.java, ParsedIRISerializer())
            }
        )
        context?.addDeserializers(
            SimpleDeserializers().apply {
                addDeserializer(ContributorId::class.java, ContributorIdDeserializer())
                addDeserializer(ObservatoryId::class.java, ObservatoryIdDeserializer())
                addDeserializer(OrganizationId::class.java, OrganizationIdDeserializer())
                addDeserializer(ThingId::class.java, ThingIdDeserializer())
                addDeserializer(ParsedIRI::class.java, ParsedIRIDeserializer())
            }
        )
    }
}
