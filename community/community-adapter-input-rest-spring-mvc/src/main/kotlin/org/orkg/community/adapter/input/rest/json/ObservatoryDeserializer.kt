package org.orkg.community.adapter.input.rest.json

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Observatory
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer
import java.util.UUID

class ObservatoryDeserializer : ValueDeserializer<Observatory>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Observatory {
        val node = ctxt.readTree(p)
        return Observatory(
            id = ObservatoryId(UUID.fromString(node["id"].asString())),
            name = node["name"].asString(),
            description = node["description"].asString(),
            researchField = node["research_field"]["id"].takeIf { !it.isNull }?.let { ThingId(it.asString()) },
            members = node["members"].map { ContributorId(UUID.fromString(it.asString())) }.toSet(),
            organizationIds = node["organization_ids"].map { OrganizationId(UUID.fromString(it.asString())) }.toSet(),
            displayId = node["display_id"].asString(),
            sustainableDevelopmentGoals = node["sdgs"].map { ThingId(it.stringValue(null)) }.toSet()
        )
    }
}
