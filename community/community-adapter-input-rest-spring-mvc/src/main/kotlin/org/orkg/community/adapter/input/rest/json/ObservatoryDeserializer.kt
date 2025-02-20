package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Observatory
import java.util.UUID

class ObservatoryDeserializer
    @JvmOverloads
    constructor(
        vc: Class<*>? = null,
    ) : StdDeserializer<Observatory?>(vc) {
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Observatory {
            val node: JsonNode = jp.codec.readTree(jp)
            return Observatory(
                id = ObservatoryId(UUID.fromString(node["id"].asText())),
                name = node["name"].asText(),
                description = node["description"].asText(),
                researchField = node["research_field"]["id"].takeIf { !it.isNull }?.let { ThingId(it.asText()) },
                members = node["members"].map { ContributorId(UUID.fromString(it.asText())) }.toSet(),
                organizationIds = node["organization_ids"].map { OrganizationId(UUID.fromString(it.asText())) }.toSet(),
                displayId = node["display_id"].asText(),
                sustainableDevelopmentGoals = node["sdgs"].map { ThingId(it.textValue()) }.toSet()
            )
        }
    }
