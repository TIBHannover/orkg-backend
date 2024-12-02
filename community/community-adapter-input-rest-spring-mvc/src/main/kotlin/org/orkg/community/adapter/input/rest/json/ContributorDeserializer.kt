package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.internal.MD5Hash
import java.time.OffsetDateTime

class ContributorDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<Contributor?>(vc) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Contributor {
        val node: JsonNode = jp.codec.readTree(jp)
        return Contributor(
            id = ContributorId(node["id"].asText()),
            name = node["display_name"].asText(),
            joinedAt = OffsetDateTime.parse(node["joined_at"].asText()),
            organizationId = OrganizationId(node["organization_id"].asText()),
            observatoryId = ObservatoryId(node["observatory_id"].asText()),
            emailMD5 = MD5Hash(node["gravatar_id"].asText())
        )
    }
}
