package org.orkg.community.adapter.input.rest.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import io.ipfs.multibase.Base16
import io.ipfs.multihash.Multihash
import io.ipfs.multihash.Multihash.Type
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import java.time.OffsetDateTime

class ContributorDeserializer
    @JvmOverloads
    constructor(
        vc: Class<*>? = null,
    ) : StdDeserializer<Contributor?>(vc) {
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Contributor {
            val node: JsonNode = jp.codec.readTree(jp)
            return Contributor(
                id = ContributorId(node["id"].asText()),
                name = node["display_name"].asText(),
                joinedAt = OffsetDateTime.parse(node["joined_at"].asText()),
                organizationId = OrganizationId(node["organization_id"].asText()),
                observatoryId = ObservatoryId(node["observatory_id"].asText()),
                emailHash = node["gravatar_id"].asText().let { gravatarId ->
                    val hash = Base16.decode(gravatarId)
                    if (hash.size == Type.md5.length) {
                        Multihash(Type.md5, hash)
                    } else {
                        Multihash(Type.sha2_256, hash)
                    }
                }
            )
        }
    }
